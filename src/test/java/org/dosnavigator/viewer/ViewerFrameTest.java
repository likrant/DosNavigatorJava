package org.dosnavigator.viewer;

import org.dosnavigator.terminal.Color;
import org.dosnavigator.terminal.TerminalSize;
import org.dosnavigator.testing.TerminalSnapshot;
import org.dosnavigator.testing.TestTerminalSurface;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ViewerFrameTest {
    @TempDir
    Path fixture;

    @Test
    void cp866SearchAndViewerFrameHaveStableCellLevelContract() throws Exception {
        Charset cp866 = Charset.forName("IBM866");
        Path sample = fixture.resolve("sample.txt");
        Files.writeString(sample, "first\nПривет DN\nlast", cp866);
        ViewerDocument document = ViewerDocument.open(sample, cp866);
        TestTerminalSurface terminal = new TestTerminalSurface(new TerminalSize(30, 6));

        new ViewerFrame().render(terminal, document, "sample.txt", 0);
        TerminalSnapshot frame = terminal.snapshot();

        assertEquals(1, document.findNext("Привет", -1));
        assertEquals("viewer:text", frame.activeView());
        assertEquals('s', frame.rows().get(0).cells().get(1).ch());
        assertEquals(Color.YELLOW_BRIGHT, frame.rows().get(0).cells().get(1).fg());
        assertEquals('F', frame.rows().get(5).cells().get(0).ch());
        assertEquals(Color.CYAN, frame.rows().get(5).cells().get(0).bg());
    }
}
