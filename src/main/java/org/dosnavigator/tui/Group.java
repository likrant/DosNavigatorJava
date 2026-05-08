package org.dosnavigator.tui;

import org.dosnavigator.command.CommandBus;
import org.dosnavigator.terminal.KeyStroke;
import org.dosnavigator.ui.Box;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Group extends View {
    private final List<View> children = new ArrayList<>();
    private View current;

    public Group(Box bounds) {
        super(bounds);
    }

    public final void add(View view) {
        view.setParent(this);
        children.add(view);
        if (current == null && view.focusable()) {
            setCurrent(view);
        }
        invalidate();
    }

    public final void remove(View view) {
        if (!children.remove(view)) {
            return;
        }
        view.setParent(null);
        if (current == view) {
            current = null;
            selectNext(true);
        }
        invalidate();
    }

    public final List<View> children() {
        return Collections.unmodifiableList(children);
    }

    public final View current() {
        return current;
    }

    public final void setCurrent(View view) {
        if (view != null && (!children.contains(view) || !view.focusable())) {
            return;
        }
        if (current == view) {
            return;
        }
        if (current != null) {
            current.setFocused(false);
        }
        current = view;
        if (current != null) {
            current.setFocused(true);
        }
        invalidate();
    }

    public boolean selectNext(boolean forwards) {
        List<View> focusable = children.stream()
                .filter(View::visible)
                .filter(View::focusable)
                .toList();
        if (focusable.isEmpty()) {
            return false;
        }

        int index = current == null ? -1 : focusable.indexOf(current);
        int delta = forwards ? 1 : -1;
        int next = Math.floorMod(index + delta, focusable.size());
        setCurrent(focusable.get(next));
        return true;
    }

    @Override
    protected void draw(RenderContext context) {
        for (View child : children) {
            child.render(context);
        }
    }

    @Override
    public boolean handleKey(KeyStroke key, CommandBus commandBus) {
        if (current != null && current.handleKey(key, commandBus)) {
            return true;
        }

        for (int i = children.size() - 1; i >= 0; i--) {
            View child = children.get(i);
            if (child != current && child.visible() && child.handleKey(key, commandBus)) {
                return true;
            }
        }
        return false;
    }
}
