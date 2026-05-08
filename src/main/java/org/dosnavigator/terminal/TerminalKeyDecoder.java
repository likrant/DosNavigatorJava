package org.dosnavigator.terminal;

import org.jline.utils.NonBlockingReader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public final class TerminalKeyDecoder {
    private TerminalKeyDecoder() {
    }

    @FunctionalInterface
    public interface Input {
        int read(int timeoutMillis) throws IOException;
    }

    public static KeyStroke decode(int ch, Input input) throws IOException {
        return switch (ch) {
            case 3 -> KeyStroke.of(KeyType.Escape, KeyModifier.CTRL);
            case 27 -> decodeEscape(input);
            case 9 -> KeyStroke.of(KeyType.Tab);
            case 10, 13 -> KeyStroke.of(KeyType.Enter);
            case 8, 127 -> KeyStroke.of(KeyType.Backspace);
            default -> ch >= 32 ? KeyStroke.character((char) ch) : KeyStroke.of(KeyType.Unknown);
        };
    }

    public static KeyStroke decodeAnsiSequence(String sequence) {
        if (sequence == null || sequence.isEmpty()) {
            return KeyStroke.of(KeyType.Unknown);
        }
        Cursor cursor = new Cursor(sequence);
        try {
            return decode(cursor.read(0), cursor::read);
        } catch (IOException e) {
            return KeyStroke.of(KeyType.Unknown);
        }
    }

    private static KeyStroke decodeEscape(Input input) throws IOException {
        int first = input.read(50);
        if (first == NonBlockingReader.READ_EXPIRED || first < 0) {
            return KeyStroke.of(KeyType.Escape);
        }
        if (first == '[') {
            return decodeControlSequence(input);
        }
        if (first == 'O') {
            return decodeApplicationSequence(input);
        }
        if (first >= 32) {
            return KeyStroke.character((char) first, KeyModifier.ALT);
        }
        return KeyStroke.of(KeyType.Escape);
    }

    private static KeyStroke decodeApplicationSequence(Input input) throws IOException {
        int second = input.read(50);
        return switch (second) {
            case 'A' -> KeyStroke.of(KeyType.ArrowUp);
            case 'B' -> KeyStroke.of(KeyType.ArrowDown);
            case 'C' -> KeyStroke.of(KeyType.ArrowRight);
            case 'D' -> KeyStroke.of(KeyType.ArrowLeft);
            case 'P' -> KeyStroke.of(KeyType.F1);
            case 'Q' -> KeyStroke.of(KeyType.F2);
            case 'R' -> KeyStroke.of(KeyType.F3);
            case 'S' -> KeyStroke.of(KeyType.F4);
            case 'H' -> KeyStroke.of(KeyType.Home);
            case 'F' -> KeyStroke.of(KeyType.End);
            case 'k' -> KeyStroke.of(KeyType.GrayPlus);
            case 'm' -> KeyStroke.of(KeyType.GrayMinus);
            case 'Y' -> KeyStroke.of(KeyType.F10);
            default -> KeyStroke.of(KeyType.Unknown);
        };
    }

    private static KeyStroke decodeControlSequence(Input input) throws IOException {
        StringBuilder sequence = new StringBuilder();
        int next;
        while ((next = input.read(50)) != NonBlockingReader.READ_EXPIRED && next >= 0) {
            sequence.append((char) next);
            if (next >= 0x40 && next <= 0x7e) {
                break;
            }
        }
        if (sequence.isEmpty()) {
            return KeyStroke.of(KeyType.Unknown);
        }

        char finalChar = sequence.charAt(sequence.length() - 1);
        String parameterText = sequence.substring(0, sequence.length() - 1);
        List<Integer> parameters = parseParameters(parameterText);
        EnumSet<KeyModifier> modifiers = decodeModifiers(parameters.size() >= 2 ? parameters.get(1) : 1);

        return switch (finalChar) {
            case 'A' -> KeyStroke.of(KeyType.ArrowUp, modifiers);
            case 'B' -> KeyStroke.of(KeyType.ArrowDown, modifiers);
            case 'C' -> KeyStroke.of(KeyType.ArrowRight, modifiers);
            case 'D' -> KeyStroke.of(KeyType.ArrowLeft, modifiers);
            case 'F' -> KeyStroke.of(KeyType.End, modifiers);
            case 'H' -> KeyStroke.of(KeyType.Home, modifiers);
            case 'Z' -> KeyStroke.of(KeyType.ShiftTab, KeyModifier.SHIFT);
            case '~' -> decodeTilde(parameters, modifiers);
            default -> KeyStroke.of(KeyType.Unknown);
        };
    }

    private static KeyStroke decodeTilde(List<Integer> parameters, EnumSet<KeyModifier> modifiers) {
        if (parameters.isEmpty()) {
            return KeyStroke.of(KeyType.Unknown);
        }
        KeyType type = switch (parameters.getFirst()) {
            case 1, 7 -> KeyType.Home;
            case 2 -> KeyType.Insert;
            case 3 -> KeyType.Delete;
            case 4, 8 -> KeyType.End;
            case 5 -> KeyType.PageUp;
            case 6 -> KeyType.PageDown;
            case 15 -> KeyType.F5;
            case 17 -> KeyType.F6;
            case 18 -> KeyType.F7;
            case 19 -> KeyType.F8;
            case 20 -> KeyType.F9;
            case 21 -> KeyType.F10;
            case 23 -> KeyType.F11;
            case 24 -> KeyType.F12;
            default -> KeyType.Unknown;
        };
        return KeyStroke.of(type, modifiers);
    }

    private static List<Integer> parseParameters(String parameterText) {
        if (parameterText.isBlank()) {
            return List.of(1);
        }
        String[] parts = parameterText.split(";");
        List<Integer> parameters = new ArrayList<>(parts.length);
        for (String part : parts) {
            if (part.isBlank() || part.equals("?")) {
                parameters.add(1);
                continue;
            }
            try {
                parameters.add(Integer.parseInt(part.replace("?", "")));
            } catch (NumberFormatException ignored) {
                parameters.add(1);
            }
        }
        return parameters;
    }

    private static EnumSet<KeyModifier> decodeModifiers(int parameter) {
        EnumSet<KeyModifier> modifiers = EnumSet.noneOf(KeyModifier.class);
        int mask = Math.max(1, parameter) - 1;
        if ((mask & 1) != 0) {
            modifiers.add(KeyModifier.SHIFT);
        }
        if ((mask & 2) != 0) {
            modifiers.add(KeyModifier.ALT);
        }
        if ((mask & 4) != 0) {
            modifiers.add(KeyModifier.CTRL);
        }
        return modifiers;
    }

    private static final class Cursor {
        private final String sequence;
        private int index;

        private Cursor(String sequence) {
            this.sequence = sequence;
        }

        private int read(int timeoutMillis) {
            if (index >= sequence.length()) {
                return NonBlockingReader.READ_EXPIRED;
            }
            return sequence.charAt(index++);
        }
    }
}
