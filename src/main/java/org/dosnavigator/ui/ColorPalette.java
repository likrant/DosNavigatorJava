package org.dosnavigator.ui;

import org.dosnavigator.terminal.Color;

public record ColorPalette(
        ColorPair desktop,
        ColorPair panel,
        ColorPair panelTitle,
        ColorPair selected,
        ColorPair activeBorder,
        ColorPair inactiveBorder,
        ColorPair menu,
        ColorPair menuSelected,
        ColorPair status,
        ColorPair dialog,
        ColorPair disabled,
        ColorPair hotkey,
        ColorPair shadow
) {
    public static ColorPalette dosNavigator() {
        return new ColorPalette(
                new ColorPair(Color.WHITE_BRIGHT, Color.DARK_BLUE),
                new ColorPair(Color.WHITE_BRIGHT, Color.DARK_BLUE),
                new ColorPair(Color.YELLOW_BRIGHT, Color.DARK_BLUE),
                new ColorPair(Color.BLACK, Color.CYAN),
                new ColorPair(Color.CYAN, Color.DARK_BLUE),
                new ColorPair(Color.CYAN, Color.DARK_BLUE),
                new ColorPair(Color.YELLOW_BRIGHT, Color.DARK_BLUE),
                new ColorPair(Color.BLACK, Color.CYAN),
                new ColorPair(Color.BLACK, Color.CYAN),
                new ColorPair(Color.WHITE_BRIGHT, Color.DARK_BLUE),
                new ColorPair(Color.WHITE, Color.BLACK),
                new ColorPair(Color.YELLOW_BRIGHT, Color.DARK_BLUE),
                new ColorPair(Color.BLACK, Color.BLACK)
        );
    }
}
