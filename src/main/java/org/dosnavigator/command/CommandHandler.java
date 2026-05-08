package org.dosnavigator.command;

@FunctionalInterface
public interface CommandHandler {
    boolean handle(Command command);
}
