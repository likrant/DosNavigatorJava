package org.dosnavigator.terminal;

public enum Color {
    BLACK("30", "40"),
    BLUE("34", "44"),
    DARK_BLUE("38;5;15", "48;5;18"),
    CYAN("36", "46"),
    CYAN_BRIGHT("96", "106"),
    GREEN("32", "42"),
    WHITE("37", "47"),
    WHITE_BRIGHT("97", "107"),
    YELLOW_BRIGHT("93", "103");

    private final String foregroundCode;
    private final String backgroundCode;

    Color(String foregroundCode, String backgroundCode) {
        this.foregroundCode = foregroundCode;
        this.backgroundCode = backgroundCode;
    }

    String foregroundCode() {
        return foregroundCode;
    }

    String backgroundCode() {
        return backgroundCode;
    }
}
