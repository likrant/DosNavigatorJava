package org.dosnavigator.testing;

import org.dosnavigator.terminal.Color;
import org.dosnavigator.terminal.TerminalSize;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TestTerminalSurfaceFrameTest {
    @Test
    void snapshotContainsCellsCursorAndActiveViewWithoutPixels() {
        TestTerminalSurface surface = new TestTerminalSurface(new TerminalSize(2, 1));
        surface.beginFrame();
        surface.putChar(0, 0, 'X', Color.YELLOW_BRIGHT, Color.DARK_BLUE);
        surface.setCursor(1, 0, true);
        surface.setActiveView("left-panel");
        surface.refresh();

        TerminalSnapshot frame = surface.snapshot();
        assertEquals(2, frame.width());
        assertEquals(1, frame.height());
        assertEquals('X', frame.rows().getFirst().cells().getFirst().ch());
        assertEquals(Color.YELLOW_BRIGHT, frame.rows().getFirst().cells().getFirst().fg());
        assertEquals(Color.DARK_BLUE, frame.rows().getFirst().cells().getFirst().bg());
        assertEquals(TerminalSnapshot.Attributes.NONE, frame.rows().getFirst().cells().getFirst().attributes());
        assertEquals(new TerminalSnapshot.Cursor(1, 0, true), frame.cursor());
        assertEquals("left-panel", frame.activeView());
    }
}
