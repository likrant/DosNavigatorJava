package org.dosnavigator.tui;

import org.dosnavigator.terminal.TerminalDriver;
import org.dosnavigator.ui.ColorPalette;

public record RenderContext(TerminalDriver terminal, ColorPalette palette) {
}
