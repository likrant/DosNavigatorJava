package org.dosnavigator.testing;

import org.dosnavigator.app.DosNavigatorApp;
import org.dosnavigator.terminal.Color;
import org.dosnavigator.terminal.TerminalSize;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Mandatory startup visual oracle with no mismatch tolerance. */
class LegacyVisualParityTest {
    private static final TerminalSize LEGACY_GRID = new TerminalSize(160, 50);

    @TempDir
    Path fixture;

    @Test
    void startupFrameEqualsCheckedInLegacyClientScreenshot() throws Exception {
        Path left = Files.createDirectories(fixture.resolve("left"));
        Path right = Files.createDirectories(fixture.resolve("right"));
        populateLegacyStartupFixture(left);
        populateLegacyStartupFixture(right);
        TestTerminalSurface terminal = new TestTerminalSurface(LEGACY_GRID);
        try (DosNavigatorApp app = new DosNavigatorApp(terminal, left, right, "C:\\DNW", "C:\\DNW")) {
            app.start();
            // The checked-in capture has the right panel focused at startup.
            app.dispatchKey(org.dosnavigator.terminal.KeyStroke.of(org.dosnavigator.terminal.KeyType.Tab));
            app.renderFrame();
        }

        BufferedImage javaFrame = render(terminal.snapshot());
        LegacyScreenshotComparator.Result result = LegacyScreenshotComparator.compareClient(
                Path.of("tests", "legacy", "ui-reference", "screenshots", "forms", "00-startup.png"),
                javaFrame,
                Path.of("target", "visual-parity", "00-startup"));

        assertEquals(0, result.differentPixels(),
                () -> "Legacy visual mismatch: " + result.differentPixels() + "/" + result.totalPixels()
                        + " pixels; see target/visual-parity/00-startup/diff.png");
    }

    private static void populateLegacyStartupFixture(Path root) throws Exception {
        for (String directory : new String[]{"COLORS", "DOC", "KIT"}) {
            Files.createDirectory(root.resolve(directory));
        }
        for (String file : new String[]{
                ".tmp", "dnv", "no_blink", "dn", "DN", "TETRIS", "file_id.diz", "ENGLISH", "RUSSIAN",
                "UKRAIN", "SMARTPAD", "DN", "DN", "DN", "dn", "dnhgl", "dn", "DN", "DN (1)", "DN (2)",
                "english", "russian", "ukrain", "dn2w", "dnini", "dn", "descript", "ENGLISH", "RUSSIAN",
                "UKRAIN", "dn", "dn", "dn", "dnlt", "dn"
        }) {
            Path candidate = root.resolve(file);
            if (Files.exists(candidate)) {
                candidate = root.resolve(file + ".dup" + System.nanoTime());
            }
            Files.writeString(candidate, "DN");
        }
    }

    private static BufferedImage render(TerminalSnapshot frame) {
        int cellWidth = 8;
        int cellHeight = 14;
        BufferedImage image = new BufferedImage(frame.width() * cellWidth, frame.height() * cellHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        try {
            graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
            graphics.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
            FontMetrics metrics = graphics.getFontMetrics();
            int baseline = metrics.getAscent() + 1;
            for (int y = 0; y < frame.height(); y++) {
                for (int x = 0; x < frame.width(); x++) {
                    TerminalSnapshot.Cell cell = frame.rows().get(y).cells().get(x);
                    graphics.setColor(toAwt(cell.bg()));
                    graphics.fillRect(x * cellWidth, y * cellHeight, cellWidth, cellHeight);
                    graphics.setColor(toAwt(cell.fg()));
                    graphics.drawString(String.valueOf(cell.ch()), x * cellWidth + 1, y * cellHeight + baseline);
                }
            }
        } finally {
            graphics.dispose();
        }
        return image;
    }

    private static java.awt.Color toAwt(Color color) {
        return switch (color) {
            case BLACK -> new java.awt.Color(0x00, 0x00, 0x00);
            case BLUE -> new java.awt.Color(0x00, 0x00, 0xAA);
            case DARK_BLUE -> new java.awt.Color(0x00, 0x00, 0x80);
            case CYAN -> new java.awt.Color(0x00, 0xFF, 0xFF);
            case CYAN_BRIGHT -> new java.awt.Color(0x55, 0xFF, 0xFF);
            case GREEN -> new java.awt.Color(0x00, 0xFF, 0x00);
            case WHITE -> new java.awt.Color(0xC0, 0xC0, 0xC0);
            case WHITE_BRIGHT -> new java.awt.Color(0xFF, 0xFF, 0xFF);
            case YELLOW_BRIGHT -> new java.awt.Color(0xFF, 0xFF, 0x00);
        };
    }
}
