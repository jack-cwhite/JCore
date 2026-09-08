package me.jackcw.jcore.util;

import org.bukkit.ChatColor;

public final class StringUtil
{
    private StringUtil()
    {
    }

    public static String color(String message)
    {
        if (message == null)
            return null;

        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static String replace(String message, Object... replacers)
    {
        if (message == null)
            return null;

        if (replacers == null)
            return color(message);

        for (int i = 0; i + 1 < replacers.length; i += 2)
        {
            String key = String.valueOf(replacers[i]);
            String value = String.valueOf(replacers[i + 1]);

            message = message.replace(
                    "{" + key + "}",
                    value
            );
        }

        return color(message);
    }
}
