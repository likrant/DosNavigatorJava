package org.dosnavigator.foundation;

import org.dosnavigator.command.CommandId;

import java.util.List;

record LegacyMenuItem(String text, char mnemonic, CommandId command, List<LegacyMenuItem> children) {
    LegacyMenuItem {
        children = List.copyOf(children);
    }

    boolean hasChildren() {
        return !children.isEmpty();
    }

    static LegacyMenuItem command(String text, char mnemonic, CommandId command) {
        return new LegacyMenuItem(text, mnemonic, command, List.of());
    }

    static LegacyMenuItem submenu(String text, char mnemonic, LegacyMenuItem... children) {
        return new LegacyMenuItem(text, mnemonic, null, List.of(children));
    }
}
