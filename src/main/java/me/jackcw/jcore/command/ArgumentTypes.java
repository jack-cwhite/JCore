package me.jackcw.jcore.command;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public final class ArgumentTypes
{
    private ArgumentTypes()
    {
    }

    public static ArgumentType<String> string()
    {
        return input -> input;
    }

    public static ArgumentType<Integer> integer()
    {
        return input ->
        {
            try
            {
                return Integer.parseInt(input);
            }
            catch (NumberFormatException e)
            {
                throw new IllegalArgumentException(
                        "'" + input + "' is not a valid integer."
                );
            }
        };
    }

    public static ArgumentType<Double> decimal()
    {
        return input ->
        {
            try
            {
                return Double.parseDouble(input);
            }
            catch (NumberFormatException e)
            {
                throw new IllegalArgumentException(
                        "'" + input + "' is not a valid number."
                );
            }
        };
    }

    public static ArgumentType<Boolean> bool()
    {
        return input ->
        {
            if (input.equalsIgnoreCase("true"))
                return true;

            if (input.equalsIgnoreCase("false"))
                return false;

            throw new IllegalArgumentException(
                    "'" + input + "' is not true or false."
            );
        };
    }

    public static ArgumentType<Player> player()
    {
        return new ArgumentType<>()
        {
            @Override
            public Player parse(String input)
            {
                Player player = Bukkit.getPlayerExact(input);

                if (player == null)
                    throw new IllegalArgumentException(
                            "Player '" + input + "' could not be found."
                    );

                return player;
            }

            @Override
            public List<String> suggest(CommandSender sender, String partial)
            {
                String prefix = partial.toLowerCase();

                return Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(name -> name.toLowerCase().startsWith(prefix))
                        .collect(Collectors.toList());
            }
        };
    }

    public static ArgumentType<World> world()
    {
        return new ArgumentType<>()
        {
            @Override
            public World parse(String input)
            {
                World world = Bukkit.getWorld(input);

                if (world == null)
                    throw new IllegalArgumentException(
                            "World '" + input + "' could not be found."
                    );

                return world;
            }

            @Override
            public List<String> suggest(CommandSender sender, String partial)
            {
                String prefix = partial.toLowerCase();

                return Bukkit.getWorlds().stream()
                        .map(World::getName)
                        .filter(name -> name.toLowerCase().startsWith(prefix))
                        .collect(Collectors.toList());
            }
        };
    }

    public static ArgumentType<Material> material()
    {
        return new ArgumentType<>()
        {
            @Override
            public Material parse(String input)
            {
                Material material = Material.matchMaterial(input);

                if (material == null)
                    throw new IllegalArgumentException(
                            "'" + input + "' is not a valid material."
                    );

                return material;
            }

            @Override
            public List<String> suggest(CommandSender sender, String partial)
            {
                String prefix = partial.toUpperCase();

                return Arrays.stream(Material.values())
                        .map(Enum::name)
                        .filter(name -> name.startsWith(prefix))
                        .collect(Collectors.toList());
            }
        };
    }

    public static <T extends Enum<T>> ArgumentType<T> enumType(Class<T> type)
    {
        return new ArgumentType<>()
        {
            @Override
            public T parse(String input)
            {
                try
                {
                    return Enum.valueOf(type, input.toUpperCase());
                }
                catch (IllegalArgumentException e)
                {
                    throw new IllegalArgumentException(
                            "'" + input + "' is not a valid " + type.getSimpleName() + "."
                    );
                }
            }

            @Override
            public List<String> suggest(CommandSender sender, String partial)
            {
                String prefix = partial.toUpperCase();

                return Arrays.stream(type.getEnumConstants())
                        .map(Enum::name)
                        .filter(name -> name.startsWith(prefix))
                        .collect(Collectors.toList());
            }
        };
    }

    public static ArgumentType<UUID> uuid()
    {
        return input ->
        {
            try
            {
                return UUID.fromString(input);
            }
            catch (IllegalArgumentException e)
            {
                throw new IllegalArgumentException(
                        "'" + input + "' is not a valid UUID."
                );
            }
        };
    }
}
