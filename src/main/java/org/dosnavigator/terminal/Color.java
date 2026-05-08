package org.dosnavigator.terminal;

public enum Color {
    BLACK(30, 40),
    BLUE(34, 44),
    CYAN(36, 46),
    GREEN(32, 42),
    WHITE(37, 47),
    WHITE_BRIGHT(97, 107),
    YELLOW_BRIGHT(93, 103);

    private final int foregroundCode;
    private final int backgroundCode;

    Color(int foregroundCode, int backgroundCode) {
        this.foregroundCode = foregroundCode;
        this.backgroundCode = backgroundCode;
    }

    int foregroundCode() {
        return foregroundCode;
    }

    int backgroundCode() {
        return backgroundCode;
    }
}
