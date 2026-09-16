package me.jackcw.jcore.menu;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class InventoryEditSession
{
    private final ItemStack[] storage;
    private final ItemStack[] armor;
    private final ItemStack offHand;
    private final ItemStack cursor;
    private boolean restored;

    private InventoryEditSession(ItemStack[] storage, ItemStack[] armor, ItemStack offHand, ItemStack cursor)
    {
        this.storage = storage;
        this.armor = armor;
        this.offHand = offHand;
        this.cursor = cursor;
    }

    public static InventoryEditSession capture(Player player)
    {
        if (player == null)
            throw new IllegalArgumentException("Player cannot be null");

        return new InventoryEditSession(
                cloneItems(player.getInventory().getStorageContents()),
                cloneItems(player.getInventory().getArmorContents()),
                cloneItem(player.getInventory().getItemInOffHand()),
                cloneItem(player.getItemOnCursor())
        );
    }

    public void restore(Player player)
    {
        if (restored)
            return;

        restored = true;

        player.getInventory().setStorageContents(cloneItems(storage));
        player.getInventory().setArmorContents(cloneItems(armor));
        player.getInventory().setItemInOffHand(itemOrAir(offHand));
        player.setItemOnCursor(itemOrAir(cursor));
    }

    private static ItemStack[] cloneItems(ItemStack[] items)
    {
        ItemStack[] clone = new ItemStack[items.length];

        for (int i = 0; i < items.length; i++)
            clone[i] = cloneItem(items[i]);

        return clone;
    }

    private static ItemStack cloneItem(ItemStack item)
    {
        return item == null || item.getType() == Material.AIR ? null : item.clone();
    }

    private static ItemStack itemOrAir(ItemStack item)
    {
        return item != null ? item.clone() : new ItemStack(Material.AIR);
    }
}
