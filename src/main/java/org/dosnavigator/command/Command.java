package org.dosnavigator.command;

public record Command(CommandId id, Object source) {
    public static Command of(CommandId id) {
        return new Command(id, null);
    }

    public static Command of(CommandId id, Object source) {
        return new Command(id, source);
    }
}
