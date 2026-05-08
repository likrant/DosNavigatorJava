package org.dosnavigator.ui;

import org.dosnavigator.command.CommandBus;
import org.dosnavigator.command.CommandId;
import org.dosnavigator.terminal.KeyStroke;
import org.dosnavigator.terminal.KeyType;
import org.dosnavigator.tui.RenderContext;
import org.dosnavigator.tui.View;

import java.util.function.Supplier;

public final class StatusLineView extends View {
    private final Supplier<String> statusSupplier;

    public StatusLineView(Box bounds, Supplier<String> statusSupplier) {
        super(bounds);
        this.statusSupplier = statusSupplier;
    }

    @Override
    protected void draw(RenderContext context) {
        String text = " F1 Help  F3 View  F4 Edit  F5 Copy  F6 RenMov  F7 MkDir  F8 Delete  F10 Menu | "
                + statusSupplier.get();
        ColorPair color = context.palette().status();
        context.terminal().putString(bounds().x(), bounds().y(), fit(text, bounds().width()), color.foreground(), color.background());
    }

    @Override
    public boolean handleKey(KeyStroke key, CommandBus commandBus) {
        if (key.keyType() == KeyType.F10) {
            return commandBus.dispatch(CommandId.MENU);
        }
        return false;
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
}
