package org.dosnavigator.terminal;

public record ColorScheme(
        Color desktopBackground,
        Color panelBackground,
        Color normalText,
        Color selectionBackground,
        Color selectionText,
        Color activeBorder,
        Color inactiveBorder,
        Color menuBackground,
        Color statusBackground
) {
    public static ColorScheme dosNavigator() {
        return new ColorScheme(
                Color.BLUE,
                Color.BLUE,
                Color.WHITE_BRIGHT,
                Color.CYAN,
                Color.BLACK,
                Color.WHITE_BRIGHT,
                Color.WHITE,
                Color.BLUE,
                Color.CYAN
        );
    }
}
