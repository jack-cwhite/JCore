package me.jackcw.jcore.menu;

import me.jackcw.jcore.storage.YamlFile;
import me.jackcw.jcore.task.TaskManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public final class MenuManager implements Listener
{
    private static final Logger LOGGER = Logger.getLogger(MenuManager.class.getName());
    private static final String DEFAULT_TITLE = "Menu";
    private static final int DEFAULT_ROWS = 1;

    private final MenuNavigator navigator = new MenuNavigator();
    private final TextInputManager input;
    private MenuNavigationStyle navigationStyle = MenuNavigationStyle.defaults();
    private YamlFile menusFile;

    public MenuManager(JavaPlugin plugin, TaskManager taskManager)
    {
        if (plugin == null)
            throw new IllegalArgumentException("Plugin cannot be null");

        this.input = new TextInputManager(plugin, taskManager);

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void configure(YamlFile menusFile)
    {
        this.menusFile = menusFile;

        configureNavigation(menusFile.getConfig().getConfigurationSection("navigation"));
    }

    public MenuBuilder builder(String title, int rows)
    {
        return new MenuBuilder(this, title, rows);
    }

    public ConfiguredMenu menu(String key)
    {
        return menu(requireMenusFile(), key);
    }

    public ConfiguredMenu menu(YamlFile file, String key)
    {
        return menu(file.getConfig().getConfigurationSection(key), key);
    }

    public ConfiguredMenu menu(ConfigurationSection section, String key)
    {
        if (section == null)
            LOGGER.warning("Menu '" + key + "' is not configured; using fallback title/rows");

        int rows = section != null ? section.getInt("rows", DEFAULT_ROWS) : DEFAULT_ROWS;
        String title = section != null ? section.getString("title", DEFAULT_TITLE) : DEFAULT_TITLE;
        ConfigurationSection items = section != null ? section.getConfigurationSection("items") : null;
        Map<String, Integer> slots = MenuSlotResolver.resolve(rows, items);

        return new ConfiguredMenu(this, builder(title, rows), title, items, slots, rows);
    }

    public <T> PaginatedMenuBuilder<T> paginatedBuilder(String title, int rows, List<T> entries)
    {
        return new PaginatedMenuBuilder<>(this, title, rows, entries);
    }

    public ConfirmMenuBuilder confirm()
    {
        return new ConfirmMenuBuilder(this);
    }

    public <T> PaginatedMenuBuilder<T> paginatedMenu(String key, List<T> entries)
    {
        return paginatedMenu(requireMenusFile(), key, entries);
    }

    public <T> PaginatedMenuBuilder<T> paginatedMenu(YamlFile file, String key, List<T> entries)
    {
        return paginatedMenu(file.getConfig().getConfigurationSection(key), entries);
    }

    public <T> PaginatedMenuBuilder<T> paginatedMenu(ConfigurationSection section, List<T> entries)
    {
        int rows = section != null ? section.getInt("rows", 6) : 6;
        String title = section != null ? section.getString("title", DEFAULT_TITLE) : DEFAULT_TITLE;
        ConfigurationSection entryTemplate = section != null ? section.getConfigurationSection("entry") : null;

        return new PaginatedMenuBuilder<T>(this, title, rows, entries).entryTemplate(entryTemplate);
    }

    private YamlFile requireMenusFile()
    {
        if (menusFile == null)
            throw new IllegalStateException("No menus file configured; call configure(YamlFile) first");

        return menusFile;
    }

    public MenuNavigator navigator()
    {
        return navigator;
    }

    public void open(Player player, Runnable render)
    {
        navigator.open(player, render);
    }

    public void openPath(Player player, List<Runnable> path)
    {
        navigator.openPath(player, path);
    }

    public TextInputManager input()
    {
        return input;
    }

    public void configureNavigation(ConfigurationSection section)
    {
        this.navigationStyle = MenuNavigationStyle.from(section);
    }

    public MenuNavigationStyle navigationStyle()
    {
        return navigationStyle;
    }

    public void shutdown()
    {
        for (Player player : Bukkit.getOnlinePlayers())
        {
            Inventory topInventory = player.getOpenInventory().getTopInventory();

            if (topInventory != null && topInventory.getHolder() instanceof MenuHolder)
                player.closeInventory();

            navigator.clear(player);
        }

        input.clear();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event)
    {
        Inventory topInventory = event.getView().getTopInventory();

        if (!(topInventory.getHolder() instanceof MenuHolder holder))
            return;

        Menu menu = holder.getMenu();
        boolean clickedTop = event.getClickedInventory() != null && event.getClickedInventory().equals(topInventory);

        if (!clickedTop)
        {
            if (!menu.hasEditableSlots())
                event.setCancelled(true);
            else if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY || isDropAction(event.getAction()))
                event.setCancelled(true);

            return;
        }

        int slot = event.getSlot();

        if (menu.isEditableSlot(slot))
        {
            if (isDropAction(event.getAction()) || !menu.validateEditable(slot, resultingItem(event)))
                event.setCancelled(true);

            return;
        }

        event.setCancelled(true);
        menu.handleClick(event);
    }

    private ItemStack resultingItem(InventoryClickEvent event)
    {
        return switch (event.getAction())
        {
            case PLACE_ALL, PLACE_SOME, PLACE_ONE, SWAP_WITH_CURSOR -> event.getCursor();
            case HOTBAR_SWAP, HOTBAR_MOVE_AND_READD ->
                    event.getHotbarButton() >= 0
                            ? ((Player) event.getWhoClicked()).getInventory().getItem(event.getHotbarButton())
                            : ((Player) event.getWhoClicked()).getInventory().getItemInOffHand();
            case PICKUP_ALL, PICKUP_HALF, PICKUP_ONE, PICKUP_SOME, COLLECT_TO_CURSOR, CLONE_STACK, NOTHING -> null;
            default -> event.getCursor();
        };
    }

    private boolean isDropAction(InventoryAction action)
    {
        return action == InventoryAction.DROP_ALL_CURSOR
                || action == InventoryAction.DROP_ONE_CURSOR
                || action == InventoryAction.DROP_ALL_SLOT
                || action == InventoryAction.DROP_ONE_SLOT;
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event)
    {
        Inventory topInventory = event.getView().getTopInventory();

        if (!(topInventory.getHolder() instanceof MenuHolder holder))
            return;

        Menu menu = holder.getMenu();
        int topSize = topInventory.getSize();

        for (Map.Entry<Integer, ItemStack> entry : event.getNewItems().entrySet())
        {
            int rawSlot = entry.getKey();

            if (rawSlot >= topSize)
                continue;

            if (!menu.isEditableSlot(rawSlot) || !menu.validateEditable(rawSlot, entry.getValue()))
            {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event)
    {
        Inventory topInventory = event.getView().getTopInventory();

        if (topInventory.getHolder() instanceof MenuHolder holder)
            holder.getMenu().handleClose((Player) event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event)
    {
        navigator.clear(event.getPlayer());
    }
}
