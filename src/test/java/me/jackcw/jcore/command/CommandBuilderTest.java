package me.jackcw.jcore.command;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CommandBuilderTest
{
    @Test
    void buildsBasicCommand()
    {
        CommandNode command = CommandBuilder.command("test")
                .build();

        assertEquals("test", command.getName());
        assertNull(command.getDescription());
        assertNull(command.getUsage());
        assertNull(command.getPermission());
        assertFalse(command.isPlayerOnly());
        assertNull(command.getExecutor());
        assertTrue(command.getChildren().isEmpty());
        assertTrue(command.getArguments().isEmpty());
        assertTrue(command.getAliases().isEmpty());
    }

    @Test
    void buildsCommandWithMetadata()
    {
        CommandExecutor executor = context -> {};

        CommandNode command = CommandBuilder.command("test")
                .description("Test command")
                .usage("/test")
                .permission("jcore.test")
                .playerOnly()
                .executes(executor)
                .alias("t")
                .alias("testing")
                .build();

        assertEquals("test", command.getName());
        assertEquals("Test command", command.getDescription());
        assertEquals("/test", command.getUsage());
        assertEquals("jcore.test", command.getPermission());
        assertTrue(command.isPlayerOnly());
        assertSame(executor, command.getExecutor());
        assertEquals(2, command.getAliases().size());
        assertTrue(command.getAliases().contains("t"));
        assertTrue(command.getAliases().contains("testing"));
    }

    @Test
    void buildsArguments()
    {
        CommandNode command = CommandBuilder.command("test")
                .argument("amount", ArgumentTypes.integer())
                .optionalArgument("name", ArgumentTypes.string())
                .build();

        assertEquals(2, command.getArguments().size());

        CommandArgument<?> amount = command.getArguments().get(0);
        assertEquals("amount", amount.getName());
        assertEquals(42, amount.getType().parse("42"));
        assertTrue(amount.isRequired());

        CommandArgument<?> name = command.getArguments().get(1);
        assertEquals("name", name.getName());
        assertFalse(name.isRequired());
    }

    @Test
    void buildsChildCommands()
    {
        CommandNode command = CommandBuilder.command("test")
                .child(
                        CommandBuilder.command("sub")
                                .description("Sub command")
                )
                .build();

        assertEquals(1, command.getChildren().size());
        assertEquals("sub", command.getChildren().get(0).getName());
        assertEquals("Sub command", command.getChildren().get(0).getDescription());
    }

    @Test
    void supportsChainedChildren()
    {
        CommandNode command = CommandBuilder.command("test")
                .child(
                        CommandBuilder.command("admin")
                                .child(
                                        CommandBuilder.command("reload")
                                )
                )
                .build();

        CommandNode admin = command.getChildren().get(0);
        CommandNode reload = admin.getChildren().get(0);

        assertEquals("admin", admin.getName());
        assertEquals("reload", reload.getName());
    }

    @Test
    void rejectsEmptyCommandName()
    {
        assertThrows(
                IllegalArgumentException.class,
                () -> CommandBuilder.command("")
        );
    }

    @Test
    void rejectsBlankCommandName()
    {
        assertThrows(
                IllegalArgumentException.class,
                () -> CommandBuilder.command("   ")
        );
    }

    @Test
    void rejectsEmptyAlias()
    {
        CommandBuilder builder = CommandBuilder.command("test");

        assertThrows(
                IllegalArgumentException.class,
                () -> builder.alias("")
        );
    }

    @Test
    void rejectsBlankAlias()
    {
        CommandBuilder builder = CommandBuilder.command("test");

        assertThrows(
                IllegalArgumentException.class,
                () -> builder.alias("   ")
        );
    }
}