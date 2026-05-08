package org.dosnavigator.terminal;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TerminalKeyDecoderTest {
    @Test
    void decodesFunctionKeysFromCommonAnsiForms() {
        assertEquals(KeyType.F1, TerminalKeyDecoder.decodeAnsiSequence("\033OP").keyType());
        assertEquals(KeyType.F4, TerminalKeyDecoder.decodeAnsiSequence("\033OS").keyType());
        assertEquals(KeyType.F10, TerminalKeyDecoder.decodeAnsiSequence("\033[21~").keyType());
        assertEquals(KeyType.F12, TerminalKeyDecoder.decodeAnsiSequence("\033[24~").keyType());
    }

    @Test
    void decodesNavigationAndEditingKeys() {
        assertEquals(KeyType.ArrowUp, TerminalKeyDecoder.decodeAnsiSequence("\033OA").keyType());
        assertEquals(KeyType.ArrowDown, TerminalKeyDecoder.decodeAnsiSequence("\033OB").keyType());
        assertEquals(KeyType.ArrowRight, TerminalKeyDecoder.decodeAnsiSequence("\033OC").keyType());
        assertEquals(KeyType.ArrowLeft, TerminalKeyDecoder.decodeAnsiSequence("\033OD").keyType());
        assertEquals(KeyType.Insert, TerminalKeyDecoder.decodeAnsiSequence("\033[2~").keyType());
        assertEquals(KeyType.Delete, TerminalKeyDecoder.decodeAnsiSequence("\033[3~").keyType());
        assertEquals(KeyType.PageUp, TerminalKeyDecoder.decodeAnsiSequence("\033[5~").keyType());
        assertEquals(KeyType.PageDown, TerminalKeyDecoder.decodeAnsiSequence("\033[6~").keyType());
    }

    @Test
    void decodesModifierParameter() {
        KeyStroke ctrlPageUp = TerminalKeyDecoder.decodeAnsiSequence("\033[5;5~");
        KeyStroke shiftTab = TerminalKeyDecoder.decodeAnsiSequence("\033[Z");

        assertEquals(KeyType.PageUp, ctrlPageUp.keyType());
        assertTrue(ctrlPageUp.hasModifier(KeyModifier.CTRL));
        assertEquals(KeyType.ShiftTab, shiftTab.keyType());
        assertTrue(shiftTab.hasModifier(KeyModifier.SHIFT));
    }

    @Test
    void decodesAltCharacter() {
        KeyStroke altX = TerminalKeyDecoder.decodeAnsiSequence("\033x");

        assertEquals(KeyType.Character, altX.keyType());
        assertEquals('x', altX.character());
        assertTrue(altX.hasModifier(KeyModifier.ALT));
    }

    @Test
    void decodesSgrMousePressAndWheel() {
        KeyStroke click = TerminalKeyDecoder.decodeAnsiSequence("\033[<0;12;7M");
        KeyStroke wheel = TerminalKeyDecoder.decodeAnsiSequence("\033[<65;12;7M");

        assertEquals(KeyType.Mouse, click.keyType());
        assertEquals(11, click.mouseEvent().x());
        assertEquals(6, click.mouseEvent().y());
        assertEquals(MouseAction.PRESS, click.mouseEvent().action());
        assertEquals(MouseAction.WHEEL_DOWN, wheel.mouseEvent().action());
    }

    @Test
    void decodesCtrlQAsModifiedCharacter() throws Exception {
        KeyStroke ctrlQ = TerminalKeyDecoder.decode(17, timeout -> -1);

        assertEquals(KeyType.Character, ctrlQ.keyType());
        assertEquals('q', ctrlQ.character());
        assertTrue(ctrlQ.hasModifier(KeyModifier.CTRL));
    }
}
