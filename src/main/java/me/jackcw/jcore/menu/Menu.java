package me.jackcw.jcore.menu;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

public final class Menu
{
    private final MenuManager menuManager;
    private final Inventory inventory;
    private final Map<Integer, MenuClickHandler> handlers = new HashMap<>();
    private final Map<Integer, Predicate<ItemStack>> editableSlots = new HashMap<>();
    private final Consumer<Player> onClose;

    private PaginatedMenu<?> pagination;

    Menu(MenuManager menuManager, String title, int rows, Consumer<Player> onClose)
    {
        this.menuManager = menuManager;
        this.onClose = onClose;

        MenuHolder holder = new MenuHolder(this);

        this.inventory = Bukkit.createInventory(holder, rows * 9, LegacyComponentSerializer.legacyAmpersand().deserialize(title));

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

    public void setEditableSlot(int slot, ItemStack initial, Predicate<ItemStack> validator)
    {
        inventory.setItem(slot, initial);
        handlers.remove(slot);
        editableSlots.put(slot, validator != null ? validator : item -> true);
    }

    boolean isEditableSlot(int slot)
    {
        return editableSlots.containsKey(slot);
    }

    boolean hasEditableSlots()
    {
        return !editableSlots.isEmpty();
    }

    boolean validateEditable(int slot, ItemStack item)
    {
        Predicate<ItemStack> validator = editableSlots.get(slot);

        if (validator == null)
            return false;

        if (item == null || item.getType() == Material.AIR)
            return true;

        return validator.test(item);
    }

    public Inventory getInventory()
    {
        return inventory;
    }

    void attachPagination(PaginatedMenu<?> pagination)
    {
        this.pagination = pagination;
    }

    void handleClick(InventoryClickEvent event)
    {
        MenuClickHandler handler = handlers.get(event.getSlot());

        if (handler == null)
            return;

        PageContext page = pagination != null ? new PageContext(pagination) : null;

        MenuContext context = new MenuContext(
                menuManager,
                (Player) event.getWhoClicked(),
                event.getSlot(),
                event.getCurrentItem(),
                event.getClick(),
                event,
                page
        );

        handler.onClick(context);
    }

    void handleClose(Player player)
    {
        if (onClose != null)
            onClose.accept(player);
    }
}
