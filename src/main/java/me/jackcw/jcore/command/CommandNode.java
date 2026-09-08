package me.jackcw.jcore.command;

import java.util.List;

public final class CommandNode
{
    private final String name;
    private final String description;
    private final String usage;
    private final String permission;

    private final List<CommandNode> children;
    private final List<CommandArgument<?>> arguments;
    private final List<String> aliases;

    private final boolean playerOnly;

    private final CommandExecutor executor;

    public CommandNode(String name, String description, String usage, String permission, List<CommandNode> children, List<CommandArgument<?>> arguments, List<String> aliases, boolean playerOnly, CommandExecutor executor)
    {
        this.name = name;
        this.description = description;
        this.usage = usage;
        this.permission = permission;
        this.children = List.copyOf(children);
        this.arguments = List.copyOf(arguments);
        this.aliases = aliases;
        this.playerOnly = playerOnly;
        this.executor = executor;
    }

    public String getName()
    {
        return name;
    }

    public String getDescription()
    {
        return description;
    }

    public String getUsage()
    {
        return usage;
    }

    public String getPermission()
    {
        return permission;
    }

    public List<CommandNode> getChildren()
    {
        return children;
    }

    public List<CommandArgument<?>> getArguments()
    {
        return arguments;
    }

    public List<String> getAliases()
    {
        return aliases;
    }

    public boolean isPlayerOnly()
    {
        return playerOnly;
    }

    public CommandExecutor getExecutor()
    {
        return executor;
    }
}
