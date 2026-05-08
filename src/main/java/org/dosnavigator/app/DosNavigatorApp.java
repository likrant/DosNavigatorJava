package org.dosnavigator.app;

import org.dosnavigator.command.CommandId;
import org.dosnavigator.fs.LocalFileSystemService;
import org.dosnavigator.panels.FilePanel;
import org.dosnavigator.panels.FilePanelWindow;
import org.dosnavigator.terminal.KeyStroke;
import org.dosnavigator.terminal.KeyType;
import org.dosnavigator.terminal.TerminalSize;
import org.dosnavigator.tui.Application;
import org.dosnavigator.tui.Desktop;
import org.dosnavigator.tui.Group;
import org.dosnavigator.ui.Box;
import org.dosnavigator.ui.MenuBarView;
import org.dosnavigator.ui.StatusLineView;

import java.io.IOException;
import java.nio.file.Path;

public final class DosNavigatorApp extends Application {
    private final MenuBarView menuBar;
    private final Desktop desktop;
    private final FilePanelWindow leftPanel;
    private final FilePanelWindow rightPanel;
    private final StatusLineView statusLine;

    public DosNavigatorApp(Path leftDirectory, Path rightDirectory) throws IOException {
        super("Dos Navigator Java");

        LocalFileSystemService fileSystem = new LocalFileSystemService();
        leftPanel = new FilePanelWindow(new Box(0, 1, 1, 1), new FilePanel(fileSystem, leftDirectory));
        rightPanel = new FilePanelWindow(new Box(1, 1, 1, 1), new FilePanel(fileSystem, rightDirectory));
        menuBar = new MenuBarView(new Box(0, 0, 1, 1));
        desktop = new Desktop(new Box(0, 1, 1, 1));
        statusLine = new StatusLineView(new Box(0, 2, 1, 1), this::activeStatusText);

        desktop.add(leftPanel);
        desktop.add(rightPanel);
        desktop.setCurrent(leftPanel);

        Group root = new Group(new Box(0, 0, 1, 1));
        root.add(desktop);
        root.add(menuBar);
        root.add(statusLine);
        setRoot(root);

        commandBus().register(CommandId.NEXT, ignored -> desktop.selectNext(true));
        commandBus().register(CommandId.PREVIOUS, ignored -> desktop.selectNext(false));
        commandBus().register(CommandId.MENU, ignored -> {
            menuBar.setActive(!menuBar.active());
            commandBus().dispatch(menuBar.active() ? CommandId.MENU_ON : CommandId.MENU_OFF);
            return true;
        });
    }

    @Override
    protected void onResize(TerminalSize size) {
        AppLayout layout = AppLayout.calculate(size);
        root().setBounds(layout.root());
        menuBar.setBounds(layout.menuBar());
        desktop.setBounds(layout.desktop());
        leftPanel.setBounds(layout.leftPanel());
        rightPanel.setBounds(layout.rightPanel());
        statusLine.setBounds(layout.statusLine());
    }

    @Override
    protected void handleKey(KeyStroke key) {
        if (key.keyType() == KeyType.Tab && !menuBar.active()) {
            commandBus().dispatch(CommandId.NEXT);
            return;
        }
        super.handleKey(key);
    }

    @Override
    protected void handleUnhandledKey(KeyStroke key) {
        if (key.keyType() == KeyType.F10) {
            commandBus().dispatch(CommandId.MENU);
            return;
        }
        if (key.keyType() == KeyType.Escape && menuBar.active()) {
            commandBus().dispatch(CommandId.MENU);
            return;
        }
        super.handleUnhandledKey(key);
    }

    private String activeStatusText() {
        if (desktop.current() == rightPanel) {
            return rightPanel.statusText();
        }
        return leftPanel.statusText();
    }
}
