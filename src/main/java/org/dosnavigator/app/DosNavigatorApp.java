package org.dosnavigator.app;

import org.dosnavigator.fs.LocalFileSystemService;
import org.dosnavigator.panels.FilePanel;
import org.dosnavigator.terminal.Color;
import org.dosnavigator.terminal.ColorScheme;
import org.dosnavigator.terminal.KeyStroke;
import org.dosnavigator.terminal.KeyType;
import org.dosnavigator.terminal.TerminalDriver;
import org.dosnavigator.terminal.TerminalSize;
import org.dosnavigator.ui.Box;
import org.dosnavigator.ui.StatusLine;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Path;

public final class DosNavigatorApp implements Closeable {
    private final TerminalDriver terminal;
    private final ColorScheme colors = ColorScheme.dosNavigator();
    private final StatusLine statusLine = new StatusLine();
    private final FilePanel leftPanel;
    private final FilePanel rightPanel;
    private boolean leftActive = true;
    private boolean running = true;

    public DosNavigatorApp(Path leftDirectory, Path rightDirectory) throws IOException {
        terminal = new TerminalDriver("Dos Navigator Java");

        LocalFileSystemService fileSystem = new LocalFileSystemService();
        leftPanel = new FilePanel(fileSystem, leftDirectory);
        rightPanel = new FilePanel(fileSystem, rightDirectory);
    }

    public void run() throws IOException {
        terminal.start();

        while (running) {
            render();
            handleKey(terminal.readKey());
        }
    }

    private void render() throws IOException {
        TerminalSize size = terminal.size();

        terminal.clear(colors.normalText(), colors.desktopBackground());

        int width = size.columns();
        int height = size.rows();
        int statusHeight = 1;
        int panelTop = 1;
        int panelHeight = Math.max(3, height - panelTop - statusHeight);
        int leftWidth = width / 2;
        int rightWidth = width - leftWidth;

        drawMenuBar(width);
        leftPanel.render(terminal, new Box(0, panelTop, leftWidth, panelHeight), leftActive, colors);
        rightPanel.render(terminal, new Box(leftWidth, panelTop, rightWidth, panelHeight), !leftActive, colors);
        statusLine.render(terminal, height - 1, width, activePanel().statusText(), colors);

        terminal.refresh();
    }

    private void drawMenuBar(int width) {
        terminal.putString(0, 0, fit("  Left  File  Commands  Options  Right", width), Color.YELLOW_BRIGHT, colors.menuBackground());
    }

    private void handleKey(KeyStroke key) throws IOException {
        if (key == null) {
            return;
        }

        KeyType type = key.getKeyType();
        if (type == KeyType.Escape || type == KeyType.F10) {
            running = false;
            return;
        }
        if (type == KeyType.Character && key.getCharacter() != null) {
            char ch = Character.toLowerCase(key.getCharacter());
            if (ch == 'q') {
                running = false;
                return;
            }
        }
        if (type == KeyType.Tab) {
            leftActive = !leftActive;
            return;
        }

        activePanel().handleKey(key);
    }

    private FilePanel activePanel() {
        return leftActive ? leftPanel : rightPanel;
    }

    private static String fit(String text, int width) {
        if (width <= 0) {
            return "";
        }
        if (text.length() >= width) {
            return text.substring(0, width);
        }
        return text + " ".repeat(width - text.length());
    }

    @Override
    public void close() throws IOException {
        terminal.close();
    }
}
