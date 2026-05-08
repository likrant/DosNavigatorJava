package org.dosnavigator.terminal;

public record MouseEvent(int x, int y, int button, MouseAction action) {
}
