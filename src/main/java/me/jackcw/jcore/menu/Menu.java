package me.jackcw.jcore.menu;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public final class Menu
{
    private final Inventory inventory;
    private final Map<Integer, MenuClickHandler> handlers = new HashMap<>();
    private final Consumer<Player> onClose;

    Menu(String title, int rows, Consumer<Player> onClose)
    {
        this.onClose = onClose;

        MenuHolder holder = new MenuHolder(this);

        this.inventory = Bukkit.createInventory(
                holder,
                rows * 9,
                LegacyComponentSerializer.legacyAmpersand().deserialize(title)
        );

        holder.setInventory(inventory);
    }

    public void open(Player player)
    {
        player.openInventory(inventory);
    }

    public void setItem(int slot, ItemStack item, MenuClickHandler handler)
    {
        inventory.setItem(slot, item);

        if (handler != null)
            handlers.put(slot, handler);
        else
            handlers.remove(slot);
    }

    public void setItem(int slot, ItemStack item)
    {
        setItem(slot, item, null);
    }

    public void removeItem(int slot)
    {
        inventory.setItem(slot, null);
        handlers.remove(slot);
    }

    public Inventory getInventory()
    {
        return inventory;
    }

    void handleClick(InventoryClickEvent event)
    {
        MenuClickHandler handler = handlers.get(event.getSlot());

        if (handler == null)
            return;

        MenuClickContext context = new MenuClickContext(
                (Player) event.getWhoClicked(),
                event.getSlot(),
                event.getCurrentItem(),
                event.getClick(),
                event
        );

        handler.onClick(context);
    }

    void handleClose(Player player)
    {
        if (onClose != null)
            onClose.accept(player);
    }
}
