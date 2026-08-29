package org.dosnavigator.app;

import org.dosnavigator.terminal.Color;
import org.dosnavigator.terminal.KeyStroke;
import org.dosnavigator.terminal.KeyType;
import org.dosnavigator.terminal.TerminalSize;
import org.dosnavigator.testing.TerminalSnapshot;
import org.dosnavigator.testing.TestTerminalSurface;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Initial flpanel/flpanelx visual slice. The fixture is disposable and mirrors
 * the 160x50 geometry declared by tests/legacy/ui-reference/screen-index.json.
 */
class TwoPanelFrameTest {
    @TempDir
    Path tempDir;

    @Test
    void rendersTwoPanelFrameWithLegacyGeometryPaletteAndFocus() throws Exception {
        Path left = fixture("left", "alpha", "beta.txt");
        Path right = fixture("right", "archive", "readme.txt");
        TestTerminalSurface terminal = new TestTerminalSurface(new TerminalSize(160, 50));

        try (DosNavigatorApp app = new DosNavigatorApp(terminal, left, right)) {
            app.start();
            app.renderFrame();
            TerminalSnapshot startup = terminal.snapshot();

            assertEquals(160, startup.width());
            assertEquals(50, startup.height());
            assertEquals("panel:left", startup.activeView());
            assertEquals('╔', cell(startup, 0, 0).ch());
            assertEquals('╔', cell(startup, 80, 0).ch());
            assertEquals(Color.CYAN, cell(startup, 0, 0).fg());
            assertEquals(Color.CYAN, cell(startup, 80, 0).fg());
            assertTrue(startup.text().contains("alpha"));
            assertTrue(startup.text().contains("archive"));

            app.dispatchKey(KeyStroke.of(KeyType.Tab));
            app.renderFrame();
            TerminalSnapshot rightFocused = terminal.snapshot();
            assertEquals("panel:right", rightFocused.activeView());
            assertEquals(Color.CYAN, cell(rightFocused, 80, 0).fg());

            app.dispatchKey(KeyStroke.of(KeyType.Tab));
            app.dispatchKey(KeyStroke.of(KeyType.ArrowDown));
            app.renderFrame();
            TerminalSnapshot selected = terminal.snapshot();
            assertEquals("panel:left", selected.activeView());
            assertEquals(Color.CYAN, cell(selected, 1, 3).bg());

            app.dispatchKey(KeyStroke.of(KeyType.Enter));
            app.renderFrame();
            assertTrue(terminal.snapshot().text().contains("inside.txt"));

            app.dispatchKey(KeyStroke.of(KeyType.Backspace));
            app.renderFrame();
            assertTrue(terminal.snapshot().text().contains("beta.txt"));
        }
    }

    @Test
    void legacyReferenceDeclaresTheSameFrameGeometryAndCorePalette() throws IOException {
        String index = Files.readString(Path.of("tests", "legacy", "ui-reference", "screen-index.json"));

        assertTrue(index.matches("(?s).*\"columns\"\\s*:\\s*160.*"));
        assertTrue(index.matches("(?s).*\"rows\"\\s*:\\s*50.*"));
        assertTrue(index.matches("(?s).*\"navy\"\\s*:\\s*\"#000080\".*"));
        assertTrue(index.matches("(?s).*\"cyan\"\\s*:\\s*\"#00FFFF\".*"));
    }

    private Path fixture(String name, String directory, String file) throws IOException {
        Path root = Files.createDirectories(tempDir.resolve(name));
        Files.createDirectories(root.resolve(directory));
        Files.writeString(root.resolve(directory).resolve("inside.txt"), "fixture");
        Files.writeString(root.resolve(file), "fixture");
        Files.setLastModifiedTime(root.resolve(file), java.nio.file.attribute.FileTime.from(Instant.parse("2026-08-28T09:30:00Z")));
        return root;
    }

    private static TerminalSnapshot.Cell cell(TerminalSnapshot snapshot, int x, int y) {
        return snapshot.rows().get(y).cells().get(x);
    }
}
