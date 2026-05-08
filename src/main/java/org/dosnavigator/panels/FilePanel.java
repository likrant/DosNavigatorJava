package org.dosnavigator.panels;

import org.dosnavigator.fs.FileSystemService;
import org.dosnavigator.terminal.Color;
import org.dosnavigator.terminal.KeyStroke;
import org.dosnavigator.terminal.TerminalDriver;
import org.dosnavigator.ui.Box;
import org.dosnavigator.ui.ColorPalette;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class FilePanel {
    private final FileSystemService fileSystem;
    private final NumberFormat numberFormat = NumberFormat.getIntegerInstance(Locale.US);
    private Path directory;
    private List<FileItem> items = new ArrayList<>();
    private int selectedIndex;
    private int topIndex;
    private int visibleRows = 20;
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

        int contentX = box.x() + 1;
        int contentY = box.y() + 1;
        int contentWidth = Math.max(0, box.width() - 2);
        int contentHeight = Math.max(0, box.height() - 2);
        visibleRows = Math.max(1, contentHeight);
        keepSelectionVisible();

        for (int row = 0; row < contentHeight; row++) {
            int itemIndex = topIndex + row;
            if (itemIndex >= items.size()) {
                writeLine(terminal, contentX, contentY + row, contentWidth, "", colors.panel().background(), colors.panel().foreground());
                continue;
            }

            FileItem item = items.get(itemIndex);
            boolean selected = active && itemIndex == selectedIndex;
            Color background = selected ? colors.selected().background() : colors.panel().background();
            Color foreground = selected ? colors.selected().foreground() : item.directory() ? Color.YELLOW_BRIGHT : colors.panel().foreground();
            writeLine(terminal, contentX, contentY + row, contentWidth, formatItem(item, contentWidth), background, foreground);
        }
    }

    public void handleKey(KeyStroke key) {
        switch (key.getKeyType()) {
            case ArrowUp -> moveSelection(-1);
            case ArrowDown -> moveSelection(1);
            case PageUp -> moveSelection(-10);
            case PageDown -> moveSelection(10);
            case Home -> moveTo(0);
            case End -> moveTo(items.size() - 1);
            case Enter -> openSelected();
            case Backspace -> openDirectory(directory.getParent());
            default -> {
            }
        }
    }

    public String statusText() {
        if (!message.isBlank()) {
            return message;
        }
        if (items.isEmpty()) {
            return directory + " | empty";
        }
        FileItem item = items.get(selectedIndex);
        String size = item.directory() ? "<DIR>" : numberFormat.format(item.size()) + " bytes";
        return directory + " | " + item.name() + " | " + size;
    }

    private void openSelected() {
        if (items.isEmpty()) {
            return;
        }
        FileItem selected = items.get(selectedIndex);
        if (selected.directory()) {
            openDirectory(selected.path());
        }
    }

    private void openDirectory(Path target) {
        if (target == null || !Files.isDirectory(target)) {
            return;
        }
        directory = target.toAbsolutePath().normalize();
        selectedIndex = 0;
        topIndex = 0;
        reload();
    }

    private void reload() {
        try {
            items = fileSystem.list(directory);
            message = "";
            clampSelection();
        } catch (IOException e) {
            items = List.of();
            selectedIndex = 0;
            topIndex = 0;
            message = "Cannot read " + directory + ": " + e.getMessage();
        }
    }

    private void moveSelection(int delta) {
        moveTo(selectedIndex + delta);
    }

    private void moveTo(int index) {
        if (items.isEmpty()) {
            selectedIndex = 0;
            topIndex = 0;
            return;
        }
        selectedIndex = Math.max(0, Math.min(index, items.size() - 1));
        keepSelectionVisible();
    }

    private void keepSelectionVisible() {
        if (selectedIndex < topIndex) {
            topIndex = selectedIndex;
        }
        if (selectedIndex >= topIndex + visibleRows) {
            topIndex = selectedIndex - visibleRows + 1;
        }
    }

    private void clampSelection() {
        if (items.isEmpty()) {
            selectedIndex = 0;
            topIndex = 0;
            return;
        }
        selectedIndex = Math.max(0, Math.min(selectedIndex, items.size() - 1));
        topIndex = Math.max(0, Math.min(topIndex, selectedIndex));
    }

    private String formatItem(FileItem item, int width) {
        String size = item.directory() ? "<DIR>" : numberFormat.format(item.size());
        int sizeWidth = Math.min(12, Math.max(5, width / 3));
        int nameWidth = Math.max(1, width - sizeWidth - 1);
        return trim(item.displayName(), nameWidth) + " " + leftPad(size, sizeWidth);
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
}
