package org.dosnavigator.tui;

import org.dosnavigator.terminal.TerminalSurface;
import org.dosnavigator.ui.ColorPalette;

public record RenderContext(TerminalSurface terminal, ColorPalette palette) {
}
