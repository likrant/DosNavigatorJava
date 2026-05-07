package org.dosnavigator.ui;

import org.dosnavigator.terminal.Color;
import org.dosnavigator.terminal.TerminalDriver;

public record Box(int x, int y, int width, int height) {
    private static final char TOP_LEFT = '\u250c';
    private static final char TOP_RIGHT = '\u2510';
    private static final char BOTTOM_LEFT = '\u2514';
    private static final char BOTTOM_RIGHT = '\u2518';
    private static final char HORIZONTAL = '\u2500';
    private static final char VERTICAL = '\u2502';

    public void draw(TerminalDriver terminal, Color foreground, Color background) {
        if (width <= 1 || height <= 1) {
            return;
        }

        terminal.putChar(x, y, TOP_LEFT, foreground, background);
        terminal.putChar(x + width - 1, y, TOP_RIGHT, foreground, background);
        terminal.putChar(x, y + height - 1, BOTTOM_LEFT, foreground, background);
        terminal.putChar(x + width - 1, y + height - 1, BOTTOM_RIGHT, foreground, background);

        for (int column = x + 1; column < x + width - 1; column++) {
            terminal.putChar(column, y, HORIZONTAL, foreground, background);
            terminal.putChar(column, y + height - 1, HORIZONTAL, foreground, background);
        }

        for (int row = y + 1; row < y + height - 1; row++) {
            terminal.putChar(x, row, VERTICAL, foreground, background);
            terminal.putChar(x + width - 1, row, VERTICAL, foreground, background);
        }
    }
}
