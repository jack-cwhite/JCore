package me.jackcw.jcore.menu;

import me.jackcw.jcore.item.ItemStackParser;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public final class ConfiguredMenu
{
    private final MenuManager menuManager;
    private final MenuBuilder builder;
    private final String rawTitle;
    private final ConfigurationSection items;
    private final Map<String, Integer> slots;
    private final int rows;
    private Map<String, Object> placeholders;
    private boolean showBack;

    ConfiguredMenu(MenuManager menuManager, MenuBuilder builder, String rawTitle, ConfigurationSection items, Map<String, Integer> slots, int rows)
    {
        this.menuManager = menuManager;
        this.builder = builder;
        this.rawTitle = rawTitle;
        this.items = items;
        this.slots = slots;
        this.rows = rows;
    }

    public ConfiguredMenu placeholders(Map<String, Object> placeholders)
    {
        this.placeholders = placeholders;
        return this;
    }

    public ConfiguredMenu item(String key, MenuClickHandler handler)
    {
        Integer slot = slots.get(key);

        if (slot != null)
            builder.item(slot, ItemStackParser.parseSafely(items.getConfigurationSection(key), placeholders), handler);

        return this;
    }

    public ConfiguredMenu item(String key, Map<String, Object> placeholders, MenuClickHandler handler)
    {
        Integer slot = slots.get(key);

        if (slot != null)
            builder.item(slot, ItemStackParser.parseSafely(items.getConfigurationSection(key), placeholders), handler);

        return this;
    }

    public ConfiguredMenu item(String key, boolean useAlternate, MenuClickHandler handler)
    {
        Integer slot = slots.get(key);

        if (slot != null)
            builder.item(slot, ItemStackParser.parseSafely(items.getConfigurationSection(key), placeholders, useAlternate), handler);

        return this;
    }

    public ConfiguredMenu item(String key, ItemStack fallback, MenuClickHandler handler)
    {
        Integer slot = slots.get(key);

        if (slot != null)
            builder.item(slot, items.getConfigurationSection(key), fallback, handler);

        return this;
    }

    public ConfiguredMenu back()
    {
        showBack = true;

        return this;
    }

    public boolean has(String key)
    {
        return slots.containsKey(key);
    }

    public MenuBuilder builder()
    {
        return builder;
    }

    public void open(Player player)
    {
        if (showBack && menuManager.navigator().hasHistory(player))
        {
            int slot = MenuNavigationSlots.backButton(rows);
            builder.item(slot, menuManager.navigationStyle().back(), MenuContext::back);
        }

        builder.title(ItemStackParser.substitute(rawTitle, placeholders));
        builder.build().open(player);
    }
}
