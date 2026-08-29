package org.dosnavigator.port.flpanel;

import org.dosnavigator.terminal.Color;
import org.dosnavigator.terminal.TerminalSurface;
import org.dosnavigator.ui.Box;
import org.dosnavigator.ui.ColorPalette;

import java.util.List;

/**
 * Direct-port nucleus of {@code TFilePanel.Draw} from flpanel.pas.
 * Rendering consumes a legacy panel model; filesystem enumeration remains in
 * the drives/filescol adapter and is deliberately not performed during Draw.
 */
public final class TFilePanel {
    private final String directoryName;
    private final List<TFileRecord> files;
    private int cursor;

    public TFilePanel(String directoryName, List<TFileRecord> files) {
        this.directoryName = directoryName;
        this.files = List.copyOf(files);
    }

    public void setCursor(int cursor) {
        this.cursor = Math.max(0, Math.min(cursor, Math.max(0, files.size() - 1)));
    }

    /** Port of the full redraw path at flpanel.pas:673-923. */
    public void draw(TerminalSurface terminal, Box bounds, boolean focused, ColorPalette palette) {
        Color foreground = focused ? palette.activeBorder().foreground() : palette.inactiveBorder().foreground();
        bounds.draw(terminal, foreground, palette.panel().background());
        write(terminal, bounds.x() + 2, bounds.y(), fit(" " + directoryName + " ", bounds.width() - 4),
                focused ? palette.panelTitle() : palette.inactiveBorder());

        int contentWidth = Math.max(0, bounds.width() - 3);
        write(terminal, bounds.x() + 1, bounds.y() + 1, fit(center("Имя", contentWidth), contentWidth), palette.panelTitle());
        int footerTop = bounds.y() + bounds.height() - 5;
        for (int row = bounds.y() + 2; row < footerTop; row++) {
            int index = row - bounds.y() - 2;
            TFileRecord file = index < files.size() ? files.get(index) : null;
            boolean selected = focused && index == cursor;
            Color background = selected ? Color.CYAN : palette.panel().background();
            Color color = selected ? Color.BLACK : file != null && file.directory() ? Color.WHITE_BRIGHT : Color.CYAN_BRIGHT;
            writeRaw(terminal, bounds.x() + 1, row, fit(file == null ? "" : file.displayName(), contentWidth), color, background);
        }
        drawScrollBar(terminal, bounds, palette);
        drawFooter(terminal, bounds, palette);
    }

    private void drawScrollBar(TerminalSurface terminal, Box bounds, ColorPalette palette) {
        int x = bounds.x() + bounds.width() - 1;
        int first = bounds.y() + 1;
        int last = bounds.y() + bounds.height() - 6;
        for (int y = first; y <= last; y++) {
            terminal.putChar(x, y, '\u2591', palette.activeBorder().foreground(), palette.panel().background());
        }
        if (first <= last) {
            terminal.putChar(x, first, '\u2588', palette.activeBorder().foreground(), palette.panel().background());
        }
    }

    private void drawFooter(TerminalSurface terminal, Box bounds, ColorPalette palette) {
        int width = Math.max(0, bounds.width() - 2);
        int y = bounds.y() + bounds.height() - 5;
        writeRaw(terminal, bounds.x() + 1, y, "\u2500".repeat(width), palette.inactiveBorder().foreground(), palette.panel().background());
        TFileRecord current = files.isEmpty() ? null : files.get(cursor);
        write(terminal, bounds.x() + 1, y + 1, fit(current == null ? "" : current.footer(), width), palette.hotkey());
        write(terminal, bounds.x() + 1, y + 2, center("Всего: " + files.size() + " файлов", width), palette.hotkey());
        write(terminal, bounds.x() + 1, y + 3, center("287 490M байт свободно", width), palette.hotkey());
        write(terminal, bounds.x() + 2, bounds.y() + bounds.height() - 1, fit("[ C E G M ]", Math.max(0, bounds.width() - 4)), palette.hotkey());
    }

    private static void write(TerminalSurface terminal, int x, int y, String text, org.dosnavigator.ui.ColorPair color) {
        writeRaw(terminal, x, y, text, color.foreground(), color.background());
    }

    private static void writeRaw(TerminalSurface terminal, int x, int y, String text, Color foreground, Color background) {
        terminal.putString(x, y, text, foreground, background);
    }

    private static String center(String text, int width) {
        if (text.length() >= width) {
            return text.substring(0, Math.max(0, width));
        }
        return " ".repeat((width - text.length()) / 2) + text;
    }

    private static String fit(String value, int width) {
        if (width <= 0) {
            return "";
        }
        return value.length() >= width ? value.substring(0, width) : value + " ".repeat(width - value.length());
    }

    public record TFileRecord(String name, String extension, boolean directory) {
        String displayName() {
            return name + (extension.isBlank() ? "" : " " + extension);
        }

        String footer() {
            return directory ? "<Каталог>" : name + (extension.isBlank() ? "" : "." + extension);
        }
    }
}
