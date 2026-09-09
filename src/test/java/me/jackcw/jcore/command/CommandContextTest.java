package me.jackcw.jcore.command;

import org.bukkit.command.CommandSender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CommandContextTest
{
    private ServerMock server;

    @BeforeEach
    void setUp()
    {
        server = MockBukkit.mock();
    }

    @AfterEach
    void tearDown()
    {
        MockBukkit.unmock();
    }

    @Test
    void returnsSender()
    {
        CommandSender sender = server.getConsoleSender();

        CommandContext context = new CommandContext(
                sender,
                new HashMap<>()
        );

        assertSame(
                sender,
                context.getSender()
        );
    }

    @Test
    void returnsArgument()
    {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("amount", 42);

        CommandContext context = new CommandContext(
                server.getConsoleSender(),
                arguments
        );

        Integer amount = context.get("amount");

        assertEquals(
                42,
                amount
        );
    }

    @Test
    void returnsTypedArgument()
    {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("name", "Steve");

        CommandContext context = new CommandContext(
                server.getConsoleSender(),
                arguments
        );

        String name = context.get("name");

        assertEquals(
                "Steve",
                name
        );
    }

    @Test
    void detectsExistingArgument()
    {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("amount", 42);

        CommandContext context = new CommandContext(
                server.getConsoleSender(),
                arguments
        );

        assertTrue(context.has("amount"));
        assertFalse(context.has("missing"));
    }

    @Test
    void returnsNullForMissingArgument()
    {
        CommandContext context = new CommandContext(
                server.getConsoleSender(),
                new HashMap<>()
        );

        assertNull(
                context.get("missing")
        );
    }

    @Test
    void returnsPlayerSender()
    {
        PlayerMock player = server.addPlayer();

        CommandContext context = new CommandContext(
                player,
                new HashMap<>()
        );

        assertSame(
                player,
                context.getPlayer()
        );
    }

    @Test
    void rejectsNonPlayerSender()
    {
        CommandSender sender = server.getConsoleSender();

        CommandContext context = new CommandContext(
                sender,
                new HashMap<>()
        );

        assertThrows(
                IllegalStateException.class,
                context::getPlayer
        );
    }
}