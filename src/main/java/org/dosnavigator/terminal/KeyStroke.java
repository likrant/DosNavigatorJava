package org.dosnavigator.terminal;

import java.util.EnumSet;
import java.util.Set;

public record KeyStroke(KeyType keyType, Character character, EnumSet<KeyModifier> modifiers) {
    public KeyStroke {
        modifiers = modifiers == null || modifiers.isEmpty()
                ? EnumSet.noneOf(KeyModifier.class)
                : EnumSet.copyOf(modifiers);
    }

    public static KeyStroke of(KeyType keyType) {
        return new KeyStroke(keyType, null, EnumSet.noneOf(KeyModifier.class));
    }

    public static KeyStroke of(KeyType keyType, KeyModifier modifier, KeyModifier... rest) {
        EnumSet<KeyModifier> modifiers = EnumSet.of(modifier, rest);
        return new KeyStroke(keyType, null, modifiers);
    }

    public static KeyStroke of(KeyType keyType, EnumSet<KeyModifier> modifiers) {
        return new KeyStroke(keyType, null, modifiers);
    }

    public static KeyStroke character(char character) {
        return new KeyStroke(KeyType.Character, character, EnumSet.noneOf(KeyModifier.class));
    }

    public static KeyStroke character(char character, KeyModifier modifier, KeyModifier... rest) {
        return new KeyStroke(KeyType.Character, character, EnumSet.of(modifier, rest));
    }

    public KeyType getKeyType() {
        return keyType;
    }

    public Character getCharacter() {
        return character;
    }

    public boolean hasModifier(KeyModifier modifier) {
        return modifiers.contains(modifier);
    }

    public Set<KeyModifier> modifierSet() {
        return Set.copyOf(modifiers);
    }
}
