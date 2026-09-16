package me.jackcw.jcore.menu;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.function.Function;

public final class PaginatedMenu<T>
{
    private final MenuManager menuManager;
    private final Menu menu;
    private final int rows;
    private final List<T> entries;
    private final Function<T, ItemStack> itemFactory;
    private final EntryClickHandler<T> clickHandler;
    private final ItemStack fillItem;
    private final boolean showBack;
    private final int contentSlotsPerPage;
    private final int totalPages;
    private int currentPage;
    private Player viewer;

    PaginatedMenu(MenuManager menuManager, Menu menu, int rows, List<T> entries, Function<T, ItemStack> itemFactory, EntryClickHandler<T> clickHandler, ItemStack fillItem, boolean showBack, int startPage)
    {
        this.menuManager = menuManager;
        this.menu = menu;
        this.rows = rows;
        this.entries = entries;
        this.itemFactory = itemFactory;
        this.clickHandler = clickHandler;
        this.fillItem = fillItem;
        this.showBack = showBack;

        this.contentSlotsPerPage = (rows - 1) * 9;
        this.totalPages = Math.max(1, (int) Math.ceil(entries.size() / (double) contentSlotsPerPage));
        this.currentPage = Math.max(0, Math.min(startPage, totalPages - 1));

        menu.attachPagination(this);
        render();
    }

    public void open(Player player)
    {
        viewer = player;
        renderNavigation();
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

    public PaginatedMenu<T> nextPage()
    {
        if (currentPage + 1 >= totalPages)
            return this;

        currentPage++;
        render();

        return this;
    }

    public PaginatedMenu<T> previousPage()
    {
        if (currentPage == 0)
            return this;

        currentPage--;
        render();

        return this;
    }

    public PaginatedMenu<T> goToPage(int page)
    {
        currentPage = Math.max(0, Math.min(page, totalPages - 1));
        render();

        return this;
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

        renderEntries();
        renderNavigation();
    }

    private void renderEntries()
    {
        int start = currentPage * contentSlotsPerPage;
        int end = Math.min(start + contentSlotsPerPage, entries.size());

        int slot = 0;

        for (int i = start; i < end; i++)
        {
            T entry = entries.get(i);
            ItemStack item = itemFactory.apply(entry);

            menu.setItem(slot, item, context -> clickHandler.onClick(context, entry));

            slot++;
        }
    }

    private void renderNavigation()
    {
        MenuNavigationStyle style = menuManager.navigationStyle();

        if (currentPage > 0)
            menu.setItem(MenuNavigationSlots.previousPage(rows), style.previous(), context -> previousPage());

        if (currentPage < totalPages - 1)
            menu.setItem(MenuNavigationSlots.nextPage(rows), style.next(), context -> nextPage());

        if (totalPages > 1)
            menu.setItem(MenuNavigationSlots.pageIndicator(rows), style.pageIndicator(currentPage + 1, totalPages));

        if (showBack && viewer != null && menuManager.navigator().hasHistory(viewer))
            menu.setItem(MenuNavigationSlots.backButton(rows), style.back(), MenuContext::back);
    }
}
