package org.dosnavigator.command;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class CommandBus {
    private final Map<CommandId, List<CommandHandler>> handlers = new EnumMap<>(CommandId.class);

    public void register(CommandId id, CommandHandler handler) {
        handlers.computeIfAbsent(id, ignored -> new ArrayList<>()).add(handler);
    }

    public boolean dispatch(CommandId id) {
        return dispatch(Command.of(id));
    }

    public boolean dispatch(Command command) {
        List<CommandHandler> commandHandlers = handlers.get(command.id());
        if (commandHandlers == null || commandHandlers.isEmpty()) {
            return false;
        }

        boolean handled = false;
        for (CommandHandler handler : List.copyOf(commandHandlers)) {
            handled |= handler.handle(command);
        }
        return handled;
    }
}
