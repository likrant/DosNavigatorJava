package org.dosnavigator.app;

import org.dosnavigator.terminal.TerminalSize;
import org.dosnavigator.ui.Box;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AppLayoutTest {
    @Test
    void calculatesClassicTwoPanelLayout() {
        AppLayout layout = AppLayout.calculate(new TerminalSize(80, 25));

        assertEquals(new Box(0, 0, 80, 25), layout.root());
        assertEquals(new Box(0, 0, 80, 1), layout.menuBar());
        assertEquals(new Box(0, 1, 80, 23), layout.desktop());
        assertEquals(new Box(0, 1, 40, 23), layout.leftPanel());
        assertEquals(new Box(40, 1, 40, 23), layout.rightPanel());
        assertEquals(new Box(0, 24, 80, 1), layout.statusLine());
    }

    @Test
    void keepsUsableLayoutForTinyTerminals() {
        AppLayout layout = AppLayout.calculate(new TerminalSize(1, 1));

        assertEquals(new Box(0, 0, 1, 3), layout.root());
        assertEquals(new Box(0, 1, 1, 1), layout.leftPanel());
        assertEquals(new Box(1, 1, 0, 1), layout.rightPanel());
        assertEquals(new Box(0, 2, 1, 1), layout.statusLine());
    }
}
