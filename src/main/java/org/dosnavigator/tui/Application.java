package org.dosnavigator.tui;

import org.dosnavigator.command.CommandBus;
import org.dosnavigator.command.CommandId;
import org.dosnavigator.terminal.KeyStroke;
import org.dosnavigator.terminal.KeyType;
import org.dosnavigator.terminal.TerminalDriver;
import org.dosnavigator.terminal.TerminalSurface;
import org.dosnavigator.terminal.TerminalSize;
import org.dosnavigator.ui.Box;
import org.dosnavigator.ui.ColorPalette;

import java.io.Closeable;
import java.io.IOException;

public abstract class Application implements Closeable {
    private final TerminalSurface terminal;
    private final CommandBus commandBus = new CommandBus();
    private final ColorPalette palette = ColorPalette.dosNavigator();
    private Group root = new Group(new Box(0, 0, 1, 1));
    private boolean running = true;
    private boolean started;
    private TerminalSize lastSize = new TerminalSize(0, 0);

    protected Application(String terminalName) throws IOException {
        this(new TerminalDriver(terminalName));
    }

    protected Application(TerminalSurface terminal) {
        this.terminal = terminal;
        commandBus.register(CommandId.QUIT, ignored -> {
            running = false;
            return true;
        });
    }

    protected final TerminalSurface terminal() {
        return terminal;
    }

    protected final CommandBus commandBus() {
        return commandBus;
    }

    protected final ColorPalette palette() {
        return palette;
    }

    protected final Group root() {
        return root;
    }

    protected final void setRoot(Group root) {
        this.root = root;
    }

    public final void start() throws IOException {
        if (started) {
            return;
        }
        terminal.start();
        resizeIfNeeded(true);
        started = true;
    }

    public final void renderFrame() {
        terminal.beginFrame();
        root.render(new RenderContext(terminal, palette));
        terminal.refresh();
    }

    public final void dispatchKey(KeyStroke key) {
        resizeIfNeeded(false);
        handleKey(key);
    }

    public final boolean running() {
        return running;
    }

    public final void stop() {
        running = false;
    }

    public final void run() throws IOException {
        start();

        while (running) {
            renderFrame();
            KeyStroke key = terminal.readKey();
            dispatchKey(key);
        }
    }

    private void resizeIfNeeded(boolean force) {
        TerminalSize size = terminal.size();
        if (force || !size.equals(lastSize)) {
            lastSize = size;
            onResize(size);
            root.invalidate();
        }
    }

    protected abstract void onResize(TerminalSize size);

    protected void handleKey(KeyStroke key) {
        if (key == null) {
            return;
        }
        if (key.keyType() == KeyType.Character && key.character() != null
                && Character.toLowerCase(key.character()) == 'q'
                && key.hasModifier(org.dosnavigator.terminal.KeyModifier.CTRL)) {
            commandBus.dispatch(CommandId.QUIT);
            return;
        }
        if (!root.handleKey(key, commandBus)) {
            handleUnhandledKey(key);
        }
    }

    protected void handleUnhandledKey(KeyStroke key) {
        if (key.keyType() == KeyType.Escape) {
            commandBus.dispatch(CommandId.QUIT);
        }
    }

    public CommandId executeModal(Dialog dialog) throws IOException {
        root.add(dialog);
        root.setCurrent(dialog);
        while (dialog.endState().isEmpty() && running) {
            renderFrame();
            dialog.handleKey(terminal.readKey(), commandBus);
        }
        root.remove(dialog);
        return dialog.endState().orElse(CommandId.CANCEL);
    }

    @Override
    public void close() throws IOException {
        terminal.close();
    }
}
