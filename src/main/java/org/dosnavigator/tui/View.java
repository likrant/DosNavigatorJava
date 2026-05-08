package org.dosnavigator.tui;

import org.dosnavigator.command.CommandBus;
import org.dosnavigator.terminal.KeyStroke;
import org.dosnavigator.ui.Box;

public abstract class View {
    private Box bounds;
    private Group parent;
    private boolean visible = true;
    private boolean focused;
    private boolean focusable;
    private boolean invalid = true;
    private boolean selfInvalid = true;

    protected View(Box bounds) {
        this.bounds = bounds;
    }

    public final Box bounds() {
        return bounds;
    }

    public final void setBounds(Box bounds) {
        if (!this.bounds.equals(bounds)) {
            this.bounds = bounds;
            invalidate();
            onBoundsChanged();
        }
    }

    public final Group parent() {
        return parent;
    }

    final void setParent(Group parent) {
        this.parent = parent;
    }

    public final boolean visible() {
        return visible;
    }

    public final void setVisible(boolean visible) {
        if (this.visible != visible) {
            this.visible = visible;
            invalidate();
        }
    }

    public final boolean focused() {
        return focused;
    }

    public void setFocused(boolean focused) {
        if (this.focused != focused) {
            this.focused = focused;
            invalidate();
        }
    }

    public final boolean focusable() {
        return focusable;
    }

    protected final void setFocusable(boolean focusable) {
        this.focusable = focusable;
    }

    public final boolean invalid() {
        return invalid;
    }

    public void invalidate() {
        selfInvalid = true;
        markInvalid();
    }

    protected final void invalidatePartial() {
        markInvalid();
    }

    final void markInvalid() {
        invalid = true;
        if (parent != null) {
            parent.markInvalid();
        }
    }

    public final void validate() {
        invalid = false;
        selfInvalid = false;
    }

    protected final boolean selfInvalid() {
        return selfInvalid;
    }

    public final void render(RenderContext context) {
        if (!visible || !invalid) {
            return;
        }
        draw(context);
        validate();
    }

    protected void onBoundsChanged() {
    }

    protected abstract void draw(RenderContext context);

    public boolean handleKey(KeyStroke key, CommandBus commandBus) {
        return false;
    }
}
