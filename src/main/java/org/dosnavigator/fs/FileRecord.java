package org.dosnavigator.fs;

import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Instant;

public record FileRecord(
        Path path,
        String name,
        boolean parentEntry,
        boolean directory,
        boolean hidden,
        boolean system,
        long size,
        FileTime modifiedTime,
        FileTime creationTime,
        FileTime accessTime
) {
    private static final FileTime EPOCH = FileTime.from(Instant.EPOCH);

    public static FileRecord parent(Path path) {
        return new FileRecord(path, "..", true, true, false, false, 0L, EPOCH, EPOCH, EPOCH);
    }

    public String displayName(boolean longNames) {
        if (parentEntry) {
            return "[..]";
        }
        if (directory) {
            return "[" + name + "]";
        }
        return longNames ? name : shortName();
    }

    public String extension() {
        int dot = name.lastIndexOf('.');
        if (dot <= 0 || dot == name.length() - 1) {
            return "";
        }
        return name.substring(dot + 1);
    }

    public String attrs() {
        StringBuilder builder = new StringBuilder(4);
        builder.append(directory ? 'D' : '-');
        builder.append(hidden ? 'H' : '-');
        builder.append(system ? 'S' : '-');
        builder.append(parentEntry ? 'P' : '-');
        return builder.toString();
    }

    private String shortName() {
        String base = name;
        String ext = "";
        int dot = name.lastIndexOf('.');
        if (dot > 0) {
            base = name.substring(0, dot);
            ext = name.substring(dot + 1);
        }
        if (base.length() <= 8 && ext.length() <= 3) {
            return name;
        }
        String shortBase = base.length() <= 8 ? base : base.substring(0, 6) + "~1";
        String shortExt = ext.length() <= 3 ? ext : ext.substring(0, 3);
        return shortExt.isBlank() ? shortBase : shortBase + "." + shortExt;
    }
}
