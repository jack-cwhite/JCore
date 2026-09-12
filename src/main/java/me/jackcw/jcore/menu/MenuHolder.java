package me.jackcw.jcore.menu;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public final class MenuHolder implements InventoryHolder
{
    private final Menu menu;
    private Inventory inventory;

    MenuHolder(Menu menu)
    {
        this.menu = menu;
    }

    void setInventory(Inventory inventory)
    {
        this.inventory = inventory;
    }

    @Override
    public Inventory getInventory()
    {
        return inventory;
    }

    public Menu getMenu()
    {
        return menu;
    }
}
