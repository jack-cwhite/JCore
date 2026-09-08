package me.jackcw.jcore.command;

import java.util.ArrayList;
import java.util.List;

public final class CommandBuilder
{
    private final String name;
    private String description;
    private String usage;
    private String permission;
    private boolean playerOnly;
    private CommandExecutor executor;
    private final List<CommandBuilder> children = new ArrayList<>();
    private final List<CommandArgument<?>> arguments = new ArrayList<>();
    private final List<String> aliases = new ArrayList<>();

    public CommandBuilder(String name)
    {
        if (name == null || name.isBlank())
            throw new IllegalArgumentException(
                    "Command name cannot be empty"
            );

        this.name = name;
    }

    public static CommandBuilder command(String name)
    {
        return new CommandBuilder(name);
    }

    public CommandBuilder description(String description)
    {
        this.description = description;
        return this;
    }

    public CommandBuilder usage(String usage)
    {
        this.usage = usage;
        return this;
    }

    public CommandBuilder permission(String permission)
    {
        this.permission = permission;
        return this;
    }

    public CommandBuilder playerOnly()
    {
        this.playerOnly = true;
        return this;
    }

    public CommandBuilder alias(String alias)
    {
        if (alias == null || alias.isBlank())
            throw new IllegalArgumentException("Command alias cannot be empty");

        aliases.add(alias);
        return this;
    }

    public CommandBuilder executes(CommandExecutor executor)
    {
        this.executor = executor;
        return this;
    }

    public CommandBuilder child(CommandBuilder child)
    {
        children.add(child);
        return this;
    }

    public <T> CommandBuilder argument(String name, ArgumentType<T> type)
    {
        return argument(name, type, true);
    }

    public <T> CommandBuilder optionalArgument(String name, ArgumentType<T> type)
    {
        return argument(name, type, false);
    }

    private <T> CommandBuilder argument(String name, ArgumentType<T> type, boolean required)
    {
        arguments.add(new CommandArgument<>(name, type, required));
        return this;
    }

    public CommandNode build()
    {
        List<CommandNode> childNodes = new ArrayList<>();

        for (CommandBuilder child : children)
            childNodes.add(child.build());

        return new CommandNode(name, description, usage, permission, childNodes, arguments, aliases, playerOnly, executor);
    }

}
