package org.dosnavigator.terminal;

public record KeyStroke(KeyType keyType, Character character) {
    public static KeyStroke of(KeyType keyType) {
        return new KeyStroke(keyType, null);
    }

    public static KeyStroke character(char character) {
        return new KeyStroke(KeyType.Character, character);
    }

    public KeyType getKeyType() {
        return keyType;
    }

    public Character getCharacter() {
        return character;
    }
}
