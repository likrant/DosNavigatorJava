package org.dosnavigator.tui;

import org.dosnavigator.ui.Box;
import org.dosnavigator.ui.ColorPair;

public final class Desktop extends Group {
    public Desktop(Box bounds) {
        super(bounds);
    }

    @Override
    protected void draw(RenderContext context) {
        if (selfInvalid()) {
            Box bounds = bounds();
            ColorPair desktop = context.palette().desktop();
            String line = " ".repeat(Math.max(0, bounds.width()));
            for (int row = 0; row < bounds.height(); row++) {
                context.terminal().putString(bounds.x(), bounds.y() + row, line, desktop.foreground(), desktop.background());
            }
            for (View child : children()) {
                child.invalidate();
            }
        }
        super.draw(context);
    }
}
