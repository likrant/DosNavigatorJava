package org.dosnavigator.foundation;

import org.dosnavigator.command.CommandId;
import org.dosnavigator.terminal.Color;
import org.dosnavigator.terminal.KeyStroke;
import org.dosnavigator.terminal.KeyType;
import org.dosnavigator.terminal.TerminalSize;
import org.dosnavigator.testing.TerminalSnapshot;
import org.dosnavigator.testing.TestTerminalSurface;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FoundationAppTest {
    @Test
    void startupRendersPlatformIndependentFrameAndStatusLine() {
        TestTerminalSurface terminal = new TestTerminalSurface(new TerminalSize(80, 25));
        FoundationApp app = new FoundationApp(terminal, LegacyLanguage.ENGLISH);

        app.renderFrame();

        TerminalSnapshot frame = terminal.snapshot();
        assertEquals("desktop", frame.activeView());
        assertEquals("File", text(frame, 3, 0, 4));
        assertEquals("F10", text(frame, 1, 24, 3));
        assertEquals(Color.YELLOW_BRIGHT, frame.rows().get(24).cells().get(1).fg());
        assertEquals(Color.DARK_BLUE, frame.rows().get(12).cells().get(2).bg());
    }

    @Test
    void f10MnemonicAndEscFollowMenuAndSubmenuStateTransitions() {
        TestTerminalSurface terminal = new TestTerminalSurface(new TerminalSize(80, 25));
        FoundationApp app = new FoundationApp(terminal, LegacyLanguage.RUSSIAN);

        app.dispatchKey(KeyStroke.of(KeyType.F10));
        app.renderFrame();
        assertTrue(app.menuOpen());
        assertEquals(CommandId.MENU, app.lastCommand());
        assertEquals("menu:Файл", terminal.snapshot().activeView());
        assertEquals(Color.YELLOW_BRIGHT, terminal.snapshot().rows().get(0).cells().get(3).fg());

        app.dispatchKey(KeyStroke.character('с'));
        app.renderFrame();
        assertEquals("menu:Смотреть", terminal.snapshot().activeView());
        assertEquals('╔', terminal.snapshot().rows().get(1).cells().get(24).ch());

        app.dispatchKey(KeyStroke.of(KeyType.Escape));
        app.renderFrame();
        assertEquals("menu:Файл", terminal.snapshot().activeView());

        app.dispatchKey(KeyStroke.of(KeyType.Escape));
        assertFalse(app.menuOpen());
        assertEquals(CommandId.CANCEL, app.lastCommand());
    }

    @Test
    void languageMnemonicMenuCommandAndModalDialogShareCommandPath() {
        TestTerminalSurface terminal = new TestTerminalSurface(new TerminalSize(80, 25));
        FoundationApp app = new FoundationApp(terminal, LegacyLanguage.RUSSIAN);

        app.dispatchKey(KeyStroke.of(KeyType.F10));
        app.dispatchKey(KeyStroke.character('н'));
        app.dispatchKey(KeyStroke.character('с'));
        app.renderFrame();

        TerminalSnapshot dialog = terminal.snapshot();
        assertEquals(CommandId.SYSTEM_SETUP, app.lastCommand());
        assertTrue(app.modalOpen());
        assertEquals("dialog:dlgSystemSetup", dialog.activeView());
        assertEquals(List.of("desktop", "dlgSystemSetup"), app.zOrder());
        assertEquals('╔', dialog.rows().get(8).cells().get(12).ch());
        assertEquals("Системные установки", text(dialog, 14, 8, 20));
        assertEquals(Color.CYAN, dialog.rows().get(8).cells().get(12).fg());

        app.dispatchKey(KeyStroke.of(KeyType.Escape));
        app.renderFrame();
        assertFalse(app.modalOpen());
        assertEquals("desktop", terminal.snapshot().activeView());
        assertEquals(List.of("desktop"), app.zOrder());
    }

    @Test
    void enterClosesModalAndF10IsTheSameMenuCommandInEveryLanguage() {
        TestTerminalSurface terminal = new TestTerminalSurface(new TerminalSize(80, 25));
        FoundationApp app = new FoundationApp(terminal, LegacyLanguage.ENGLISH);

        app.dispatchKey(KeyStroke.of(KeyType.F10));
        app.dispatchKey(KeyStroke.character('o'));
        app.dispatchKey(KeyStroke.character('s'));
        assertEquals(CommandId.SYSTEM_SETUP, app.lastCommand());
        assertTrue(app.modalOpen());

        app.dispatchKey(KeyStroke.of(KeyType.Enter));
        assertEquals(CommandId.OK, app.lastCommand());
        assertFalse(app.modalOpen());
    }

    private static String text(TerminalSnapshot frame, int x, int y, int length) {
        return frame.rows().get(y).text().substring(x, x + length).trim();
    }
}
