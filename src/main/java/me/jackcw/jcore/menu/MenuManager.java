package me.jackcw.jcore.menu;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;

public final class MenuManager implements Listener
{
    public MenuManager(JavaPlugin plugin)
    {
        if (plugin == null)
            throw new IllegalArgumentException(
                    "Plugin cannot be null"
            );

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public MenuBuilder builder(String title, int rows)
    {
        return new MenuBuilder(title, rows);
    }

    public PaginatedMenuBuilder paginatedBuilder(String title, int rows)
    {
        return new PaginatedMenuBuilder(this, title, rows);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event)
    {
        Inventory topInventory = event.getView().getTopInventory();

        if (!(topInventory.getHolder() instanceof MenuHolder holder))
            return;

        event.setCancelled(true);

        if (event.getClickedInventory() != null && event.getClickedInventory().equals(topInventory))
            holder.getMenu().handleClick(event);
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event)
    {
        Inventory topInventory = event.getView().getTopInventory();

        if (topInventory.getHolder() instanceof MenuHolder)
            event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event)
    {
        Inventory topInventory = event.getView().getTopInventory();

        if (topInventory.getHolder() instanceof MenuHolder holder)
            holder.getMenu().handleClose((Player) event.getPlayer());
    }
}
