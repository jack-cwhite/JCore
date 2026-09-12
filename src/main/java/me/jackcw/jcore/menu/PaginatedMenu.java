package me.jackcw.jcore.menu;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public final class PaginatedMenu
{
    private final Menu menu;
    private final int rows;
    private final List<Entry> entries;
    private final ItemStack fillItem;
    private final ItemStack previousButtonItem;
    private final ItemStack nextButtonItem;

    private final int contentSlotsPerPage;
    private final int totalPages;

    private int currentPage;

    PaginatedMenu(Menu menu, int rows, List<Entry> entries, ItemStack fillItem, ItemStack previousButtonItem, ItemStack nextButtonItem)
    {
        this.menu = menu;
        this.rows = rows;
        this.entries = entries;
        this.fillItem = fillItem;
        this.previousButtonItem = previousButtonItem;
        this.nextButtonItem = nextButtonItem;

        this.contentSlotsPerPage = (rows - 1) * 9;
        this.totalPages = Math.max(1, (int) Math.ceil(entries.size() / (double) contentSlotsPerPage));

        render();
    }

    public void open(Player player)
    {
        menu.open(player);
    }

    public int getCurrentPage()
    {
        return currentPage;
    }

    public int getTotalPages()
    {
        return totalPages;
    }

    public void nextPage()
    {
        if (currentPage + 1 >= totalPages)
            return;

        currentPage++;
        render();
    }

    public void previousPage()
    {
        if (currentPage == 0)
            return;

        currentPage--;
        render();
    }

    public Menu getMenu()
    {
        return menu;
    }

    private void render()
    {
        int size = rows * 9;

        for (int slot = 0; slot < size; slot++)
            menu.removeItem(slot);

        if (fillItem != null)
            for (int slot = 0; slot < size; slot++)
                menu.setItem(slot, fillItem);

        int start = currentPage * contentSlotsPerPage;
        int end = Math.min(start + contentSlotsPerPage, entries.size());

        for (int i = start; i < end; i++)
        {
            Entry entry = entries.get(i);
            int slot = i - start;

            menu.setItem(slot, entry.item(), entry.handler());
        }

        int navRow = size - 9;

        menu.setItem(navRow, previousButtonItem, context -> previousPage());
        menu.setItem(navRow + 8, nextButtonItem, context -> nextPage());
    }

    record Entry(ItemStack item, MenuClickHandler handler)
    {
    }
}
