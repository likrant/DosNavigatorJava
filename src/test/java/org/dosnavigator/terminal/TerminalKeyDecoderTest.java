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
}
