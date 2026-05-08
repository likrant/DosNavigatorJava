package org.dosnavigator.command;

import java.util.Arrays;
import java.util.Optional;

public enum CommandId {
    QUIT(1, "cmQuit"),
    MENU(3, "cmMenu"),
    CLOSE(4, "cmClose"),
    NEXT(7, "cmNext"),
    PREVIOUS(8, "cmPrev"),
    OK(10, "cmOK"),
    CANCEL(11, "cmCancel"),
    MENU_ON(10022, "cmMenuOn"),
    MENU_OFF(10023, "cmMenuOff");

    private final int pascalId;
    private final String legacyName;

    CommandId(int pascalId, String legacyName) {
        this.pascalId = pascalId;
        this.legacyName = legacyName;
    }

    public int pascalId() {
        return pascalId;
    }

    public String legacyName() {
        return legacyName;
    }

    public static Optional<CommandId> fromPascalId(int pascalId) {
        return Arrays.stream(values())
                .filter(command -> command.pascalId == pascalId)
                .findFirst();
    }
}
