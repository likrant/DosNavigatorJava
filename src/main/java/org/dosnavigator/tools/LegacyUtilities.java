package org.dosnavigator.tools;

import java.time.YearMonth;

/** Initial pure-Java adapters for Calculat.pas and calendar.pas. */
public final class LegacyUtilities {
    private LegacyUtilities() {
    }

    public static long calculate(long left, char operator, long right) {
        return switch (operator) {
            case '+' -> left + right;
            case '-' -> left - right;
            case '*' -> left * right;
            case '/' -> left / right;
            default -> throw new IllegalArgumentException("Unsupported calculator operator: " + operator);
        };
    }

    public static int daysInMonth(int year, int month) {
        return YearMonth.of(year, month).lengthOfMonth();
    }
}
