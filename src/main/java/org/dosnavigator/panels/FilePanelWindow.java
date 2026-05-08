package org.dosnavigator.panels;

import org.dosnavigator.command.CommandBus;
import org.dosnavigator.terminal.KeyStroke;
import org.dosnavigator.tui.RenderContext;
import org.dosnavigator.tui.Window;
import org.dosnavigator.ui.Box;

import java.util.LinkedHashSet;
import java.util.Set;

public final class FilePanelWindow extends Window {
    private final FilePanel panel;
    private final Set<Integer> dirtyRows = new LinkedHashSet<>();
    private boolean forceFullRender;

    public FilePanelWindow(Box bounds, FilePanel panel) {
        super(bounds, "");
        this.panel = panel;
    }

    public String statusText() {
        return panel.statusText();
    }

    @Override
    protected void draw(RenderContext context) {
        if (selfInvalid() || forceFullRender || dirtyRows.isEmpty()) {
            panel.render(context.terminal(), bounds(), focused(), context.palette());
            forceFullRender = false;
        } else {
            panel.renderRows(context.terminal(), bounds(), focused(), context.palette(), dirtyRows);
        }
        dirtyRows.clear();
    }

    @Override
    public boolean handleKey(KeyStroke key, CommandBus commandBus) {
        boolean handled = switch (key.keyType()) {
            case ArrowUp, ArrowDown, ArrowLeft, ArrowRight, PageUp, PageDown, Home, End, Enter, Backspace, Insert, GrayPlus, GrayMinus, GrayStar, F9, Character, Mouse -> true;
            default -> false;
        };
        if (handled) {
            int previousSelected = panel.selectedIndex();
            int previousTop = panel.topIndex();
            if (key.mouseEvent() != null) {
                panel.handleMouse(key.mouseEvent(), bounds());
            } else {
                panel.handleKey(key);
            }
            if (panel.topIndex() == previousTop && isSelectionOnlyKey(key)) {
                dirtyRows.add(previousSelected - panel.topIndex() + 1);
                dirtyRows.add(panel.selectedIndex() - panel.topIndex() + 1);
                invalidatePartial();
            } else {
                forceFullRender = true;
                invalidate();
            }
            return true;
        }
        return super.handleKey(key, commandBus);
    }

    @Override
    protected void onBoundsChanged() {
        forceFullRender = true;
    }

    private static boolean isSelectionOnlyKey(KeyStroke key) {
        return switch (key.keyType()) {
            case ArrowUp, ArrowDown, ArrowLeft, ArrowRight, PageUp, PageDown, Home, End -> true;
            default -> false;
        };
    }
}
