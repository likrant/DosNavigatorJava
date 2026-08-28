package org.dosnavigator.foundation;

import org.dosnavigator.command.CommandBus;
import org.dosnavigator.command.CommandId;
import org.dosnavigator.terminal.Color;
import org.dosnavigator.terminal.KeyStroke;
import org.dosnavigator.terminal.KeyType;
import org.dosnavigator.terminal.TerminalSize;
import org.dosnavigator.terminal.TerminalSurface;
import org.dosnavigator.ui.Box;
import org.dosnavigator.ui.ColorPair;
import org.dosnavigator.ui.ColorPalette;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Source-traceable foundation for views.pas, dialogs.pas, menus.pas, DN1.PAS,
 * and DNAPP.PAS. It intentionally contains no panel or filesystem behavior.
 */
public final class FoundationApp {
    private final TerminalSurface terminal;
    private final ColorPalette palette = ColorPalette.dosNavigator();
    private final CommandBus commands = new CommandBus();
    private final LegacyLanguage language;
    private final List<LegacyMenuItem> topMenus;
    private final List<Integer> menuPath = new ArrayList<>();
    private final List<String> zOrder = new ArrayList<>(List.of("desktop"));
    private boolean menuOpen;
    private DialogState modal;
    private CommandId lastCommand;

    public FoundationApp(TerminalSurface terminal, LegacyLanguage language) {
        this.terminal = terminal;
        this.language = language;
        this.topMenus = LegacyMenuResources.mainMenu(language);
        commands.register(CommandId.MENU, ignored -> toggleMenu());
        commands.register(CommandId.CANCEL, ignored -> cancel());
        commands.register(CommandId.OK, ignored -> accept());
        commands.register(CommandId.SYSTEM_SETUP, ignored -> openDialog(CommandId.SYSTEM_SETUP));
        commands.register(CommandId.INTERFACE_SETUP, ignored -> openDialog(CommandId.INTERFACE_SETUP));
        commands.register(CommandId.ABOUT, ignored -> openDialog(CommandId.ABOUT));
        commands.register(CommandId.REFRESH, ignored -> true);
    }

    public void dispatchKey(KeyStroke key) {
        if (key == null) {
            return;
        }
        if (key.keyType() == KeyType.F10) {
            dispatch(CommandId.MENU);
            return;
        }
        if (key.keyType() == KeyType.Escape) {
            dispatch(CommandId.CANCEL);
            return;
        }
        if (key.keyType() == KeyType.Enter) {
            dispatch(CommandId.OK);
            return;
        }
        if (modal != null) {
            return;
        }
        if (!menuOpen) {
            return;
        }
        if (key.keyType() == KeyType.Character && key.character() != null) {
            selectMnemonic(key.character());
            return;
        }
        if (key.keyType() == KeyType.ArrowRight) {
            selectTop(1);
        } else if (key.keyType() == KeyType.ArrowLeft) {
            selectTop(-1);
        }
    }

    public void renderFrame() {
        TerminalSize size = terminal.size();
        terminal.beginFrame();
        fill(0, 0, size.columns(), size.rows(), palette.desktop());
        drawMenuBar(size.columns());
        drawStartup(size);
        if (menuOpen) {
            drawMenu(size);
        }
        if (modal != null) {
            drawDialog(size);
        }
        drawStatusLine(size);
        terminal.setActiveView(activeView());
        terminal.setCursor(0, 0, false);
        terminal.refresh();
    }

    public CommandId lastCommand() {
        return lastCommand;
    }

    public boolean menuOpen() {
        return menuOpen;
    }

    public boolean modalOpen() {
        return modal != null;
    }

    public List<String> zOrder() {
        return List.copyOf(zOrder);
    }

    public String activeView() {
        if (modal != null) {
            return "dialog:" + modal.id;
        }
        if (menuOpen) {
            return "menu:" + current().text();
        }
        return "desktop";
    }

    public void run() throws IOException {
        terminal.start();
        while (true) {
            renderFrame();
            dispatchKey(terminal.readKey());
        }
    }

    private void dispatch(CommandId command) {
        lastCommand = command;
        commands.dispatch(command);
    }

    private boolean toggleMenu() {
        if (modal != null) {
            return false;
        }
        menuOpen = !menuOpen;
        menuPath.clear();
        if (menuOpen) {
            menuPath.add(0);
        }
        return true;
    }

    private boolean cancel() {
        if (modal != null) {
            closeDialog();
            return true;
        }
        if (!menuOpen) {
            return false;
        }
        if (menuPath.size() > 1) {
            menuPath.removeLast();
        } else {
            menuOpen = false;
            menuPath.clear();
        }
        return true;
    }

    private boolean accept() {
        if (modal != null) {
            closeDialog();
            return true;
        }
        if (!menuOpen) {
            return false;
        }
        activate(current());
        return true;
    }

    private boolean openDialog(CommandId command) {
        menuOpen = false;
        menuPath.clear();
        modal = DialogState.forCommand(command, language);
        zOrder.add(modal.id);
        return true;
    }

    private void closeDialog() {
        zOrder.removeLast();
        modal = null;
    }

    private void selectMnemonic(char value) {
        char mnemonic = Character.toLowerCase(value);
        for (int index = 0; index < topMenus.size(); index++) {
            if (topMenus.get(index).mnemonic() == mnemonic) {
                menuPath.clear();
                menuPath.add(index);
                return;
            }
        }
        LegacyMenuItem current = current();
        for (int index = 0; index < current.children().size(); index++) {
            LegacyMenuItem child = current.children().get(index);
            if (child.mnemonic() == mnemonic) {
                menuPath.add(index);
                if (!child.hasChildren() && child.command() != null) {
                    dispatch(child.command());
                }
                return;
            }
        }
    }

    private void selectTop(int delta) {
        int next = Math.floorMod(menuPath.getFirst() + delta, topMenus.size());
        menuPath.clear();
        menuPath.add(next);
    }

    private void activate(LegacyMenuItem item) {
        if (item.hasChildren()) {
            menuPath.add(0);
        } else if (item.command() != null) {
            dispatch(item.command());
        }
    }

    private LegacyMenuItem current() {
        LegacyMenuItem current = topMenus.get(menuPath.getFirst());
        for (int index = 1; index < menuPath.size(); index++) {
            current = current.children().get(menuPath.get(index));
        }
        return current;
    }

    private void drawStartup(TerminalSize size) {
        String text = language == LegacyLanguage.RUSSIAN ? "Dos Navigator - загрузка" : "Dos Navigator - startup";
        put(2, Math.max(1, size.rows() / 2), text, palette.panelTitle());
    }

    private void drawMenuBar(int width) {
        fill(0, 0, width, 1, palette.menu());
        int x = 2;
        for (int index = 0; index < topMenus.size(); index++) {
            LegacyMenuItem item = topMenus.get(index);
            ColorPair color = menuOpen && index == menuPath.getFirst() ? palette.menuSelected() : palette.menu();
            putMnemonic(x + 1, 0, item, color);
            x += item.text().length() + 2;
        }
    }

    private void drawMenu(TerminalSize size) {
        LegacyMenuItem parent = current();
        if (!parent.hasChildren()) {
            return;
        }
        int x = menuPath.size() == 1 ? 2 : 24;
        int y = 1;
        int width = Math.min(34, Math.max(18, parent.children().stream().mapToInt(item -> item.text().length()).max().orElse(10) + 6));
        Box bounds = new Box(x, y, Math.min(width, size.columns() - x), parent.children().size() + 2);
        bounds.draw(terminal, palette.activeBorder().foreground(), palette.activeBorder().background());
        for (int index = 0; index < parent.children().size(); index++) {
            LegacyMenuItem item = parent.children().get(index);
            ColorPair color = palette.menu();
            if (menuPath.size() > 1 && menuPath.getLast() == index) {
                color = palette.menuSelected();
            }
            putMnemonic(x + 2, y + 1 + index, item, color);
            put(x + 2 + item.text().length(), y + 1 + index,
                    pad(item.hasChildren() ? " >" : "", bounds.width() - 4 - item.text().length()), color);
        }
    }

    private void drawDialog(TerminalSize size) {
        int width = Math.min(56, size.columns() - 8);
        int height = 9;
        int x = Math.max(0, (size.columns() - width) / 2);
        int y = Math.max(1, (size.rows() - height) / 2);
        Box bounds = new Box(x, y, width, height);
        fill(x + 1, y + 1, width - 2, height - 2, palette.dialog());
        bounds.draw(terminal, palette.activeBorder().foreground(), palette.dialog().background());
        put(x + 2, y, " " + modal.title + " ", palette.panelTitle());
        put(x + 3, y + 3, modal.message, palette.dialog());
        put(x + 3, y + 6, "[Enter] OK   [Esc] Cancel", palette.hotkey());
    }

    private void drawStatusLine(TerminalSize size) {
        ColorPair status = palette.status();
        fill(0, size.rows() - 1, size.columns(), 1, status);
        put(1, size.rows() - 1, "F10", palette.hotkey());
        put(5, size.rows() - 1, "Menu   Enter Select   Esc Cancel", status);
    }

    private void fill(int x, int y, int width, int height, ColorPair color) {
        String line = " ".repeat(Math.max(0, width));
        for (int row = 0; row < height; row++) {
            terminal.putString(x, y + row, line, color.foreground(), color.background());
        }
    }

    private void put(int x, int y, String text, ColorPair color) {
        terminal.putString(x, y, text, color.foreground(), color.background());
    }

    private void putMnemonic(int x, int y, LegacyMenuItem item, ColorPair color) {
        put(x, y, item.text(), color);
        int index = item.text().toLowerCase().indexOf(Character.toLowerCase(item.mnemonic()));
        if (index >= 0) {
            terminal.putChar(x + index, y, item.text().charAt(index), palette.hotkey().foreground(), color.background());
        }
    }

    private static String pad(String value, int width) {
        return value.length() >= width ? value.substring(0, width) : value + " ".repeat(width - value.length());
    }

    private record DialogState(String id, String title, String message) {
        private static DialogState forCommand(CommandId command, LegacyLanguage language) {
            boolean russian = language == LegacyLanguage.RUSSIAN;
            return switch (command) {
                case SYSTEM_SETUP -> new DialogState("dlgSystemSetup", russian ? "Системные установки" : "System setup", russian ? "Системные параметры" : "System parameters");
                case INTERFACE_SETUP -> new DialogState("dlgInterfaceSetup", russian ? "Установки интерфейса" : "Interface setup", russian ? "Параметры интерфейса" : "Interface parameters");
                case ABOUT -> new DialogState("dlgAbout", russian ? "О программе" : "About", "Dos Navigator");
                default -> throw new IllegalArgumentException("No dialog for " + command);
            };
        }
    }
}
