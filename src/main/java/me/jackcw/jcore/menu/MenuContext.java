package me.jackcw.jcore.menu;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

public final class MenuContext
{
    private final MenuManager menuManager;
    private final Player player;
    private final int slot;
    private final ItemStack item;
    private final ClickType clickType;
    private final InventoryClickEvent event;
    private final PageContext page;

    MenuContext(MenuManager menuManager, Player player, int slot, ItemStack item, ClickType clickType, InventoryClickEvent event, PageContext page)
    {
        this.menuManager = menuManager;
        this.player = player;
        this.slot = slot;
        this.item = item;
        this.clickType = clickType;
        this.event = event;
        this.page = page;
    }

    public Player player()
    {
        return player;
    }

    public int slot()
    {
        return slot;
    }

    public ItemStack item()
    {
        return item;
    }

    public ClickType clickType()
    {
        return clickType;
    }

    public InventoryClickEvent event()
    {
        return event;
    }

    public PageContext page()
    {
        return page;
    }

    public void openChild(Runnable render)
    {
        menuManager.navigator().openChild(player, render);
    }

    public void back()
    {
        menuManager.navigator().back(player);
    }

    public boolean back(int levels)
    {
        return menuManager.navigator().back(player, levels);
    }

    public void reopen()
    {
        menuManager.navigator().reopen(player);
    }

    public void requestInput(Component prompt, Consumer<String> onSubmit, Runnable onCancel)
    {
        menuManager.input().request(player, prompt, onSubmit, onCancel);
    }
}
