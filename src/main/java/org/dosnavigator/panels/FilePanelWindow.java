package org.dosnavigator.panels;

import org.dosnavigator.command.CommandBus;
import org.dosnavigator.terminal.KeyStroke;
import org.dosnavigator.tui.RenderContext;
import org.dosnavigator.tui.Window;
import org.dosnavigator.ui.Box;

public final class FilePanelWindow extends Window {
    private final FilePanel panel;

    public FilePanelWindow(Box bounds, FilePanel panel) {
        super(bounds, "");
        this.panel = panel;
    }

    public String statusText() {
        return panel.statusText();
    }

    @Override
    protected void draw(RenderContext context) {
        panel.render(context.terminal(), bounds(), focused(), context.palette());
    }

    @Override
    public boolean handleKey(KeyStroke key, CommandBus commandBus) {
        panel.handleKey(key);
        return switch (key.keyType()) {
            case ArrowUp, ArrowDown, PageUp, PageDown, Home, End, Enter, Backspace -> true;
            default -> super.handleKey(key, commandBus);
        };
    }
}
