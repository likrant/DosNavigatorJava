package org.dosnavigator.tui;

import org.dosnavigator.command.CommandBus;
import org.dosnavigator.command.CommandId;
import org.dosnavigator.terminal.KeyStroke;
import org.dosnavigator.terminal.KeyType;
import org.dosnavigator.ui.Box;
import org.dosnavigator.ui.ColorPair;

public class Window extends Group {
    private final String title;

    public Window(Box bounds, String title) {
        super(bounds);
        this.title = title;
        setFocusable(true);
    }

    protected String title() {
        return title;
    }

    @Override
    protected void draw(RenderContext context) {
        drawFrame(context);
        super.draw(context);
    }

    protected void drawFrame(RenderContext context) {
        ColorPair border = focused() ? context.palette().activeBorder() : context.palette().inactiveBorder();
        Box bounds = bounds();
        bounds.draw(context.terminal(), border.foreground(), border.background());
        String titleText = " " + title + " ";
        context.terminal().putString(
                bounds.x() + 2,
                bounds.y(),
                fit(titleText, Math.max(0, bounds.width() - 4)),
                context.palette().panelTitle().foreground(),
                context.palette().panelTitle().background()
        );
    }

    @Override
    public boolean handleKey(KeyStroke key, CommandBus commandBus) {
        if (super.handleKey(key, commandBus)) {
            return true;
        }
        if (key.keyType() == KeyType.Escape) {
            return commandBus.dispatch(CommandId.CANCEL);
        }
        return false;
    }

    protected static String fit(String text, int width) {
        if (width <= 0) {
            return "";
        }
        if (text.length() >= width) {
            return text.substring(0, width);
        }
        return text + " ".repeat(width - text.length());
    }
}
