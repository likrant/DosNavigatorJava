package org.dosnavigator.ui;

import org.dosnavigator.terminal.Color;
import org.dosnavigator.terminal.ColorScheme;
import org.dosnavigator.terminal.TerminalDriver;

public final class StatusLine {
    public void render(TerminalDriver terminal, int row, int width, String status, ColorScheme colors) {
        terminal.putString(0, row, fit(" F1 Help  F3 View  F4 Edit  F5 Copy  F6 RenMov  F7 MkDir  F8 Delete  F10 Quit | " + status, width), Color.BLACK, colors.statusBackground());
    }

    private static String fit(String text, int width) {
        if (width <= 0) {
            return "";
        }
        if (text.length() >= width) {
            return text.substring(0, width);
        }
        return text + " ".repeat(width - text.length());
    }
}
