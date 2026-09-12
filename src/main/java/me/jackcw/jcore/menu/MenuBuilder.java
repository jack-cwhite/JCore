package me.jackcw.jcore.menu;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public final class MenuBuilder
{
    private final String title;
    private final int rows;

    private final Map<Integer, ItemStack> items = new HashMap<>();
    private final Map<Integer, MenuClickHandler> handlers = new HashMap<>();

    private ItemStack fillItem;
    private Consumer<Player> onClose;

    MenuBuilder(String title, int rows)
    {
        if (title == null)
            throw new IllegalArgumentException(
                    "Title cannot be null"
            );

        if (rows < 1 || rows > 6)
            throw new IllegalArgumentException(
                    "Rows must be between 1 and 6"
            );

        this.title = title;
        this.rows = rows;
    }

    public MenuBuilder item(int slot, ItemStack item, MenuClickHandler handler)
    {
        validateSlot(slot);

        items.put(slot, item);

        if (handler != null)
            handlers.put(slot, handler);

        return this;
    }

    public MenuBuilder item(int slot, ItemStack item)
    {
        return item(slot, item, null);
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
        Menu menu = new Menu(title, rows, onClose);

        if (fillItem != null)
            for (int slot = 0; slot < rows * 9; slot++)
                if (!items.containsKey(slot))
                    menu.setItem(slot, fillItem);

        for (Map.Entry<Integer, ItemStack> entry : items.entrySet())
            menu.setItem(entry.getKey(), entry.getValue(), handlers.get(entry.getKey()));

        return menu;
    }

    private void validateSlot(int slot)
    {
        if (slot < 0 || slot >= rows * 9)
            throw new IllegalArgumentException(
                    "Slot " + slot + " is out of bounds for " + rows + " rows"
            );
    }
}
