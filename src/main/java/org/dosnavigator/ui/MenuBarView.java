package org.dosnavigator.ui;

import org.dosnavigator.command.CommandBus;
import org.dosnavigator.command.CommandId;
import org.dosnavigator.terminal.KeyStroke;
import org.dosnavigator.terminal.KeyType;
import org.dosnavigator.tui.RenderContext;
import org.dosnavigator.tui.View;

import java.util.List;

public final class MenuBarView extends View {
    private static final List<String> ITEMS = List.of("Left", "File", "Commands", "Options", "Right");
    private boolean active;
    private int selectedIndex;

    public MenuBarView(Box bounds) {
        super(bounds);
        setFocusable(false);
    }

    public boolean active() {
        return active;
    }

    public void setActive(boolean active) {
        if (this.active != active) {
            this.active = active;
            invalidate();
        }
    }

    @Override
    protected void draw(RenderContext context) {
        Box bounds = bounds();
        ColorPair menu = context.palette().menu();
        context.terminal().putString(bounds.x(), bounds.y(), " ".repeat(Math.max(0, bounds.width())), menu.foreground(), menu.background());

        int x = bounds.x() + 2;
        for (int i = 0; i < ITEMS.size(); i++) {
            String text = " " + ITEMS.get(i) + " ";
            ColorPair color = active && i == selectedIndex ? context.palette().menuSelected() : menu;
            context.terminal().putString(x, bounds.y(), trim(text, Math.max(0, bounds.x() + bounds.width() - x)), color.foreground(), color.background());
            x += text.length() + 1;
        }
    }

    @Override
    public boolean handleKey(KeyStroke key, CommandBus commandBus) {
        if (key.keyType() == KeyType.F10) {
            return commandBus.dispatch(CommandId.MENU);
        }
        if (!active) {
            return false;
        }

        switch (key.keyType()) {
            case Escape -> {
                return commandBus.dispatch(CommandId.MENU);
            }
            case ArrowLeft -> {
                selectedIndex = Math.floorMod(selectedIndex - 1, ITEMS.size());
                invalidate();
                return true;
            }
            case ArrowRight, Tab -> {
                selectedIndex = Math.floorMod(selectedIndex + 1, ITEMS.size());
                invalidate();
                return true;
            }
            case Enter -> {
                return true;
            }
            default -> {
                return false;
            }
        }
    }

    private static String trim(String text, int width) {
        if (width <= 0) {
            return "";
        }
        if (text.length() <= width) {
            return text;
        }
        return text.substring(0, width);
    }
}
