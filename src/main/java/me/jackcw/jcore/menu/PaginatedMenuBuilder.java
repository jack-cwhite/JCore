package me.jackcw.jcore.menu;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class PaginatedMenuBuilder
{
    private final MenuManager menuManager;
    private final String title;
    private final int rows;

    private final List<PaginatedMenu.Entry> entries = new ArrayList<>();

    private ItemStack fillItem;
    private ItemStack previousButtonItem = namedItem(Material.ARROW, "&ePrevious Page");
    private ItemStack nextButtonItem = namedItem(Material.ARROW, "&eNext Page");
    private Consumer<Player> onClose;

    PaginatedMenuBuilder(MenuManager menuManager, String title, int rows)
    {
        if (title == null)
            throw new IllegalArgumentException(
                    "Title cannot be null"
            );

        if (rows < 2 || rows > 6)
            throw new IllegalArgumentException(
                    "Paginated menus need at least 2 rows (content plus navigation)"
            );

        this.menuManager = menuManager;
        this.title = title;
        this.rows = rows;
    }

    public PaginatedMenuBuilder addItem(ItemStack item, MenuClickHandler handler)
    {
        entries.add(new PaginatedMenu.Entry(item, handler));
        return this;
    }

    public PaginatedMenuBuilder fill(ItemStack item)
    {
        this.fillItem = item;
        return this;
    }

    public PaginatedMenuBuilder previousButton(ItemStack item)
    {
        this.previousButtonItem = item;
        return this;
    }

    public PaginatedMenuBuilder nextButton(ItemStack item)
    {
        this.nextButtonItem = item;
        return this;
    }

    public PaginatedMenuBuilder onClose(Consumer<Player> onClose)
    {
        this.onClose = onClose;
        return this;
    }

    public PaginatedMenu build()
    {
        Menu menu = menuManager.builder(title, rows)
                .onClose(onClose)
                .build();

        return new PaginatedMenu(menu, rows, entries, fillItem, previousButtonItem, nextButtonItem);
    }

    private static ItemStack namedItem(Material material, String name)
    {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(LegacyComponentSerializer.legacyAmpersand().deserialize(name));
        item.setItemMeta(meta);

        return item;
    }
}
