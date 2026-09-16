package me.jackcw.jcore.menu;

import org.bukkit.configuration.ConfigurationSection;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.logging.Logger;

public final class MenuSlotResolver
{
    private static final Logger LOGGER = Logger.getLogger(MenuSlotResolver.class.getName());

    private MenuSlotResolver()
    {
    }

    public static Map<String, Integer> resolve(int rows, ConfigurationSection itemsSection, Consumer<String> onWarning)
    {
        Map<String, Integer> resolved = new LinkedHashMap<>();

        if (itemsSection == null)
            return resolved;

        int size = rows * 9;
        Set<Integer> used = new HashSet<>();

        for (String key : itemsSection.getKeys(false))
        {
            boolean specified = itemsSection.contains(key + ".slot");
            int requested = itemsSection.getInt(key + ".slot", -1);

            int slot;

            if (!specified)
            {
                warn(onWarning, "Item '" + key + "' does not specify a slot; placing it at the next available slot instead");
                slot = nextFreeSlot(used, size);
            }
            else if (requested < 0 || requested >= size)
            {
                warn(onWarning, "Item '" + key + "' has an invalid slot (" + requested + "); placing it at the next available slot instead");
                slot = nextFreeSlot(used, size);
            }
            else if (used.contains(requested))
            {
                warn(onWarning, "Item '" + key + "' requested slot " + requested + " which is already taken; placing it at the next available slot instead");
                slot = nextFreeSlot(used, size);
            }
            else
            {
                slot = requested;
            }

            if (slot == -1)
            {
                warn(onWarning, "Item '" + key + "' could not be placed - the menu is full (" + size + " slots)");
                continue;
            }

            used.add(slot);
            resolved.put(key, slot);
        }

        return resolved;
    }

    public static Map<String, Integer> resolve(int rows, ConfigurationSection itemsSection)
    {
        return resolve(rows, itemsSection, LOGGER::warning);
    }

    private static void warn(Consumer<String> onWarning, String message)
    {
        if (onWarning != null)
            onWarning.accept(message);
    }

    private static int nextFreeSlot(Set<Integer> used, int size)
    {
        for (int slot = 0; slot < size; slot++)
            if (!used.contains(slot))
                return slot;

        return -1;
    }
}
