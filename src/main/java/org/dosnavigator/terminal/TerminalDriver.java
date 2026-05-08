package org.dosnavigator.terminal;

import org.jline.terminal.Attributes;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.terminal.TerminalBuilder.SystemOutput;
import org.jline.utils.InfoCmp.Capability;

import java.io.Closeable;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

public final class TerminalDriver implements Closeable {
    private final Terminal terminal;
    private final PrintWriter writer;
    private Attributes originalAttributes;
    private Cell[][] frontBuffer = new Cell[0][0];
    private Cell[][] backBuffer = new Cell[0][0];
    private TerminalSize bufferSize = new TerminalSize(0, 0);

    public TerminalDriver(String name) throws IOException {
        try {
            terminal = TerminalBuilder.builder()
                    .name(name)
                    .system(true)
                    .systemOutput(SystemOutput.ForcedSysOut)
                    .provider("jna")
                    .dumb(false)
                    .encoding(StandardCharsets.UTF_8)
                    .build();
        } catch (IOException | IllegalStateException e) {
            throw new IOException("""
                    Unable to open an interactive terminal.
                    Run this from Windows Terminal, PowerShell, or cmd.exe with stdin/stdout attached to the console.
                    IDE run consoles and redirected Maven sessions usually cannot run the TUI.
                    """, e);
        }
        if ("dumb".equalsIgnoreCase(terminal.getType())) {
            throw new IOException("JLine created a dumb terminal; refusing to start the TUI without ANSI/raw-key support.");
        }
        writer = terminal.writer();
    }

    public void start() {
        originalAttributes = terminal.enterRawMode();
        terminal.puts(Capability.enter_ca_mode);
        terminal.puts(Capability.cursor_invisible);
        terminal.puts(Capability.clear_screen);
        writer.print("\033[?1000h\033[?1002h\033[?1003h\033[?1006h");
        writer.flush();
    }

    public TerminalSize size() {
        org.jline.terminal.Size size = terminal.getSize();
        return new TerminalSize(Math.max(1, size.getColumns()), Math.max(1, size.getRows()));
    }

    public void beginFrame() {
        TerminalSize size = size();
        ensureBuffers(size);
        for (int row = 0; row < size.rows(); row++) {
            System.arraycopy(frontBuffer[row], 0, backBuffer[row], 0, size.columns());
        }
    }

    public void clear(Color foreground, Color background) {
        writer.print("\033[0m");
        setColors(foreground, background);
        terminal.puts(Capability.clear_screen);
        bufferSize = new TerminalSize(0, 0);
        frontBuffer = new Cell[0][0];
        backBuffer = new Cell[0][0];
    }

    public void putChar(int x, int y, char ch, Color foreground, Color background) {
        putString(x, y, String.valueOf(ch), foreground, background);
    }

    public void putString(int x, int y, String text, Color foreground, Color background) {
        if (y < 0 || y >= bufferSize.rows() || text.isEmpty()) {
            return;
        }
        int start = Math.max(0, x);
        int offset = Math.max(0, -x);
        for (int index = offset; index < text.length() && start < bufferSize.columns(); index++, start++) {
            backBuffer[y][start] = new Cell(text.charAt(index), foreground, background);
        }
    }

    public void refresh() {
        if (bufferSize.columns() > 0 && bufferSize.rows() > 0) {
            writeDiff();
        }
        writer.flush();
    }

    public KeyStroke readKey() throws IOException {
        int ch = terminal.reader().read();
        return TerminalKeyDecoder.decode(ch, terminal.reader()::read);
    }

    private void setColors(Color foreground, Color background) {
        writer.printf("\033[%s;%sm", foreground.foregroundCode(), background.backgroundCode());
    }

    private void ensureBuffers(TerminalSize size) {
        if (size.equals(bufferSize)) {
            return;
        }
        bufferSize = size;
        frontBuffer = createBuffer(size, null);
        backBuffer = createBuffer(size, new Cell(' ', Color.WHITE_BRIGHT, Color.BLUE));
    }

    private static Cell[][] createBuffer(TerminalSize size, Cell fill) {
        Cell[][] buffer = new Cell[size.rows()][size.columns()];
        for (int row = 0; row < size.rows(); row++) {
            for (int column = 0; column < size.columns(); column++) {
                buffer[row][column] = fill;
            }
        }
        return buffer;
    }

    private void writeDiff() {
        for (int row = 0; row < bufferSize.rows(); row++) {
            int column = 0;
            while (column < bufferSize.columns()) {
                Cell next = backBuffer[row][column];
                Cell current = frontBuffer[row][column];
                if (next == null || next.equals(current)) {
                    column++;
                    continue;
                }

                int runStart = column;
                Color foreground = next.foreground();
                Color background = next.background();
                StringBuilder run = new StringBuilder();
                while (column < bufferSize.columns()) {
                    Cell candidate = backBuffer[row][column];
                    Cell existing = frontBuffer[row][column];
                    if (candidate == null || candidate.equals(existing)
                            || candidate.foreground() != foreground
                            || candidate.background() != background) {
                        break;
                    }
                    run.append(candidate.character());
                    frontBuffer[row][column] = candidate;
                    column++;
                }

                writer.printf("\033[%d;%dH", row + 1, runStart + 1);
                setColors(foreground, background);
                writer.print(run);
            }
        }
    }

    @Override
    public void close() throws IOException {
        writer.print("\033[0m");
        writer.print("\033[?1006l\033[?1003l\033[?1002l\033[?1000l");
        terminal.puts(Capability.cursor_visible);
        terminal.puts(Capability.exit_ca_mode);
        if (originalAttributes != null) {
            terminal.setAttributes(originalAttributes);
        }
        writer.flush();
        terminal.close();
    }

    private record Cell(char character, Color foreground, Color background) {
    }
}
