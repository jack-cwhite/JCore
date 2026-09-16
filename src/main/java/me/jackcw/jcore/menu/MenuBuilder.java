package me.jackcw.jcore.menu;

import me.jackcw.jcore.item.ItemStackParser;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

public final class MenuBuilder
{
    private final MenuManager menuManager;
    private String title;
    private final int rows;

    private final Map<Integer, ItemStack> items = new HashMap<>();
    private final Map<Integer, MenuClickHandler> handlers = new HashMap<>();
    private final Map<Integer, ItemStack> editableItems = new HashMap<>();
    private final Map<Integer, Predicate<ItemStack>> editableValidators = new HashMap<>();

    private ItemStack fillItem;
    private Consumer<Player> onClose;

    MenuBuilder(MenuManager menuManager, String title, int rows)
    {
        if (title == null)
            throw new IllegalArgumentException("Title cannot be null");

        if (rows < 1 || rows > 6)
            throw new IllegalArgumentException("Rows must be between 1 and 6");

        this.menuManager = menuManager;
        this.title = title;
        this.rows = rows;
    }

    public MenuBuilder item(int slot, ItemStack item, MenuClickHandler handler)
    {
        validateSlot(slot);

        items.put(slot, item);

        if (handler != null)
            handlers.put(slot, handler);
        else
            handlers.remove(slot);

        return this;
    }

    public MenuBuilder item(int slot, ItemStack item)
    {
        return item(slot, item, null);
    }

    public MenuBuilder item(int slot, ConfigurationSection config, MenuClickHandler handler)
    {
        return item(slot, ItemStackParser.parseSafely(config), handler);
    }

    public MenuBuilder item(int slot, ConfigurationSection config, Map<String, Object> placeholders, MenuClickHandler handler)
    {
        return item(slot, ItemStackParser.parseSafely(config, placeholders), handler);
    }

    public MenuBuilder item(int slot, ConfigurationSection config, Map<String, Object> placeholders, boolean useAlternate, MenuClickHandler handler)
    {
        return item(slot, ItemStackParser.parseSafely(config, placeholders, useAlternate), handler);
    }

    public MenuBuilder item(int slot, ConfigurationSection config, ItemStack fallback, MenuClickHandler handler)
    {
        return item(slot, ItemStackParser.parseSafely(config, fallback), handler);
    }

    MenuBuilder title(String title)
    {
        this.title = title;
        return this;
    }

    public MenuBuilder editableSlot(int slot, ItemStack initial, Predicate<ItemStack> validator)
    {
        validateSlot(slot);

        items.remove(slot);
        handlers.remove(slot);
        editableItems.put(slot, initial);
        editableValidators.put(slot, validator);

        return this;
    }

    public MenuBuilder editableSlot(int slot, ItemStack initial)
    {
        return editableSlot(slot, initial, null);
    }

    public MenuBuilder fill(ItemStack item)
    {
        this.fillItem = item;
        return this;
    }

    public MenuBuilder onClose(Consumer<Player> onClose)
    {
        this.onClose = onClose;
        return this;
    }

    public Menu build()
    {
        Menu menu = new Menu(menuManager, title, rows, onClose);

        if (fillItem != null)
            for (int slot = 0; slot < rows * 9; slot++)
                if (!items.containsKey(slot) && !editableItems.containsKey(slot))
                    menu.setItem(slot, fillItem);

        for (Map.Entry<Integer, ItemStack> entry : items.entrySet())
            menu.setItem(entry.getKey(), entry.getValue(), handlers.get(entry.getKey()));

        for (Map.Entry<Integer, ItemStack> entry : editableItems.entrySet())
            menu.setEditableSlot(entry.getKey(), entry.getValue(), editableValidators.get(entry.getKey()));

        return menu;
    }

    private void validateSlot(int slot)
    {
        if (slot < 0 || slot >= rows * 9)
            throw new IllegalArgumentException("Slot " + slot + " is out of bounds for " + rows + " rows");
    }
}
