package org.dosnavigator.viewer;

import org.dosnavigator.terminal.Color;
import org.dosnavigator.terminal.TerminalSurface;
import org.dosnavigator.ui.ColorPalette;

/** Cell renderer for the initial FVIEWER.PAS text-mode view/status contract. */
public final class ViewerFrame {
    private final ColorPalette palette = ColorPalette.dosNavigator();

    public void render(TerminalSurface terminal, ViewerDocument document, String title, int firstLine) {
        int width = terminal.size().columns();
        int height = terminal.size().rows();
        terminal.beginFrame();
        terminal.clear(palette.panel().foreground(), palette.panel().background());
        terminal.putString(0, 0, " " + title + " ", palette.panelTitle().foreground(), palette.panelTitle().background());
        for (int row = 1; row < height - 1; row++) {
            int line = firstLine + row - 1;
            if (line < document.lines().size()) {
                terminal.putString(0, row, crop(document.lines().get(line), width), palette.panel().foreground(), palette.panel().background());
            }
        }
        terminal.putString(0, height - 1, crop("F7 Search  Esc Exit  " + (firstLine + 1) + "/" + document.lines().size(), width),
                palette.status().foreground(), palette.status().background());
        terminal.setCursor(0, Math.min(height - 2, 1), false);
        terminal.setActiveView("viewer:text");
        terminal.refresh();
    }

    private static String crop(String value, int width) {
        return value.length() <= width ? value : value.substring(0, width);
    }
}
