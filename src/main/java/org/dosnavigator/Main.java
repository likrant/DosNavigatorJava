package org.dosnavigator;

import org.dosnavigator.app.DosNavigatorApp;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.nio.file.Path;
import java.util.concurrent.Callable;

@Command(
        name = "dnj",
        mixinStandardHelpOptions = true,
        version = "Dos Navigator Java 0.1.0",
        description = "Text-mode file manager inspired by Dos Navigator."
)
public final class Main implements Callable<Integer> {
    @Parameters(index = "0", arity = "0..1", description = "Initial left panel directory.")
    private Path leftDirectory = Path.of(System.getProperty("user.dir"));

    @Parameters(index = "1", arity = "0..1", description = "Initial right panel directory.")
    private Path rightDirectory = Path.of(System.getProperty("user.home"));

    public static void main(String[] args) {
        int exitCode = new CommandLine(new Main()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() throws Exception {
        try (DosNavigatorApp app = new DosNavigatorApp(leftDirectory, rightDirectory)) {
            app.run();
        }
        return 0;
    }
}
