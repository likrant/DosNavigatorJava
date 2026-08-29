package org.dosnavigator.testing;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Pixel evidence comparator for a legacy DN client image and a rendered Java Frame. */
final class LegacyScreenshotComparator {
    private LegacyScreenshotComparator() {
    }

    static Result compareClient(Path legacyPng, BufferedImage actual, Path artifacts) throws IOException {
        BufferedImage legacyWindow = ImageIO.read(legacyPng.toFile());
        if (legacyWindow == null) {
            throw new IOException("Not a PNG: " + legacyPng);
        }
        // screen-index.json: logical DN client starts after the native window border.
        BufferedImage expected = legacyWindow.getSubimage(8, 31, 1280, 700);
        if (actual.getWidth() != expected.getWidth() || actual.getHeight() != expected.getHeight()) {
            throw new AssertionError("Java client is " + actual.getWidth() + "x" + actual.getHeight()
                    + ", legacy client is " + expected.getWidth() + "x" + expected.getHeight());
        }

        Files.createDirectories(artifacts);
        ImageIO.write(actual, "png", artifacts.resolve("java.png").toFile());
        ImageIO.write(expected, "png", artifacts.resolve("legacy-client.png").toFile());
        BufferedImage diff = new BufferedImage(expected.getWidth(), expected.getHeight(), BufferedImage.TYPE_INT_RGB);
        long different = 0;
        for (int y = 0; y < expected.getHeight(); y++) {
            for (int x = 0; x < expected.getWidth(); x++) {
                int before = expected.getRGB(x, y);
                int after = actual.getRGB(x, y);
                if (before == after) {
                    diff.setRGB(x, y, before);
                } else {
                    different++;
                    diff.setRGB(x, y, Color.MAGENTA.getRGB());
                }
            }
        }
        ImageIO.write(diff, "png", artifacts.resolve("diff.png").toFile());
        return new Result(expected.getWidth(), expected.getHeight(), different);
    }

    record Result(int width, int height, long differentPixels) {
        long totalPixels() {
            return (long) width * height;
        }
    }
}
