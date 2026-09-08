package me.jackcw.jcore.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

public class CommandContext
{
    private final CommandSender sender;
    private final Map<String, Object> arguments;

    public CommandContext(CommandSender sender, Map<String, Object> arguments)
    {
        this.sender = sender;
        this.arguments = arguments;
    }

    public CommandSender getSender()
    {
        return sender;
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String name)
    {
        return (T) arguments.get(name);
    }

    public boolean has(String name)
    {
        return arguments.containsKey(name);
    }

    public Player getPlayer()
    {
        if (!(sender instanceof Player player))
            throw new IllegalStateException(
                    "Command sender is not a player"
            );

        return player;
    }
}
