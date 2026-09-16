package me.jackcw.jcore.menu;

import me.jackcw.jcore.item.ItemStackParser;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public final class PaginatedMenuBuilder<T>
{
    private final MenuManager menuManager;
    private final String title;
    private final int rows;
    private final List<T> entries;

    private ConfigurationSection entryTemplate;
    private Function<T, ItemStack> itemFactory = entry -> null;
    private EntryClickHandler<T> clickHandler = (context, entry) -> {};
    private ItemStack fillItem;
    private Consumer<Player> onClose;
    private boolean showBack;
    private int page;
    private Map<String, Object> titlePlaceholders;

    PaginatedMenuBuilder(MenuManager menuManager, String title, int rows, List<T> entries)
    {
        if (title == null)
            throw new IllegalArgumentException("Title cannot be null");

        if (rows < 2 || rows > 6)
            throw new IllegalArgumentException("Paginated menus need at least 2 rows (content plus navigation)");

        this.menuManager = menuManager;
        this.title = title;
        this.rows = rows;
        this.entries = entries;
    }

    PaginatedMenuBuilder<T> entryTemplate(ConfigurationSection entryTemplate)
    {
        this.entryTemplate = entryTemplate;
        return this;
    }

    public PaginatedMenuBuilder<T> item(Function<T, Map<String, Object>> placeholders)
    {
        if (entryTemplate == null)
            throw new IllegalStateException("No entry template configured; use item(ConfigurationSection, Function) instead");

        this.itemFactory = entry -> ItemStackParser.parseSafely(entryTemplate, placeholders.apply(entry));

        return this;
    }

    public PaginatedMenuBuilder<T> item(ConfigurationSection template, Function<T, Map<String, Object>> placeholders)
    {
        this.itemFactory = entry -> ItemStackParser.parseSafely(template, placeholders.apply(entry));
        return this;
    }

    public PaginatedMenuBuilder<T> itemFactory(Function<T, ItemStack> itemFactory)
    {
        this.itemFactory = itemFactory;
        return this;
    }

    public ConfigurationSection entryTemplate()
    {
        return entryTemplate;
    }

    public PaginatedMenuBuilder<T> onClick(EntryClickHandler<T> handler)
    {
        this.clickHandler = handler;
        return this;
    }

    public PaginatedMenuBuilder<T> fill(ItemStack item)
    {
        this.fillItem = item;
        return this;
    }

    public PaginatedMenuBuilder<T> back()
    {
        this.showBack = true;
        return this;
    }

    public PaginatedMenuBuilder<T> onClose(Consumer<Player> onClose)
    {
        this.onClose = onClose;
        return this;
    }

    public PaginatedMenuBuilder<T> page(int page)
    {
        this.page = page;
        return this;
    }
    public PaginatedMenuBuilder<T> placeholders(Map<String, Object> placeholders)
    {
        this.titlePlaceholders = placeholders;
        return this;
    }

    public PaginatedMenu<T> build()
    {
        String resolvedTitle = ItemStackParser.substitute(title, titlePlaceholders);

        Menu menu = menuManager.builder(resolvedTitle, rows).onClose(onClose).build();

        return new PaginatedMenu<>(menuManager, menu, rows, entries, itemFactory, clickHandler, fillItem, showBack, page);
    }

    public void open(Player player)
    {
        build().open(player);
    }
}
