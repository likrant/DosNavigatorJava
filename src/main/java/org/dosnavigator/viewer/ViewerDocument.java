package org.dosnavigator.viewer;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** Read-only source slice of FVIEWER.PAS and its CP866 boundary. */
public final class ViewerDocument {
    private final List<String> lines;
    private final Charset charset;

    private ViewerDocument(List<String> lines, Charset charset) {
        this.lines = List.copyOf(lines);
        this.charset = charset;
    }

    public static ViewerDocument open(Path path, Charset charset) throws IOException {
        return new ViewerDocument(Files.readAllLines(path, charset), charset);
    }

    public List<String> lines() {
        return lines;
    }

    public Charset charset() {
        return charset;
    }

    public int findNext(String needle, int afterLine) {
        for (int index = Math.max(0, afterLine + 1); index < lines.size(); index++) {
            if (lines.get(index).contains(needle)) {
                return index;
            }
        }
        return -1;
    }
}
