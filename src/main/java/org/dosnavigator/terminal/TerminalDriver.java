package org.dosnavigator.terminal;

import org.jline.terminal.Attributes;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.InfoCmp.Capability;
import org.jline.utils.NonBlockingReader;

import java.io.Closeable;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

public final class TerminalDriver implements Closeable {
    private final Terminal terminal;
    private final PrintWriter writer;
    private Attributes originalAttributes;

    public TerminalDriver(String name) throws IOException {
        terminal = TerminalBuilder.builder()
                .name(name)
                .system(true)
                .encoding(StandardCharsets.UTF_8)
                .jna(true)
                .build();
        writer = terminal.writer();
    }

    public void start() {
        originalAttributes = terminal.enterRawMode();
        terminal.puts(Capability.enter_ca_mode);
        terminal.puts(Capability.cursor_invisible);
        terminal.puts(Capability.clear_screen);
        writer.flush();
    }

    public TerminalSize size() {
        org.jline.terminal.Size size = terminal.getSize();
        return new TerminalSize(Math.max(1, size.getColumns()), Math.max(1, size.getRows()));
    }

    public void clear(Color foreground, Color background) {
        writer.print("\033[0m");
        setColors(foreground, background);
        terminal.puts(Capability.clear_screen);
    }

    public void putChar(int x, int y, char ch, Color foreground, Color background) {
        putString(x, y, String.valueOf(ch), foreground, background);
    }

    public void putString(int x, int y, String text, Color foreground, Color background) {
        if (x < 0 || y < 0 || text.isEmpty()) {
            return;
        }
        writer.printf("\033[%d;%dH", y + 1, x + 1);
        setColors(foreground, background);
        writer.print(text);
    }

    public void refresh() {
        writer.flush();
    }

    public KeyStroke readKey() throws IOException {
        int ch = terminal.reader().read();
        return decode(ch);
    }

    private KeyStroke decode(int ch) throws IOException {
        return switch (ch) {
            case 3, 27 -> decodeEscape();
            case 9 -> KeyStroke.of(KeyType.Tab);
            case 10, 13 -> KeyStroke.of(KeyType.Enter);
            case 8, 127 -> KeyStroke.of(KeyType.Backspace);
            default -> ch >= 32 ? KeyStroke.character((char) ch) : KeyStroke.of(KeyType.Unknown);
        };
    }

    private KeyStroke decodeEscape() throws IOException {
        NonBlockingReader reader = terminal.reader();
        int first = reader.read(50);
        if (first == NonBlockingReader.READ_EXPIRED || first < 0) {
            return KeyStroke.of(KeyType.Escape);
        }
        if (first == '[') {
            return decodeControlSequence(reader);
        }
        if (first == 'O') {
            int second = reader.read(50);
            return second == 'Y' ? KeyStroke.of(KeyType.F10) : KeyStroke.of(KeyType.Unknown);
        }
        return KeyStroke.of(KeyType.Escape);
    }

    private KeyStroke decodeControlSequence(NonBlockingReader reader) throws IOException {
        int second = reader.read(50);
        return switch (second) {
            case 'A' -> KeyStroke.of(KeyType.ArrowUp);
            case 'B' -> KeyStroke.of(KeyType.ArrowDown);
            case 'F' -> KeyStroke.of(KeyType.End);
            case 'H' -> KeyStroke.of(KeyType.Home);
            case '1', '5', '6', '2' -> decodeTildeSequence(reader, second);
            default -> KeyStroke.of(KeyType.Unknown);
        };
    }

    private KeyStroke decodeTildeSequence(NonBlockingReader reader, int firstDigit) throws IOException {
        StringBuilder value = new StringBuilder();
        value.append((char) firstDigit);
        int next;
        while ((next = reader.read(50)) != NonBlockingReader.READ_EXPIRED && next >= 0) {
            if (next == '~') {
                break;
            }
            value.append((char) next);
        }
        return switch (value.toString()) {
            case "1" -> KeyStroke.of(KeyType.Home);
            case "5" -> KeyStroke.of(KeyType.PageUp);
            case "6" -> KeyStroke.of(KeyType.PageDown);
            case "21" -> KeyStroke.of(KeyType.F10);
            default -> KeyStroke.of(KeyType.Unknown);
        };
    }

    private void setColors(Color foreground, Color background) {
        writer.printf("\033[%d;%dm", foreground.foregroundCode(), background.backgroundCode());
    }

    @Override
    public void close() throws IOException {
        writer.print("\033[0m");
        terminal.puts(Capability.cursor_visible);
        terminal.puts(Capability.exit_ca_mode);
        if (originalAttributes != null) {
            terminal.setAttributes(originalAttributes);
        }
        writer.flush();
        terminal.close();
    }
}
