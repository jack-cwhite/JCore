package me.jackcw.jcore.command;

import org.bukkit.command.CommandSender;

import java.util.List;

public interface ArgumentType<T>
{
    T parse(String input) throws IllegalArgumentException;

    default List<String> suggest(CommandSender sender, String partial)
    {
        return List.of();
    }
}
