package me.jackcw.jcore.command;

import me.jackcw.jcore.TestPlugin;
import me.jackcw.jcore.TestUtils;
import me.jackcw.jcore.message.MessageManager;
import me.jackcw.jcore.storage.YamlFile;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class CommandManagerTest
{
    private TestPlugin plugin;
    private ServerMock server;
    private CommandManager commandManager;

    @BeforeEach
    void setUp()
    {
        plugin = TestUtils.mockPlugin();
        server = MockBukkit.getMock();

        YamlFile file = TestUtils.createYaml(plugin);
        file.set("core.no-permission", "You do not have permission.");
        file.save();
        file.reload();

        MessageManager messageManager = new MessageManager(plugin, file);

        commandManager = new CommandManager(plugin, messageManager);
    }

    @AfterEach
    void tearDown()
    {
        TestUtils.cleanup();
    }

    @Test
    void registersCommand()
    {
        CommandNode command = CommandBuilder.command("test")
                .executes(context ->
                {
                })
                .build();

        assertDoesNotThrow(
                () -> commandManager.register(command)
        );
    }

    @Test
    void executesCommand()
    {
        AtomicBoolean executed = new AtomicBoolean(false);

        CommandNode command = CommandBuilder.command("test")
                .executes(context -> executed.set(true))
                .build();

        commandManager.register(command);

        assertTrue(
                server.dispatchCommand(
                        server.getConsoleSender(),
                        "test"
                )
        );

        assertTrue(executed.get());
    }

    @Test
    void passesCorrectSenderToExecutor()
    {
        AtomicReference<Object> receivedSender = new AtomicReference<>();

        CommandNode command = CommandBuilder.command("test")
                .executes(context -> receivedSender.set(context.getSender()))
                .build();

        commandManager.register(command);

        server.dispatchCommand(
                server.getConsoleSender(),
                "test"
        );

        assertSame(
                server.getConsoleSender(),
                receivedSender.get()
        );
    }

    @Test
    void executesChildCommand()
    {
        AtomicBoolean executed = new AtomicBoolean(false);

        CommandNode command = CommandBuilder.command("test")
                .child(
                        CommandBuilder.command("sub")
                                .executes(context -> executed.set(true))
                )
                .build();

        commandManager.register(command);

        assertTrue(
                server.dispatchCommand(
                        server.getConsoleSender(),
                        "test sub"
                )
        );

        assertTrue(executed.get());
    }

    @Test
    void executesCommandUsingAlias()
    {
        AtomicBoolean executed = new AtomicBoolean(false);

        CommandNode command = CommandBuilder.command("test")
                .alias("t")
                .executes(context -> executed.set(true))
                .build();

        commandManager.register(command);

        assertTrue(
                server.dispatchCommand(
                        server.getConsoleSender(),
                        "t"
                )
        );

        assertTrue(executed.get());
    }

    @Test
    void returnsFalseForUnknownCommand()
    {
        assertFalse(
                server.dispatchCommand(
                        server.getConsoleSender(),
                        "unknown"
                )
        );
    }

    @Test
    void executesCommandWithPermission()
    {
        AtomicBoolean executed = new AtomicBoolean(false);

        CommandNode command = CommandBuilder.command("test")
                .permission("jcore.test")
                .executes(context -> executed.set(true))
                .build();

        commandManager.register(command);

        server.getConsoleSender().addAttachment(
                plugin,
                "jcore.test",
                true
        );

        assertTrue(
                server.dispatchCommand(
                        server.getConsoleSender(),
                        "test"
                )
        );

        assertTrue(executed.get());
    }

    @Test
    void doesNotExecuteCommandWithoutPermission()
    {
        AtomicBoolean executed = new AtomicBoolean(false);

        CommandNode command = CommandBuilder.command("test")
                .permission("jcore.test")
                .executes(context -> executed.set(true))
                .build();

        commandManager.register(command);

        server.addPlayer("TestPlayer");

        assertTrue(
                server.dispatchCommand(
                        server.getPlayer("TestPlayer"),
                        "test"
                )
        );

        assertFalse(executed.get());
    }

    @Test
    void passesStringArgumentToExecutor()
    {
        AtomicReference<String> received = new AtomicReference<>();

        CommandNode command = CommandBuilder.command("test")
                .argument("name", ArgumentTypes.string())
                .executes(context -> received.set(context.get("name")))
                .build();

        commandManager.register(command);

        assertTrue(
                server.dispatchCommand(
                        server.getConsoleSender(),
                        "test Steve"
                )
        );

        assertEquals(
                "Steve",
                received.get()
        );
    }

    @Test
    void passesIntegerArgumentToExecutor()
    {
        AtomicReference<Integer> received = new AtomicReference<>();

        CommandNode command = CommandBuilder.command("test")
                .argument("amount", ArgumentTypes.integer())
                .executes(context -> received.set(context.get("amount")))
                .build();

        commandManager.register(command);

        assertTrue(
                server.dispatchCommand(
                        server.getConsoleSender(),
                        "test 42"
                )
        );

        assertEquals(
                42,
                received.get()
        );
    }

    @Test
    void passesDecimalArgumentToExecutor()
    {
        AtomicReference<Double> received = new AtomicReference<>();

        CommandNode command = CommandBuilder.command("test")
                .argument("amount", ArgumentTypes.decimal())
                .executes(context -> received.set(context.get("amount")))
                .build();

        commandManager.register(command);

        assertTrue(
                server.dispatchCommand(
                        server.getConsoleSender(),
                        "test 42.5"
                )
        );

        assertEquals(
                42.5,
                received.get()
        );
    }

    @Test
    void passesBooleanArgumentToExecutor()
    {
        AtomicReference<Boolean> received = new AtomicReference<>();

        CommandNode command = CommandBuilder.command("test")
                .argument("enabled", ArgumentTypes.bool())
                .executes(context -> received.set(context.get("enabled")))
                .build();

        commandManager.register(command);

        assertTrue(
                server.dispatchCommand(
                        server.getConsoleSender(),
                        "test true"
                )
        );

        assertTrue(received.get());
    }

    @Test
    void passesMultipleArgumentsToExecutor()
    {
        AtomicReference<String> receivedName = new AtomicReference<>();
        AtomicReference<Integer> receivedAmount = new AtomicReference<>();

        CommandNode command = CommandBuilder.command("test")
                .argument("name", ArgumentTypes.string())
                .argument("amount", ArgumentTypes.integer())
                .executes(context ->
                {
                    receivedName.set(context.get("name"));
                    receivedAmount.set(context.get("amount"));
                })
                .build();

        commandManager.register(command);

        assertTrue(
                server.dispatchCommand(
                        server.getConsoleSender(),
                        "test Steve 42"
                )
        );

        assertEquals(
                "Steve",
                receivedName.get()
        );

        assertEquals(
                42,
                receivedAmount.get()
        );
    }

    @Test
    void suggestsMatchingChildCommandNames()
    {
        CommandNode command = CommandBuilder.command("test")
                .child(CommandBuilder.command("add").executes(context -> {}))
                .child(CommandBuilder.command("remove").executes(context -> {}))
                .build();

        commandManager.register(command);

        PluginCommand bukkitCommand = plugin.getCommand("test");

        assertEquals(
                List.of("add"),
                bukkitCommand.tabComplete(server.getConsoleSender(), "test", new String[]{"a"})
        );
    }

    @Test
    void suggestsArgumentValues()
    {
        ArgumentType<String> color = new ArgumentType<>()
        {
            @Override
            public String parse(String input)
            {
                return input;
            }

            @Override
            public List<String> suggest(CommandSender sender, String partial)
            {
                return List.of("red", "blue", "green").stream()
                        .filter(value -> value.startsWith(partial))
                        .toList();
            }
        };

        CommandNode command = CommandBuilder.command("test")
                .argument("color", color)
                .executes(context -> {})
                .build();

        commandManager.register(command);

        PluginCommand bukkitCommand = plugin.getCommand("test");

        assertEquals(
                List.of("red"),
                bukkitCommand.tabComplete(server.getConsoleSender(), "test", new String[]{"r"})
        );
    }

    @Test
    void suggestsChildrenAndArgumentTogetherAtAmbiguousPosition()
    {
        ArgumentType<String> color = new ArgumentType<>()
        {
            @Override
            public String parse(String input)
            {
                return input;
            }

            @Override
            public List<String> suggest(CommandSender sender, String partial)
            {
                return List.of("aqua").stream()
                        .filter(value -> value.startsWith(partial))
                        .toList();
            }
        };

        CommandNode command = CommandBuilder.command("test")
                .child(CommandBuilder.command("add").executes(context -> {}))
                .argument("color", color)
                .executes(context -> {})
                .build();

        commandManager.register(command);

        PluginCommand bukkitCommand = plugin.getCommand("test");

        List<String> completions = bukkitCommand.tabComplete(
                server.getConsoleSender(), "test", new String[]{"a"}
        );

        assertTrue(completions.contains("add"));
        assertTrue(completions.contains("aqua"));
    }

    @Test
    void stopsSuggestingChildrenOnceArgumentsAreBeingConsumed()
    {
        ArgumentType<String> color = new ArgumentType<>()
        {
            @Override
            public String parse(String input)
            {
                return input;
            }

            @Override
            public List<String> suggest(CommandSender sender, String partial)
            {
                return List.of("red", "blue");
            }
        };

        CommandNode command = CommandBuilder.command("test")
                .child(CommandBuilder.command("add").executes(context -> {}))
                .argument("color", color)
                .argument("shade", ArgumentTypes.string())
                .executes(context -> {})
                .build();

        commandManager.register(command);

        PluginCommand bukkitCommand = plugin.getCommand("test");

        List<String> completions = bukkitCommand.tabComplete(
                server.getConsoleSender(), "test", new String[]{"notachild", ""}
        );

        assertTrue(completions.isEmpty());
    }
}