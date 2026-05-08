package org.dosnavigator.tui;

import org.dosnavigator.command.CommandBus;
import org.dosnavigator.command.CommandId;
import org.dosnavigator.terminal.KeyStroke;
import org.dosnavigator.terminal.KeyType;
import org.dosnavigator.ui.Box;

import java.util.Optional;

public class Dialog extends Window {
    private CommandId endState;

    public Dialog(Box bounds, String title) {
        super(bounds, title);
    }

    public Optional<CommandId> endState() {
        return Optional.ofNullable(endState);
    }

    public void endModal(CommandId commandId) {
        endState = commandId;
    }

    @Override
    public boolean handleKey(KeyStroke key, CommandBus commandBus) {
        if (key.keyType() == KeyType.Enter) {
            endModal(CommandId.OK);
            return true;
        }
        if (key.keyType() == KeyType.Escape) {
            endModal(CommandId.CANCEL);
            return true;
        }
        return super.handleKey(key, commandBus);
    }
}
