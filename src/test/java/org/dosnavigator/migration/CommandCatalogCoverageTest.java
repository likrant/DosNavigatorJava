package org.dosnavigator.migration;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommandCatalogCoverageTest {
    private static final Path CATALOG = Path.of("docs", "porting", "command-key-catalog.tsv");
    private static final Path COMMANDS = Path.of("DosNavigator-master", "commands.pas");
    private static final Charset DOS_OEM = Charset.forName("IBM866");
    private static final Pattern DECLARATION = Pattern.compile("^\\s*(c[m]|k[b])(?<name>[A-Za-z0-9_]+)\\s*=\\s*(?<value>[^;]+);", Pattern.CASE_INSENSITIVE);
    private static final List<String> STATUSES = List.of(
            "direct port", "adapter", "prototype to replace", "keep as test infrastructure", "remove after replacement"
    );

    @Test
    void everyDiscoveredPascalCommandAndKeyHasStatusAndTraceability() throws IOException {
        assertTrue(Files.isRegularFile(CATALOG), "Run scripts/generate-phase0-command-catalog.ps1 and commit its output.");
        Map<String, CatalogRow> catalog = readCatalog();
        List<String> missing = Files.readAllLines(COMMANDS, DOS_OEM).stream()
                .map(DECLARATION::matcher)
                .filter(Matcher::matches)
                .map(matcher -> (matcher.group(1).equalsIgnoreCase("cm") ? "command:" : "key:") + matcher.group(1) + matcher.group("name"))
                .filter(identity -> !catalog.containsKey(identity))
                .toList();

        assertTrue(missing.isEmpty(), () -> "Uncatalogued Commands.pas declarations: " + missing);
        assertFalse(catalog.values().stream().filter(CatalogRow::isCommandOrKey).anyMatch(row -> !STATUSES.contains(row.status())),
                "Every catalogued command/key needs one of the approved migration statuses.");
        assertFalse(catalog.values().stream().filter(CatalogRow::isCommandOrKey).anyMatch(row -> row.context().isBlank() || row.note().isBlank()),
                "Every catalogued command/key needs Pascal context and a traceability note.");
        assertFalse(catalog.values().stream().filter(CatalogRow::isCommandOrKey).anyMatch(row -> row.javaAnalog().isBlank()),
                "Every catalogued command/key needs an explicit Java analogue or pending-port marker.");
    }

    private static Map<String, CatalogRow> readCatalog() throws IOException {
        Map<String, CatalogRow> rows = new HashMap<>();
        List<String> lines = Files.readAllLines(CATALOG);
        for (String line : lines.subList(1, lines.size())) {
            String[] fields = line.split("\\t", -1);
            if (fields.length != 9) {
                throw new IOException("Malformed catalog row: " + line);
            }
            CatalogRow row = new CatalogRow(fields[0], fields[1], fields[5], fields[6], fields[7], fields[8]);
            if (row.isCommandOrKey()) {
                rows.put(row.kind() + ":" + row.pascalName(), row);
            }
        }
        return rows;
    }

    private record CatalogRow(String kind, String pascalName, String context, String status, String javaAnalog, String note) {
        boolean isCommandOrKey() {
            return kind.equals("command") || kind.equals("key");
        }
    }
}
