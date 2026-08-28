package org.dosnavigator.testing;

import org.dosnavigator.terminal.Color;

import java.util.ArrayList;
import java.util.List;

public record TerminalSnapshot(int width, int height, List<Row> rows) {
    public static TerminalSnapshot from(TestTerminalSurface.Cell[][] buffer) {
        List<Row> rows = new ArrayList<>(buffer.length);
        for (TestTerminalSurface.Cell[] sourceRow : buffer) {
            List<Cell> cells = new ArrayList<>(sourceRow.length);
            StringBuilder text = new StringBuilder(sourceRow.length);
            for (TestTerminalSurface.Cell cell : sourceRow) {
                cells.add(new Cell(cell.character(), cell.foreground(), cell.background()));
                text.append(cell.character());
            }
            rows.add(new Row(text.toString(), List.copyOf(cells)));
        }
        return new TerminalSnapshot(buffer.length == 0 ? 0 : buffer[0].length, buffer.length, List.copyOf(rows));
    }

    public String text() {
        StringBuilder builder = new StringBuilder();
        for (int index = 0; index < rows.size(); index++) {
            if (index > 0) {
                builder.append(System.lineSeparator());
            }
            builder.append(rows.get(index).text());
        }
        return builder.toString();
    }

    public record Row(String text, List<Cell> cells) {
    }

    public record Cell(char ch, Color fg, Color bg) {
    }
}
