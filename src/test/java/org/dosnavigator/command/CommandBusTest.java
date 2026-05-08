package org.dosnavigator.command;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommandBusTest {
    @Test
    void dispatchesRegisteredHandlersByCommandId() {
        CommandBus bus = new CommandBus();
        AtomicInteger calls = new AtomicInteger();
        bus.register(CommandId.NEXT, command -> {
            calls.incrementAndGet();
            return true;
        });

        assertTrue(bus.dispatch(CommandId.NEXT));
        assertEquals(1, calls.get());
    }

    @Test
    void reportsUnhandledCommands() {
        CommandBus bus = new CommandBus();

        assertFalse(bus.dispatch(CommandId.CLOSE));
    }

    @Test
    void exposesPascalCommandNumbers() {
        assertEquals(CommandId.QUIT, CommandId.fromPascalId(1).orElseThrow());
        assertEquals("cmMenu", CommandId.MENU.legacyName());
    }
}
