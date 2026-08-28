package org.dosnavigator.testing;

import org.dosnavigator.app.DosNavigatorApp;
import org.dosnavigator.terminal.Color;
import org.dosnavigator.terminal.KeyModifier;
import org.dosnavigator.terminal.KeyStroke;
import org.dosnavigator.terminal.KeyType;
import org.dosnavigator.terminal.TerminalSize;

import javax.imageio.ImageIO;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class IsolatedVisualHarnessMain {
    private static final TerminalSize SCREEN = new TerminalSize(80, 25);
    private static final Instant FIXED_TIME = Instant.parse("2026-08-28T09:30:00Z");
    private static final DateTimeFormatter RUN_ID_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss").withZone(java.time.ZoneOffset.UTC);

    public static void main(String[] args) throws Exception {
        Path artifactsRoot = Path.of("target", "isolated-harness", RUN_ID_FORMAT.format(Instant.now()));
        Files.createDirectories(artifactsRoot);
        Path sandboxRoot = artifactsRoot.resolve("sandbox");
        Path fixturesRoot = sandboxRoot.resolve("fixtures");
        Path leftRoot = fixturesRoot.resolve("left");
        Path rightRoot = fixturesRoot.resolve("right");
        createFixtureTree(leftRoot, rightRoot);

        TestTerminalSurface terminal = new TestTerminalSurface(SCREEN);
        List<ScenarioStep> steps = new ArrayList<>();

        try (DosNavigatorApp app = new DosNavigatorApp(terminal, leftRoot, rightRoot)) {
            app.start();
            capture(app, terminal, steps, artifactsRoot, "00-startup",
                    "Первый кадр после запуска в изолированном дереве. Активна левая панель, меню скрыто.");

            press(app, terminal, steps, artifactsRoot, "01-menu-open", KeyStroke.of(KeyType.F10),
                    "F10 открывает верхнее меню. Фокус остается в приложении, виден пункт Left.");
            press(app, terminal, steps, artifactsRoot, "02-menu-right-file", KeyStroke.of(KeyType.ArrowRight),
                    "Стрелка вправо в меню переключает выделение на следующий пункт: File.");
            press(app, terminal, steps, artifactsRoot, "03-menu-right-commands", KeyStroke.of(KeyType.ArrowRight),
                    "Повторная стрелка вправо переводит выделение на Commands.");
            press(app, terminal, steps, artifactsRoot, "04-menu-close", KeyStroke.of(KeyType.Escape),
                    "Esc закрывает меню и возвращает обычную навигацию по панелям.");

            press(app, terminal, steps, artifactsRoot, "05-panel-down", KeyStroke.of(KeyType.ArrowDown),
                    "Стрелка вниз в левой панели переводит курсор с [..] на первый каталог [apps].");
            press(app, terminal, steps, artifactsRoot, "06-panel-right-column", KeyStroke.of(KeyType.ArrowRight),
                    "Стрелка вправо в brief-режиме перескакивает в следующую колонку списка.");
            press(app, terminal, steps, artifactsRoot, "07-panel-left-column", KeyStroke.of(KeyType.ArrowLeft),
                    "Стрелка влево возвращает курсор в предыдущую колонку.");
            press(app, terminal, steps, artifactsRoot, "08-panel-enter-directory", KeyStroke.of(KeyType.Enter),
                    "Enter на каталоге открывает его содержимое внутри активной панели.");
            press(app, terminal, steps, artifactsRoot, "09-panel-backspace-parent", KeyStroke.of(KeyType.Backspace),
                    "Backspace возвращает текущую панель в родительский каталог.");
            press(app, terminal, steps, artifactsRoot, "10-panel-tab-right", KeyStroke.of(KeyType.Tab),
                    "Tab переключает активную панель. Фокус уходит на правую сторону.");
            press(app, terminal, steps, artifactsRoot, "11-right-panel-down", KeyStroke.of(KeyType.ArrowDown),
                    "Стрелка вниз в правой панели переводит курсор на каталог [archive].");
            press(app, terminal, steps, artifactsRoot, "12-right-panel-enter", KeyStroke.of(KeyType.Enter),
                    "Enter в правой панели открывает каталог archive.");
            press(app, terminal, steps, artifactsRoot, "13-quit", KeyStroke.character('q', KeyModifier.CTRL),
                    "Ctrl+Q завершает приложение. Обычная q в текущей реализации используется как quick search и не закрывает DN.");
        }

        writeReport(artifactsRoot, steps);
        System.out.println("Artifacts: " + artifactsRoot.toAbsolutePath());
    }

    private static void press(
            DosNavigatorApp app,
            TestTerminalSurface terminal,
            List<ScenarioStep> steps,
            Path artifactsRoot,
            String id,
            KeyStroke key,
            String behavior
    ) throws IOException {
        app.dispatchKey(key);
        capture(app, terminal, steps, artifactsRoot, id, behavior);
    }

    private static void capture(
            DosNavigatorApp app,
            TestTerminalSurface terminal,
            List<ScenarioStep> steps,
            Path artifactsRoot,
            String id,
            String behavior
    ) throws IOException {
        app.renderFrame();
        TerminalSnapshot snapshot = terminal.snapshot();
        Path png = artifactsRoot.resolve(id + ".png");
        Path txt = artifactsRoot.resolve(id + ".txt");
        Path json = artifactsRoot.resolve(id + ".json");
        renderSnapshot(snapshot, png);
        Files.writeString(txt, snapshot.text(), StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        Files.writeString(json, toJson(id, snapshot), StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        steps.add(new ScenarioStep(id, behavior, png, txt, summarize(snapshot)));
    }

    private static void createFixtureTree(Path leftRoot, Path rightRoot) throws IOException {
        Files.createDirectories(leftRoot.resolve("apps"));
        Files.createDirectories(leftRoot.resolve("docs"));
        Files.createDirectories(leftRoot.resolve("games"));
        Files.createDirectories(rightRoot.resolve("archive"));
        Files.createDirectories(rightRoot.resolve("backup"));
        Files.createDirectories(rightRoot.resolve("incoming"));

        writeFile(leftRoot.resolve("apps").resolve("DNJ.BAT"), "@echo off\r\n");
        writeFile(leftRoot.resolve("docs").resolve("README.TXT"), "Dos Navigator Java test docs\r\n");
        writeFile(leftRoot.resolve("games").resolve("TETRIS.CFG"), "speed=fast\r\n");

        for (int index = 0; index < 24; index++) {
            writeFile(leftRoot.resolve(String.format(Locale.ROOT, "FILE%02d.TXT", index)), "fixture " + index + "\r\n");
        }
        writeFile(leftRoot.resolve("AUTOEXEC.BAT"), "prompt $p$g\r\n");
        writeFile(leftRoot.resolve("LONGFILENAME-DEMO.TXT"), "long name\r\n");

        writeFile(rightRoot.resolve("archive").resolve("pack.zip"), "PK\u0003\u0004");
        writeFile(rightRoot.resolve("backup").resolve("notes.txt"), "backup notes\r\n");
        writeFile(rightRoot.resolve("incoming").resolve("todo.txt"), "todo\r\n");
        writeFile(rightRoot.resolve("README.NFO"), "right panel root\r\n");
        writeFile(rightRoot.resolve("Z-LAST.LOG"), "tail\r\n");
    }

    private static void writeFile(Path path, String content) throws IOException {
        Files.createDirectories(path.getParent());
        Files.writeString(path, content, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        Files.setLastModifiedTime(path, FileTime.from(FIXED_TIME));
    }

    private static void renderSnapshot(TerminalSnapshot snapshot, Path output) throws IOException {
        int cellWidth = 10;
        int cellHeight = 18;
        BufferedImage image = new BufferedImage(snapshot.width() * cellWidth, snapshot.height() * cellHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        try {
            graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
            graphics.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
            FontMetrics metrics = graphics.getFontMetrics();
            int baseline = metrics.getAscent() + 2;
            for (int y = 0; y < snapshot.height(); y++) {
                TerminalSnapshot.Row row = snapshot.rows().get(y);
                for (int x = 0; x < snapshot.width(); x++) {
                    TerminalSnapshot.Cell cell = row.cells().get(x);
                    graphics.setColor(toAwt(cell.bg()));
                    graphics.fillRect(x * cellWidth, y * cellHeight, cellWidth, cellHeight);
                    graphics.setColor(toAwt(cell.fg()));
                    graphics.drawString(String.valueOf(cell.ch()), x * cellWidth + 1, y * cellHeight + baseline);
                }
            }
        } finally {
            graphics.dispose();
        }
        ImageIO.write(image, "png", output.toFile());
    }

    private static java.awt.Color toAwt(Color color) {
        return switch (color) {
            case BLACK -> new java.awt.Color(0x00, 0x00, 0x00);
            case BLUE, DARK_BLUE -> new java.awt.Color(0x00, 0x00, 0xAA);
            case CYAN -> new java.awt.Color(0x00, 0xAA, 0xAA);
            case CYAN_BRIGHT -> new java.awt.Color(0x55, 0xFF, 0xFF);
            case GREEN -> new java.awt.Color(0x00, 0xAA, 0x00);
            case WHITE -> new java.awt.Color(0xAA, 0xAA, 0xAA);
            case WHITE_BRIGHT -> new java.awt.Color(0xFF, 0xFF, 0xFF);
            case YELLOW_BRIGHT -> new java.awt.Color(0xFF, 0xFF, 0x55);
        };
    }

    private static String summarize(TerminalSnapshot snapshot) {
        String menu = snapshot.rows().getFirst().text().trim();
        String left = snapshot.rows().get(2).text().stripTrailing();
        String right = snapshot.rows().get(2).text().length() > 40
                ? snapshot.rows().get(2).text().substring(40).stripTrailing()
                : "";
        String status = snapshot.rows().get(snapshot.height() - 1).text().trim();
        return "menu=[" + menu + "], leftRow=[" + left + "], rightRow=[" + right + "], status=[" + status + "]";
    }

    private static String toJson(String snapshotId, TerminalSnapshot snapshot) {
        StringBuilder builder = new StringBuilder();
        builder.append("{\n");
        builder.append("  \"snapshotId\": \"").append(snapshotId).append("\",\n");
        builder.append("  \"screen\": { \"width\": ").append(snapshot.width()).append(", \"height\": ").append(snapshot.height()).append(" },\n");
        builder.append("  \"rows\": [\n");
        for (int y = 0; y < snapshot.rows().size(); y++) {
            TerminalSnapshot.Row row = snapshot.rows().get(y);
            builder.append("    { \"y\": ").append(y).append(", \"text\": \"").append(escape(row.text())).append("\", \"cells\": [");
            for (int x = 0; x < row.cells().size(); x++) {
                TerminalSnapshot.Cell cell = row.cells().get(x);
                if (x > 0) {
                    builder.append(", ");
                }
                builder.append("{\"x\": ").append(x)
                        .append(", \"ch\": \"").append(escape(String.valueOf(cell.ch()))).append("\"")
                        .append(", \"fg\": \"").append(cell.fg().name()).append("\"")
                        .append(", \"bg\": \"").append(cell.bg().name()).append("\"}");
            }
            builder.append("] }");
            if (y + 1 < snapshot.rows().size()) {
                builder.append(',');
            }
            builder.append('\n');
        }
        builder.append("  ]\n");
        builder.append("}\n");
        return builder.toString();
    }

    private static String escape(String text) {
        return text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n");
    }

    private static void writeReport(Path artifactsRoot, List<ScenarioStep> steps) throws IOException {
        StringBuilder builder = new StringBuilder();
        builder.append("# Isolated Java DN visual harness").append(System.lineSeparator()).append(System.lineSeparator());
        builder.append("Run date: 2026-08-28").append(System.lineSeparator());
        builder.append("Screen: 80x25").append(System.lineSeparator());
        builder.append("Artifacts directory: ").append(artifactsRoot.toAbsolutePath()).append(System.lineSeparator()).append(System.lineSeparator());
        builder.append("## Steps").append(System.lineSeparator()).append(System.lineSeparator());
        for (ScenarioStep step : steps) {
            builder.append("- ").append(step.id()).append(": ").append(step.behavior()).append(System.lineSeparator());
            builder.append("  PNG: ").append(step.png().getFileName()).append(System.lineSeparator());
            builder.append("  TXT: ").append(step.textFile().getFileName()).append(System.lineSeparator());
            builder.append("  Summary: ").append(step.summary()).append(System.lineSeparator());
        }
        Files.writeString(artifactsRoot.resolve("report.md"), builder.toString(), StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    private record ScenarioStep(String id, String behavior, Path png, Path textFile, String summary) {
    }
}
