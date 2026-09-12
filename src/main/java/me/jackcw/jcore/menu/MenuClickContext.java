package me.jackcw.jcore.menu;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public final class MenuClickContext
{
    private final Player player;
    private final int slot;
    private final ItemStack item;
    private final ClickType clickType;
    private final InventoryClickEvent event;

    public MenuClickContext(Player player, int slot, ItemStack item, ClickType clickType, InventoryClickEvent event)
    {
        this.player = player;
        this.slot = slot;
        this.item = item;
        this.clickType = clickType;
        this.event = event;
    }

    public Player getPlayer()
    {
        return player;
    }

    public int getSlot()
    {
        return slot;
    }

    public ItemStack getItem()
    {
        return item;
    }

    public ClickType getClickType()
    {
        return clickType;
    }

    public InventoryClickEvent getEvent()
    {
        return event;
    }
}
