package org.dosnavigator.panels;

import org.dosnavigator.fs.DirectoryModel;
import org.dosnavigator.fs.FileRecord;
import org.dosnavigator.fs.FileSystemService;
import org.dosnavigator.terminal.Color;
import org.dosnavigator.terminal.KeyModifier;
import org.dosnavigator.terminal.KeyStroke;
import org.dosnavigator.terminal.MouseAction;
import org.dosnavigator.terminal.MouseEvent;
import org.dosnavigator.terminal.TerminalDriver;
import org.dosnavigator.ui.Box;
import org.dosnavigator.ui.ColorPalette;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.text.NumberFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

public final class FilePanel {
    private static final long QUICK_SEARCH_TIMEOUT_MILLIS = 1_000L;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    private static final int BRIEF_LINE_LENGTH = 18;
    private static final int FOOTER_HEIGHT = 4;
    private static final List<SortPopupItem> SORT_POPUP_ITEMS = List.of(
            new SortPopupItem("Name", PanelSortMode.NAME, null),
            new SortPopupItem("Extension", PanelSortMode.EXTENSION, null),
            new SortPopupItem("Size", PanelSortMode.SIZE, null),
            new SortPopupItem("Date", PanelSortMode.MODIFIED_TIME, null),
            new SortPopupItem("Creation date", PanelSortMode.CREATION_TIME, null),
            new SortPopupItem("Last access", PanelSortMode.ACCESS_TIME, null),
            new SortPopupItem("DIZ", PanelSortMode.DESCRIPTION, null),
            new SortPopupItem("Unsorted", PanelSortMode.UNSORTED, null),
            new SortPopupItem("Owner first", null, PanelSortFlag.OWNER_FIRST),
            new SortPopupItem("Sort by type", null, PanelSortFlag.SORT_BY_TYPE),
            new SortPopupItem("Inverted", null, PanelSortFlag.INVERTED),
            new SortPopupItem("Dirs by name", null, PanelSortFlag.DIRS_BY_NAME)
    );

    private final FileSystemService fileSystem;
    private final NumberFormat numberFormat = NumberFormat.getIntegerInstance(Locale.US);
    private final Set<Path> selectedPaths = new LinkedHashSet<>();
    private DirectoryModel model;
    private Path directory;
    private List<FileRecord> records = new ArrayList<>();
    private PanelSortMode sortMode = PanelSortMode.NAME;
    private final EnumSet<PanelSortFlag> sortFlags = EnumSet.of(PanelSortFlag.DIRS_BY_NAME);
    private PanelViewMode viewMode = PanelViewMode.BRIEF;
    private boolean reverseSort;
    private boolean longNames = true;
    private boolean showHiddenAndSystem = true;
    private int selectedIndex;
    private int topIndex;
    private int visibleRows = 20;
    private int visibleColumns = 1;
    private String quickSearch = "";
    private long quickSearchUpdatedAt;
    private boolean sortPopupOpen;
    private int sortPopupIndex;
    private String message = "";

    public FilePanel(FileSystemService fileSystem, Path directory) {
        this.fileSystem = fileSystem;
        this.directory = directory.toAbsolutePath().normalize();
        reload();
    }

    public void render(TerminalDriver terminal, Box box, boolean active, ColorPalette colors) {
        Color borderColor = active ? colors.activeBorder().foreground() : colors.inactiveBorder().foreground();
        box.draw(terminal, borderColor, colors.panel().background());

        String title = " " + directory + " ";
        terminal.putString(
                box.x() + 2,
                box.y(),
                trim(title, box.width() - 4),
                active ? colors.panelTitle().foreground() : colors.inactiveBorder().foreground(),
                colors.panel().background()
        );

        int contentHeight = contentHeight(box);
        visibleRows = Math.max(1, contentHeight);
        visibleColumns = viewMode == PanelViewMode.BRIEF ? briefColumns(panelListWidth(box)) : 1;
        keepSelectionVisible();

        renderHeader(terminal, box, colors);
        for (int row = 1; row < contentHeight; row++) {
            renderContentRow(terminal, box, row, active, colors);
        }
        renderScrollbar(terminal, box, colors);
        renderFooter(terminal, box, colors);
        if (sortPopupOpen) {
            renderSortPopup(terminal, box, colors);
        }
    }

    public void renderRows(TerminalDriver terminal, Box box, boolean active, ColorPalette colors, Iterable<Integer> rows) {
        render(terminal, box, active, colors);
    }

    public void handleKey(KeyStroke key) {
        if (sortPopupOpen) {
            handleSortPopupKey(key);
            return;
        }
        switch (key.keyType()) {
            case ArrowUp -> moveSelection(-1);
            case ArrowDown -> moveSelection(1);
            case ArrowLeft -> moveByColumn(-1);
            case ArrowRight -> moveByColumn(1);
            case PageUp -> moveSelection(-visibleRows);
            case PageDown -> moveSelection(visibleRows);
            case Home -> moveTo(0);
            case End -> moveTo(records.size() - 1);
            case Enter -> openSelected();
            case Backspace -> openParent();
            case Insert -> toggleSelectedAndAdvance();
            case GrayPlus -> selectByMask("*", key.hasModifier(KeyModifier.SHIFT));
            case GrayMinus -> deselectByMask("*", key.hasModifier(KeyModifier.SHIFT));
            case GrayStar -> invertSelection(key.hasModifier(KeyModifier.CTRL));
            case F9 -> openSortPopup();
            case Character -> handleCharacter(key);
            case Mouse -> handleMouse(key.mouseEvent());
            default -> {
            }
        }
    }

    public String statusText() {
        if (!message.isBlank()) {
            return message;
        }
        if (records.isEmpty()) {
            return directory + " | empty";
        }
        FileRecord record = records.get(selectedIndex);
        return directory + " | " + record.name() + " | " + describeSize(record)
                + " | " + selectedCount() + " selected, " + numberFormat.format(selectedSize()) + " bytes";
    }

    public int selectedIndex() {
        return selectedIndex;
    }

    public int topIndex() {
        return topIndex;
    }

    public int visibleRows() {
        return visibleRows;
    }

    public Path directory() {
        return directory;
    }

    public PanelSortMode sortMode() {
        return sortMode;
    }

    public int selectedCount() {
        return selectedPaths.size();
    }

    public long selectedSize() {
        return records.stream()
                .filter(record -> selectedPaths.contains(record.path()))
                .filter(record -> !record.directory())
                .mapToLong(FileRecord::size)
                .filter(size -> size > 0)
                .sum();
    }

    public List<FileRecord> records() {
        return List.copyOf(records);
    }

    public void setSortMode(PanelSortMode sortMode) {
        this.sortMode = sortMode;
        applyOrdering();
    }

    public void setReverseSort(boolean reverseSort) {
        this.reverseSort = reverseSort;
        if (reverseSort) {
            sortFlags.add(PanelSortFlag.INVERTED);
        } else {
            sortFlags.remove(PanelSortFlag.INVERTED);
        }
        applyOrdering();
    }

    public void selectByMask(String mask) {
        selectByMask(mask, false);
    }

    public void selectByMask(String mask, boolean includeDirectories) {
        Pattern pattern = maskPattern(mask);
        for (FileRecord record : records) {
            if (!record.parentEntry() && (includeDirectories || !record.directory()) && pattern.matcher(record.name()).matches()) {
                selectedPaths.add(record.path());
            }
        }
    }

    public void deselectByMask(String mask) {
        deselectByMask(mask, false);
    }

    public void deselectByMask(String mask, boolean includeDirectories) {
        Pattern pattern = maskPattern(mask);
        selectedPaths.removeIf(path -> records.stream()
                .anyMatch(record -> record.path().equals(path)
                        && (includeDirectories || !record.directory())
                        && pattern.matcher(record.name()).matches()));
    }

    public void invertSelection() {
        invertSelection(false);
    }

    public void invertSelection(boolean includeDirectories) {
        for (FileRecord record : records) {
            if (record.parentEntry() || (!includeDirectories && record.directory())) {
                continue;
            }
            if (!selectedPaths.remove(record.path())) {
                selectedPaths.add(record.path());
            }
        }
    }

    private void handleCharacter(KeyStroke key) {
        char character = key.character() == null ? 0 : key.character();
        if (key.hasModifier(KeyModifier.ALT)) {
            handleAltSelection(Character.toLowerCase(character), key);
            return;
        }
        if (character == '*') {
            invertSelection(key.hasModifier(KeyModifier.CTRL));
            return;
        }
        if (character == ' ') {
            toggleSelectedAndAdvance();
            return;
        }
        if (!Character.isISOControl(character)) {
            quickSearch(character);
        }
    }

    private void handleAltSelection(char command, KeyStroke key) {
        switch (command) {
            case '+', '=' -> selectSameBaseName(true, key.hasModifier(KeyModifier.SHIFT));
            case '-' -> selectSameBaseName(false, key.hasModifier(KeyModifier.SHIFT));
            case '*' -> selectSameExtension(true);
            case '/' -> selectSameExtension(false);
            default -> {
            }
        }
    }

    private void selectSameBaseName(boolean select, boolean invert) {
        FileRecord current = currentRecord();
        if (current == null || current.parentEntry()) {
            return;
        }
        String base = baseName(current.name());
        for (FileRecord record : records) {
            if (!record.parentEntry() && !record.directory()
                    && (baseName(record.name()).equalsIgnoreCase(base) ^ invert)) {
                setRecordSelected(record, select);
            }
        }
    }

    private void selectSameExtension(boolean select) {
        FileRecord current = currentRecord();
        if (current == null || current.parentEntry()) {
            return;
        }
        String extension = current.extension();
        for (FileRecord record : records) {
            if (!record.parentEntry() && !record.directory()
                    && record.extension().equalsIgnoreCase(extension)) {
                setRecordSelected(record, select);
            }
        }
    }

    private void quickSearch(char character) {
        long now = System.currentTimeMillis();
        if (now - quickSearchUpdatedAt > QUICK_SEARCH_TIMEOUT_MILLIS) {
            quickSearch = "";
        }
        quickSearch += Character.toLowerCase(character);
        quickSearchUpdatedAt = now;
        for (int i = 0; i < records.size(); i++) {
            FileRecord record = records.get(i);
            if (!record.parentEntry() && record.name().toLowerCase(Locale.ROOT).startsWith(quickSearch)) {
                moveTo(i);
                return;
            }
        }
    }

    private void toggleSelectedAndAdvance() {
        if (records.isEmpty()) {
            return;
        }
        FileRecord record = records.get(selectedIndex);
        if (!record.parentEntry()) {
            if (!selectedPaths.remove(record.path())) {
                selectedPaths.add(record.path());
            }
            moveSelection(1);
        }
    }

    private void handleMouse(MouseEvent mouse) {
        if (mouse == null) {
            return;
        }
        if (mouse.action() == MouseAction.WHEEL_UP) {
            moveSelection(-3);
            return;
        }
        if (mouse.action() == MouseAction.WHEEL_DOWN) {
            moveSelection(3);
            return;
        }
    }

    public boolean handleMouse(MouseEvent mouse, Box box) {
        if (mouse == null || !box.contains(mouse.x(), mouse.y())) {
            return false;
        }
        if (mouse.action() == MouseAction.WHEEL_UP) {
            moveSelection(-3);
            return true;
        }
        if (mouse.action() == MouseAction.WHEEL_DOWN) {
            moveSelection(3);
            return true;
        }
        if (mouse.action() != MouseAction.PRESS || mouse.button() != 0) {
            return true;
        }
        if (sortPopupOpen) {
            handleSortPopupMouse(mouse, box);
            return true;
        }
        if (mouse.y() == box.y() && mouse.x() >= box.x() && mouse.x() <= box.x() + 2) {
            openSortPopup();
            return true;
        }
        int contentHeight = contentHeight(box);
        int relativeY = mouse.y() - box.y() - 1;
        int relativeX = mouse.x() - box.x() - 1;
        int index = indexAt(relativeX, relativeY, panelListWidth(box), contentHeight);
        if (index >= 0 && index < records.size()) {
            moveTo(index);
        }
        return true;
    }

    private void openSelected() {
        if (records.isEmpty()) {
            return;
        }
        FileRecord selected = records.get(selectedIndex);
        if (selected.directory()) {
            openDirectory(selected.path());
        }
    }

    private void openParent() {
        Path parent = directory.getParent();
        if (parent != null) {
            openDirectory(parent);
        }
    }

    private void openDirectory(Path target) {
        if (target == null || !Files.isDirectory(target)) {
            return;
        }
        directory = target.toAbsolutePath().normalize();
        selectedIndex = 0;
        topIndex = 0;
        quickSearch = "";
        reload();
    }

    private void reload() {
        try {
            model = fileSystem.open(directory);
            directory = model.directory();
            message = "";
            applyOrdering();
        } catch (IOException e) {
            records = List.of();
            selectedIndex = 0;
            topIndex = 0;
            message = "Cannot read " + directory + ": " + e.getMessage();
        }
    }

    private void applyOrdering() {
        if (model == null) {
            return;
        }
        records = model.records().stream()
                .filter(record -> showHiddenAndSystem || record.parentEntry() || (!record.hidden() && !record.system()))
                .sorted(comparator())
                .toList();
        selectedPaths.retainAll(records.stream().map(FileRecord::path).collect(java.util.stream.Collectors.toSet()));
        clampSelection();
    }

    private Comparator<FileRecord> comparator() {
        Comparator<FileRecord> parentFirst = Comparator.comparing(FileRecord::parentEntry).reversed();
        Comparator<FileRecord> dirsFirst = Comparator.comparing(FileRecord::directory).reversed();
        Comparator<FileRecord> byName = Comparator.comparing(record -> record.name().toLowerCase(Locale.ROOT));
        Comparator<FileRecord> byType = Comparator
                .comparing((FileRecord record) -> record.directory() ? 0 : record.extension().isBlank() ? 99 : 1)
                .thenComparing(record -> record.extension().toLowerCase(Locale.ROOT));
        Comparator<FileRecord> body = switch (sortMode) {
            case NAME -> byName;
            case EXTENSION -> Comparator
                    .comparing((FileRecord record) -> record.extension().toLowerCase(Locale.ROOT))
                    .thenComparing(byName);
            case SIZE -> Comparator
                    .comparingLong(FileRecord::size)
                    .thenComparing(byName);
            case MODIFIED_TIME -> Comparator
                    .comparing((FileRecord record) -> safeTime(record.modifiedTime()))
                    .thenComparing(byName);
            case CREATION_TIME -> Comparator
                    .comparing((FileRecord record) -> safeTime(record.creationTime()))
                    .thenComparing(byName);
            case ACCESS_TIME -> Comparator
                    .comparing((FileRecord record) -> safeTime(record.accessTime()))
                    .thenComparing(byName);
            case DESCRIPTION -> byName;
            case UNSORTED -> Comparator.comparingInt(record -> model.records().indexOf(record));
        };
        if (reverseSort) {
            body = body.reversed();
        }
        Comparator<FileRecord> result = parentFirst;
        if (sortFlags.contains(PanelSortFlag.SORT_BY_TYPE)) {
            result = result.thenComparing(byType);
        } else {
            result = result.thenComparing(dirsFirst);
        }
        if (sortFlags.contains(PanelSortFlag.DIRS_BY_NAME)) {
            result = result.thenComparing((left, right) -> {
                if ((left.directory() || right.directory()) && left.directory() == right.directory()) {
                    return byName.compare(left, right);
                }
                return 0;
            });
        }
        return result.thenComparing(body);
    }

    private void moveSelection(int delta) {
        moveTo(selectedIndex + delta);
    }

    private void moveByColumn(int direction) {
        if (viewMode != PanelViewMode.BRIEF) {
            moveSelection(direction);
            return;
        }
        int bodyRows = Math.max(1, visibleRows - 1);
        moveTo(selectedIndex + direction * bodyRows);
    }

    private void moveTo(int index) {
        if (records.isEmpty()) {
            selectedIndex = 0;
            topIndex = 0;
            return;
        }
        selectedIndex = Math.max(0, Math.min(index, records.size() - 1));
        keepSelectionVisible();
    }

    private void keepSelectionVisible() {
        if (selectedIndex < topIndex) {
            topIndex = selectedIndex;
        }
        int bodyRows = Math.max(1, visibleRows - 1);
        int pageItems = Math.max(1, bodyRows * visibleColumns);
        if (selectedIndex >= topIndex + pageItems) {
            topIndex += bodyRows;
            while (selectedIndex >= topIndex + pageItems) {
                topIndex += bodyRows;
            }
        }
        if (viewMode == PanelViewMode.BRIEF && bodyRows > 0) {
            topIndex = Math.max(0, topIndex - Math.floorMod(topIndex, bodyRows));
        }
    }

    private void clampSelection() {
        if (records.isEmpty()) {
            selectedIndex = 0;
            topIndex = 0;
            return;
        }
        selectedIndex = Math.max(0, Math.min(selectedIndex, records.size() - 1));
        topIndex = Math.max(0, Math.min(topIndex, selectedIndex));
    }

    private void renderHeader(TerminalDriver terminal, Box box, ColorPalette colors) {
        int width = Math.max(0, box.width() - 2);
        if (width <= 0 || contentHeight(box) <= 0) {
            return;
        }
        String text = viewMode == PanelViewMode.BRIEF
                ? repeatColumnTitle(panelListWidth(box))
                : " Name" + " ".repeat(Math.max(1, width - 38)) + "Size       Date       Time Attr";
        writeLine(terminal, box.x() + 1, box.y() + 1, width, text, colors.panel().background(), colors.hotkey().foreground());
        terminal.putString(box.x() + 1, box.y(), sortIndicator(), colors.panelTitle().foreground(), colors.panel().background());
    }

    private void renderContentRow(TerminalDriver terminal, Box box, int row, boolean active, ColorPalette colors) {
        int contentX = box.x() + 1;
        int contentY = box.y() + 1;
        int contentWidth = panelListWidth(box);
        if (viewMode == PanelViewMode.BRIEF) {
            renderBriefContentRow(terminal, contentX, contentY + row, contentWidth, row, active, colors);
            return;
        }
        int itemIndex = topIndex + row - 1;
        if (itemIndex >= records.size()) {
            writeLine(terminal, contentX, contentY + row, contentWidth, "", colors.panel().background(), colors.panel().foreground());
            return;
        }

        FileRecord record = records.get(itemIndex);
        boolean cursor = active && itemIndex == selectedIndex;
        boolean marked = selectedPaths.contains(record.path());
        Color background = cursor ? colors.selected().background() : colors.panel().background();
        Color foreground = cursor
                ? colors.selected().foreground()
                : marked ? colors.hotkey().foreground()
                : record.directory() ? Color.WHITE_BRIGHT
                : Color.CYAN_BRIGHT;
        writeLine(terminal, contentX, contentY + row, contentWidth, formatItem(record, contentWidth), background, foreground);
    }

    private void renderBriefContentRow(
            TerminalDriver terminal,
            int x,
            int y,
            int width,
            int row,
            boolean active,
            ColorPalette colors
    ) {
        writeLine(terminal, x, y, width, "", colors.panel().background(), colors.panel().foreground());
        int bodyRows = Math.max(1, visibleRows - 1);
        int columns = briefColumns(width);
        int cellWidth = briefCellWidth(width);
        for (int column = 0; column < columns - 1; column++) {
            if (!hasVisibleColumn(column + 1, bodyRows)) {
                continue;
            }
            int separatorX = x + (column + 1) * cellWidth - 1;
            if (separatorX < x + width) {
                terminal.putChar(separatorX, y, '\u2502', colors.inactiveBorder().foreground(), colors.panel().background());
            }
        }
        for (int column = 0; column < columns; column++) {
            int itemIndex = topIndex + column * bodyRows + row - 1;
            if (itemIndex >= records.size()) {
                continue;
            }
            FileRecord record = records.get(itemIndex);
            boolean cursor = active && itemIndex == selectedIndex;
            boolean marked = selectedPaths.contains(record.path());
            Color background = cursor ? colors.selected().background() : colors.panel().background();
            Color foreground = cursor
                    ? colors.selected().foreground()
                    : marked ? colors.hotkey().foreground()
                    : record.directory() ? Color.WHITE_BRIGHT
                    : Color.CYAN_BRIGHT;
            int remaining = Math.max(0, width - column * cellWidth);
            writeLine(
                    terminal,
                    x + column * cellWidth,
                    y,
                    Math.min(cellWidth - 1, remaining),
                    formatItem(record, width),
                    background,
                    foreground
            );
        }
    }

    private void renderFooter(TerminalDriver terminal, Box box, ColorPalette colors) {
        int width = Math.max(0, box.width() - 2);
        if (width <= 0 || box.height() < FOOTER_HEIGHT + 2) {
            return;
        }
        int dividerY = box.y() + box.height() - FOOTER_HEIGHT - 1;
        String divider = footerDivider(width);
        writeLine(terminal, box.x() + 1, dividerY, width, divider, colors.panel().background(), colors.inactiveBorder().foreground());

        String current;
        if (!message.isBlank()) {
            current = message;
        } else if (records.isEmpty()) {
            current = " empty ";
        } else {
            FileRecord record = records.get(selectedIndex);
            current = " " + trim(record.name(), Math.max(1, width / 3))
                    + "  " + describeSize(record)
                    + "  " + formatDate(record.modifiedTime())
                    + "  " + formatTime(record.modifiedTime())
                    + "  " + record.attrs();
        }
        writeLine(terminal, box.x() + 1, dividerY + 1, width, fit(current, width), colors.panel().background(), colors.hotkey().foreground());

        String totals = "Total: " + (records.size() - parentEntryCount()) + " files  "
                + numberFormat.format(totalSize()) + " bytes";
        writeLine(terminal, box.x() + 1, dividerY + 2, width, centered(totals, width), colors.panel().background(), colors.hotkey().foreground());

        String selected = selectedCount() == 0
                ? "No files selected"
                : numberFormat.format(selectedSize()) + " bytes in " + selectedCount() + " selected";
        String bottom = centered(selected, Math.max(0, width / 2))
                + trim("  Free " + freeSpace(), Math.max(0, width - width / 2));
        writeLine(terminal, box.x() + 1, dividerY + 3, width, bottom, colors.panel().background(), colors.hotkey().foreground());

        String driveLine = driveLine(width);
        int driveX = box.x() + 2;
        int maxDriveWidth = Math.max(0, box.width() - 4);
        terminal.putString(driveX, box.y() + box.height() - 1, trim(driveLine, maxDriveWidth),
                colors.hotkey().foreground(), colors.panel().background());
    }

    private String formatItem(FileRecord record, int width) {
        if (viewMode == PanelViewMode.BRIEF) {
            return trim(record.displayName(longNames), Math.max(1, briefCellWidth(width) - 1));
        }
        String size = record.directory() ? "<DIR>" : numberFormat.format(record.size());
        String date = formatDate(record.modifiedTime());
        String time = formatTime(record.modifiedTime());
        int fixedWidth = 34;
        int nameWidth = Math.max(1, width - fixedWidth);
        return trim(mark(record) + record.displayName(longNames), nameWidth)
                + " "
                + leftPad(size, 10)
                + " "
                + leftPad(date, 10)
                + " "
                + leftPad(time, 5)
                + " "
                + record.attrs();
    }

    private int indexAt(int relativeX, int relativeY, int contentWidth, int contentHeight) {
        if (relativeY < 1 || relativeY >= contentHeight || relativeX < 0 || contentWidth <= 0) {
            return -1;
        }
        int bodyRows = Math.max(1, contentHeight - 1);
        if (viewMode == PanelViewMode.FULL) {
            return topIndex + relativeY - 1;
        }
        int cellWidth = briefCellWidth(contentWidth);
        int columns = briefColumns(contentWidth);
        int column = Math.min(columns - 1, relativeX / cellWidth);
        return topIndex + column * bodyRows + relativeY - 1;
    }

    private int briefColumns(int width) {
        return Math.max(1, (Math.max(1, width) + 1) / BRIEF_LINE_LENGTH);
    }

    private int briefCellWidth(int width) {
        return BRIEF_LINE_LENGTH;
    }

    private String sortModeLabel() {
        return switch (sortMode) {
            case NAME -> "name";
            case EXTENSION -> "ext";
            case SIZE -> "size";
            case MODIFIED_TIME -> "date";
            case CREATION_TIME -> "create";
            case ACCESS_TIME -> "access";
            case DESCRIPTION -> "diz";
            case UNSORTED -> "none";
        };
    }

    private void renderScrollbar(TerminalDriver terminal, Box box, ColorPalette colors) {
        int trackX = box.x() + box.width() - 1;
        int top = box.y() + 1;
        int bottom = box.y() + box.height() - FOOTER_HEIGHT - 2;
        if (box.width() < 4 || bottom < top) {
            return;
        }
        for (int y = top; y <= bottom; y++) {
            terminal.putChar(trackX, y, '\u2591', colors.activeBorder().foreground(), colors.panel().background());
        }
        int trackHeight = bottom - top + 1;
        int thumbTop = records.size() <= 1
                ? top
                : top + (trackHeight - 1) * selectedIndex / (records.size() - 1);
        terminal.putChar(trackX, thumbTop, '\u2588', colors.activeBorder().foreground(), colors.panel().background());
    }

    private int panelListWidth(Box box) {
        return Math.max(0, box.width() - 3);
    }

    private String repeatColumnTitle(int width) {
        int cellWidth = briefCellWidth(width);
        int columns = briefColumns(width);
        StringBuilder builder = new StringBuilder(width);
        for (int column = 0; column < columns; column++) {
            if (!hasVisibleColumn(column, Math.max(1, visibleRows - 1))) {
                break;
            }
            String title = centered("Name", Math.max(1, cellWidth - 1));
            builder.append(title);
            if (column < columns - 1 && hasVisibleColumn(column + 1, Math.max(1, visibleRows - 1))) {
                builder.append('\u2502');
            }
        }
        return trim(builder.toString(), width);
    }

    private String footerDivider(int width) {
        if (width <= 0) {
            return "";
        }
        StringBuilder builder = new StringBuilder("\u2500".repeat(width));
        if (viewMode == PanelViewMode.BRIEF) {
            int cellWidth = briefCellWidth(width);
            for (int x = cellWidth - 1; x < width; x += cellWidth) {
                int column = (x + 1) / cellWidth;
                if (!hasVisibleColumn(column, Math.max(1, visibleRows - 1))) {
                    continue;
                }
                if (x >= 0 && x < builder.length()) {
                    builder.setCharAt(x, '\u2534');
                }
            }
        }
        return builder.toString();
    }

    private long totalSize() {
        return records.stream()
                .filter(record -> !record.parentEntry())
                .filter(record -> !record.directory())
                .mapToLong(FileRecord::size)
                .filter(size -> size > 0)
                .sum();
    }

    private long parentEntryCount() {
        return records.stream().filter(FileRecord::parentEntry).count();
    }

    private String driveLine(int width) {
        List<String> roots = new ArrayList<>();
        for (Path root : FileSystems.getDefault().getRootDirectories()) {
            String label = root.toString();
            roots.add(label.length() >= 1 ? label.substring(0, 1).toUpperCase(Locale.ROOT) : label);
        }
        roots.add("~");
        String line = "[ " + String.join(" ", roots) + " ]";
        return trim(line, width);
    }

    private boolean hasVisibleColumn(int column, int bodyRows) {
        return topIndex + column * bodyRows < records.size();
    }

    private String sortIndicator() {
        String tag = switch (sortMode) {
            case NAME -> "n";
            case EXTENSION -> "e";
            case SIZE -> "s";
            case MODIFIED_TIME -> "d";
            case CREATION_TIME -> "c";
            case ACCESS_TIME -> "a";
            case DESCRIPTION -> "z";
            case UNSORTED -> "u";
        };
        return sortFlags.contains(PanelSortFlag.INVERTED) ? tag.toUpperCase(Locale.ROOT) : tag;
    }

    private void openSortPopup() {
        sortPopupOpen = true;
        for (int i = 0; i < SORT_POPUP_ITEMS.size(); i++) {
            SortPopupItem item = SORT_POPUP_ITEMS.get(i);
            if (item.mode() == sortMode) {
                sortPopupIndex = i;
                break;
            }
        }
    }

    private void handleSortPopupKey(KeyStroke key) {
        switch (key.keyType()) {
            case Escape -> sortPopupOpen = false;
            case ArrowUp -> sortPopupIndex = Math.floorMod(sortPopupIndex - 1, SORT_POPUP_ITEMS.size());
            case ArrowDown -> sortPopupIndex = Math.floorMod(sortPopupIndex + 1, SORT_POPUP_ITEMS.size());
            case Enter -> activateSortPopupItem();
            case Character -> handleSortPopupCharacter(key.character());
            case Mouse -> {
            }
            default -> {
            }
        }
    }

    private void handleSortPopupCharacter(Character character) {
        if (character == null) {
            return;
        }
        char ch = Character.toLowerCase(character);
        for (int i = 0; i < SORT_POPUP_ITEMS.size(); i++) {
            SortPopupItem item = SORT_POPUP_ITEMS.get(i);
            if (Character.toLowerCase(item.label().charAt(0)) == ch) {
                sortPopupIndex = i;
                activateSortPopupItem();
                return;
            }
        }
    }

    private void handleSortPopupMouse(MouseEvent mouse, Box panelBox) {
        int popupX = Math.max(panelBox.x() + 1, Math.min(panelBox.x() + panelBox.width() - 24, panelBox.x() + 2));
        int popupY = panelBox.y() + 1;
        int row = mouse.y() - popupY - 1;
        if (mouse.x() < popupX || mouse.x() >= popupX + 22 || row < 0 || row >= SORT_POPUP_ITEMS.size()) {
            sortPopupOpen = false;
            return;
        }
        sortPopupIndex = row;
        activateSortPopupItem();
    }

    private void activateSortPopupItem() {
        SortPopupItem item = SORT_POPUP_ITEMS.get(sortPopupIndex);
        if (item.mode() != null) {
            setSortMode(item.mode());
            sortPopupOpen = false;
            return;
        }
        if (item.flag() != null) {
            if (!sortFlags.remove(item.flag())) {
                sortFlags.add(item.flag());
            }
            reverseSort = sortFlags.contains(PanelSortFlag.INVERTED);
            applyOrdering();
        }
    }

    private void renderSortPopup(TerminalDriver terminal, Box panelBox, ColorPalette colors) {
        int width = 22;
        int height = SORT_POPUP_ITEMS.size() + 2;
        int x = Math.max(panelBox.x() + 1, Math.min(panelBox.x() + panelBox.width() - width - 1, panelBox.x() + 2));
        int y = panelBox.y() + 1;
        Box popup = new Box(x, y, width, height);
        popup.draw(terminal, colors.activeBorder().foreground(), colors.panel().background());
        for (int i = 0; i < SORT_POPUP_ITEMS.size(); i++) {
            SortPopupItem item = SORT_POPUP_ITEMS.get(i);
            boolean current = i == sortPopupIndex;
            boolean checked = item.mode() == sortMode || (item.flag() != null && sortFlags.contains(item.flag()));
            String text = (checked ? "*" : " ") + " " + item.label();
            Color background = current ? colors.selected().background() : colors.panel().background();
            Color foreground = current ? colors.selected().foreground() : colors.panel().foreground();
            writeLine(terminal, x + 1, y + 1 + i, width - 2, text, background, foreground);
        }
    }

    private FileRecord currentRecord() {
        if (records.isEmpty() || selectedIndex >= records.size()) {
            return null;
        }
        return records.get(selectedIndex);
    }

    private void setRecordSelected(FileRecord record, boolean selected) {
        if (selected) {
            selectedPaths.add(record.path());
        } else {
            selectedPaths.remove(record.path());
        }
    }

    private static String baseName(String name) {
        int dot = name.lastIndexOf('.');
        return dot <= 0 ? name : name.substring(0, dot);
    }

    private record SortPopupItem(String label, PanelSortMode mode, PanelSortFlag flag) {
    }

    private String mark(FileRecord record) {
        return selectedPaths.contains(record.path()) ? "*" : " ";
    }

    private String describeSize(FileRecord record) {
        return record.directory() ? "<DIR>" : numberFormat.format(record.size()) + " bytes";
    }

    private String freeSpace() {
        if (model == null || model.drive() == null || model.drive().freeSpace() < 0) {
            return "?";
        }
        return numberFormat.format(model.drive().freeSpace());
    }

    private static Pattern maskPattern(String mask) {
        String pattern = mask == null || mask.isBlank() ? "*" : mask;
        StringBuilder regex = new StringBuilder("(?i)^");
        for (char ch : pattern.toCharArray()) {
            switch (ch) {
                case '*' -> regex.append(".*");
                case '?' -> regex.append('.');
                case '.', '(', ')', '[', ']', '{', '}', '+', '$', '^', '|', '\\' -> regex.append('\\').append(ch);
                default -> regex.append(ch);
            }
        }
        regex.append('$');
        return Pattern.compile(regex.toString());
    }

    private static Instant safeTime(FileTime time) {
        return time == null ? Instant.EPOCH : time.toInstant();
    }

    private static String formatDate(FileTime time) {
        if (time == null) {
            return "";
        }
        return LocalDateTime.ofInstant(time.toInstant(), ZoneId.systemDefault()).format(DATE_FORMAT);
    }

    private static String formatTime(FileTime time) {
        if (time == null) {
            return "";
        }
        return LocalDateTime.ofInstant(time.toInstant(), ZoneId.systemDefault()).format(TIME_FORMAT);
    }

    private static int contentHeight(Box box) {
        return Math.max(0, box.height() - FOOTER_HEIGHT - 2);
    }

    private static void writeLine(
            TerminalDriver terminal,
            int x,
            int y,
            int width,
            String text,
            Color background,
            Color foreground
    ) {
        terminal.putString(x, y, fit(text, width), foreground, background);
    }

    private static String fit(String text, int width) {
        if (width <= 0) {
            return "";
        }
        String trimmed = trim(text, width);
        return trimmed + " ".repeat(width - trimmed.length());
    }

    private static String trim(String text, int width) {
        if (width <= 0) {
            return "";
        }
        if (text.length() <= width) {
            return text;
        }
        if (width == 1) {
            return text.substring(0, 1);
        }
        return text.substring(0, width - 1) + "~";
    }

    private static String leftPad(String text, int width) {
        if (text.length() >= width) {
            return text.substring(0, width);
        }
        return " ".repeat(width - text.length()) + text;
    }

    private static String centered(String text, int width) {
        String trimmed = trim(text, width);
        int left = Math.max(0, (width - trimmed.length()) / 2);
        return " ".repeat(left) + trimmed;
    }
}
