package org.dosnavigator.terminal;

import java.io.Closeable;
import java.io.IOException;

public interface TerminalSurface extends Closeable {
    void start() throws IOException;

    TerminalSize size();

    void beginFrame();

    void clear(Color foreground, Color background);

    void putChar(int x, int y, char ch, Color foreground, Color background);

    void putString(int x, int y, String text, Color foreground, Color background);

    void refresh();

    KeyStroke readKey() throws IOException;
}
