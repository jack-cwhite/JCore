package me.jackcw.jcore.menu;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MenuSlotResolverTest
{
    @Test
    void returnsEmptyMapForNullSection()
    {
        assertTrue(MenuSlotResolver.resolve(1, null).isEmpty());
    }

    @Test
    void usesRequestedSlotWhenValid()
    {
        YamlConfiguration config = new YamlConfiguration();
        config.set("items.button.slot", 4);

        Map<String, Integer> resolved = MenuSlotResolver.resolve(1, config.getConfigurationSection("items"));

        assertEquals(4, resolved.get("button"));
    }

    @Test
    void fallsBackToNextFreeSlotWhenMissing()
    {
        YamlConfiguration config = new YamlConfiguration();
        config.set("items.button", "");

        Map<String, Integer> resolved = MenuSlotResolver.resolve(1, config.getConfigurationSection("items"));

        assertEquals(0, resolved.get("button"));
    }

    @Test
    void fallsBackToNextFreeSlotWhenOutOfRange()
    {
        YamlConfiguration config = new YamlConfiguration();
        config.set("items.button.slot", 99);

        Map<String, Integer> resolved = MenuSlotResolver.resolve(1, config.getConfigurationSection("items"));

        assertEquals(0, resolved.get("button"));
    }

    @Test
    void secondItemRequestingSameSlotFallsBackToNextFree()
    {
        YamlConfiguration config = new YamlConfiguration();
        config.set("items.first.slot", 3);
        config.set("items.second.slot", 3);

        Map<String, Integer> resolved = MenuSlotResolver.resolve(1, config.getConfigurationSection("items"));

        assertEquals(3, resolved.get("first"));
        assertEquals(0, resolved.get("second"));
    }

    @Test
    void dropsItemWhenNoFreeSlotRemains()
    {
        YamlConfiguration config = new YamlConfiguration();

        for (int i = 0; i < 9; i++)
            config.set("items.item" + i + ".slot", i);

        config.set("items.overflow.slot", 0);

        Map<String, Integer> resolved = MenuSlotResolver.resolve(1, config.getConfigurationSection("items"));

        assertEquals(9, resolved.size());
        assertFalse(resolved.containsKey("overflow"));
    }

    @Test
    void neverThrowsRegardlessOfBadInput()
    {
        YamlConfiguration config = new YamlConfiguration();
        config.set("items.a.slot", -1);
        config.set("items.b.slot", 1000);

        assertDoesNotThrow(() -> MenuSlotResolver.resolve(1, config.getConfigurationSection("items")));
    }

    @Test
    void warnsWhenSlotIsUnSpecified()
    {
        YamlConfiguration config = new YamlConfiguration();
        config.set("items.button", "");

        List<String> warnings = new ArrayList<>();

        MenuSlotResolver.resolve(1, config.getConfigurationSection("items"), warnings::add);

        assertEquals(1, warnings.size());
    }

    @Test
    void warnsWhenRequestedSlotIsOutOfRange()
    {
        YamlConfiguration config = new YamlConfiguration();
        config.set("items.button.slot", 99);

        List<String> warnings = new ArrayList<>();

        MenuSlotResolver.resolve(1, config.getConfigurationSection("items"), warnings::add);

        assertEquals(1, warnings.size());
    }

    @Test
    void warnsWhenRequestedSlotIsAlreadyTaken()
    {
        YamlConfiguration config = new YamlConfiguration();
        config.set("items.first.slot", 3);
        config.set("items.second.slot", 3);

        List<String> warnings = new ArrayList<>();

        MenuSlotResolver.resolve(1, config.getConfigurationSection("items"), warnings::add);

        assertEquals(1, warnings.size());
    }

    @Test
    void warnsWhenItemIsDroppedDueToNoFreeSlots()
    {
        YamlConfiguration config = new YamlConfiguration();

        for (int i = 0; i < 9; i++)
            config.set("items.item" + i + ".slot", i);

        // No slot requested at all (rather than one that collides), so this
        // triggers exactly the "menu is full" warning and nothing else.
        config.set("items.overflow", "");

        List<String> warnings = new ArrayList<>();

        MenuSlotResolver.resolve(1, config.getConfigurationSection("items"), warnings::add);

        assertEquals(2, warnings.size());
    }
}
