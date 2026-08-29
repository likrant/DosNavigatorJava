package org.dosnavigator.port.flpanel;

import org.dosnavigator.terminal.Color;
import org.dosnavigator.terminal.TerminalSize;
import org.dosnavigator.testing.TerminalSnapshot;
import org.dosnavigator.testing.TestTerminalSurface;
import org.dosnavigator.ui.Box;
import org.dosnavigator.ui.ColorPalette;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TFilePanelTest {
    @Test
    void directPortPanelKeepsLegacyBorderTitleCursorAndFooterCells() {
        TestTerminalSurface terminal = new TestTerminalSurface(new TerminalSize(80, 50));
        TFilePanel panel = new TFilePanel("C:\\DNW", List.of(
                new TFilePanel.TFileRecord("COLORS", "", true),
                new TFilePanel.TFileRecord("DN", "EXE", false)));
        panel.setCursor(1);
        terminal.beginFrame();
        panel.draw(terminal, new Box(0, 0, 80, 50), true, ColorPalette.dosNavigator());
        terminal.refresh();
        TerminalSnapshot frame = terminal.snapshot();

        assertEquals('╔', frame.rows().get(0).cells().get(0).ch());
        assertEquals('C', frame.rows().get(0).cells().get(3).ch());
        assertEquals('И', frame.rows().get(1).cells().get(38).ch());
        assertEquals(Color.CYAN, frame.rows().get(3).cells().get(1).bg());
        assertEquals('В', frame.rows().get(47).cells().get(32).ch());
    }
}
