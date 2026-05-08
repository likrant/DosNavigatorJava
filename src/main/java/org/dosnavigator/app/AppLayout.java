package org.dosnavigator.app;

import org.dosnavigator.terminal.TerminalSize;
import org.dosnavigator.ui.Box;

public record AppLayout(Box root, Box menuBar, Box desktop, Box leftPanel, Box rightPanel, Box statusLine) {
    public static AppLayout calculate(TerminalSize size) {
        int width = Math.max(1, size.columns());
        int height = Math.max(3, size.rows());
        int desktopTop = 1;
        int statusY = height - 1;
        int desktopHeight = Math.max(1, statusY - desktopTop);
        int leftWidth = Math.max(1, width / 2);
        int rightWidth = Math.max(0, width - leftWidth);

        Box root = new Box(0, 0, width, height);
        Box menuBar = new Box(0, 0, width, 1);
        Box desktop = new Box(0, desktopTop, width, desktopHeight);
        Box leftPanel = new Box(0, desktopTop, leftWidth, desktopHeight);
        Box rightPanel = new Box(leftWidth, desktopTop, rightWidth, desktopHeight);
        Box statusLine = new Box(0, statusY, width, 1);
        return new AppLayout(root, menuBar, desktop, leftPanel, rightPanel, statusLine);
    }
}
