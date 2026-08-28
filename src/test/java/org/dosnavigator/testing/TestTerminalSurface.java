package org.dosnavigator.testing;

import org.dosnavigator.terminal.Color;
import org.dosnavigator.terminal.KeyStroke;
import org.dosnavigator.terminal.TerminalSize;
import org.dosnavigator.terminal.TerminalSurface;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;

public final class TestTerminalSurface implements TerminalSurface {
    private final TerminalSize size;
    private final Cell[][] frontBuffer;
    private final Cell[][] backBuffer;
    private final Deque<KeyStroke> queuedKeys = new ArrayDeque<>();

    public TestTerminalSurface(TerminalSize size) {
        this.size = size;
        this.frontBuffer = createBuffer(size.columns(), size.rows());
        this.backBuffer = createBuffer(size.columns(), size.rows());
        fill(frontBuffer, new Cell(' ', Color.WHITE_BRIGHT, Color.DARK_BLUE));
        fill(backBuffer, new Cell(' ', Color.WHITE_BRIGHT, Color.DARK_BLUE));
    }

    @Override
    public void start() {
    }

    @Override
    public TerminalSize size() {
        return size;
    }

    @Override
    public void beginFrame() {
        copy(frontBuffer, backBuffer);
    }

    @Override
    public void clear(Color foreground, Color background) {
        Cell fill = new Cell(' ', foreground, background);
        fill(frontBuffer, fill);
        fill(backBuffer, fill);
    }

    @Override
    public void putChar(int x, int y, char ch, Color foreground, Color background) {
        putString(x, y, String.valueOf(ch), foreground, background);
    }

    @Override
    public void putString(int x, int y, String text, Color foreground, Color background) {
        if (text == null || text.isEmpty() || y < 0 || y >= size.rows()) {
            return;
        }
        int start = Math.max(0, x);
        int offset = Math.max(0, -x);
        for (int index = offset; index < text.length() && start < size.columns(); index++, start++) {
            backBuffer[y][start] = new Cell(text.charAt(index), foreground, background);
        }
    }

    @Override
    public void refresh() {
        copy(backBuffer, frontBuffer);
    }

    @Override
    public KeyStroke readKey() throws IOException {
        if (queuedKeys.isEmpty()) {
            throw new IOException("No queued keys in test terminal.");
        }
        return queuedKeys.removeFirst();
    }

    public void enqueue(KeyStroke... keys) {
        queuedKeys.addAll(Arrays.asList(keys));
    }

    public TerminalSnapshot snapshot() {
        return TerminalSnapshot.from(frontBuffer);
    }

    @Override
    public void close() {
    }

    private static Cell[][] createBuffer(int columns, int rows) {
        Cell[][] buffer = new Cell[rows][columns];
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < columns; x++) {
                buffer[y][x] = new Cell(' ', Color.WHITE_BRIGHT, Color.DARK_BLUE);
            }
        }
        return buffer;
    }

    private static void fill(Cell[][] buffer, Cell fill) {
        for (int y = 0; y < buffer.length; y++) {
            Arrays.fill(buffer[y], fill);
        }
    }

    private static void copy(Cell[][] source, Cell[][] target) {
        for (int y = 0; y < source.length; y++) {
            System.arraycopy(source[y], 0, target[y], 0, source[y].length);
        }
    }

    record Cell(char character, Color foreground, Color background) {
    }
}
