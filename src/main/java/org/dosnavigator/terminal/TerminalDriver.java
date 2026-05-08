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
        return TerminalKeyDecoder.decode(ch, terminal.reader()::read);
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
