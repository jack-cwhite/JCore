package me.jackcw.jcore.menu;

import me.jackcw.jcore.TestPlugin;
import me.jackcw.jcore.TestUtils;
import me.jackcw.jcore.storage.YamlFile;
import me.jackcw.jcore.task.TaskManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class ConfiguredMenuTest
{
    private TestPlugin plugin;
    private MenuManager menuManager;
    private YamlFile file;
    private Player player;

    @BeforeEach
    void setup()
    {
        plugin = TestUtils.mockPlugin();
        menuManager = new MenuManager(plugin, new TaskManager(plugin));
        file = TestUtils.createYaml(plugin);
        player = MockBukkit.getMock().addPlayer();

        file.set("test-menu.title", "&8Test Menu");
        file.set("test-menu.rows", 1);
        file.set("test-menu.items.list.slot", 2);
        file.set("test-menu.items.list.material", "CHEST");
        file.set("test-menu.items.list.name", "&aList");
        file.set("test-menu.items.create.slot", 6);
        file.set("test-menu.items.create.material", "NETHER_STAR");
        file.set("test-menu.items.create.name", "&aCreate");
    }

    @AfterEach
    void cleanup()
    {
        TestUtils.cleanup();
    }

    @Test
    void hasReflectsWhetherKeyResolvedASlot()
    {
        ConfiguredMenu menu = menuManager.menu(file, "test-menu");

        assertTrue(menu.has("list"));
        assertTrue(menu.has("create"));
        assertFalse(menu.has("missing"));
    }

    @Test
    void itemOnlyRegisteredWhenKeyHasASlot()
    {
        AtomicBoolean clicked = new AtomicBoolean();

        ConfiguredMenu menu = menuManager.menu(file, "test-menu")
                .item("list", context -> clicked.set(true))
                .item("missing", context -> fail("should never be reachable"));

        menu.open(player);

        menuManager.onInventoryClick(clickEvent(player, 2));

        assertTrue(clicked.get());
    }

    @Test
    void fallsBackToDefaultTitleAndRowsWhenSectionMissing()
    {
        ConfiguredMenu menu = menuManager.menu(file, "does-not-exist");

        assertFalse(menu.has("list"));

        assertDoesNotThrow(() -> menu.open(player));
    }

    @Test
    void placeholdersSubstituteIntoConfiguredItem()
    {
        file.set("test-menu.items.dynamic.slot", 4);
        file.set("test-menu.items.dynamic.material", "{material}");
        file.set("test-menu.items.dynamic.name", "&e{label}");

        ConfiguredMenu menu = menuManager.menu(file, "test-menu")
                .item("dynamic", Map.of("material", "DIAMOND", "label", "Hello"), context -> {});

        menu.open(player);

        ItemStack item = player.getOpenInventory().getTopInventory().getItem(4);

        assertEquals(Material.DIAMOND, item.getType());
    }

    @Test
    void placeholdersSubstituteIntoTitle()
    {
        file.set("test-menu.title", "&8{arena-name}");

        ConfiguredMenu menu = menuManager.menu(file, "test-menu")
                .placeholders(Map.of("arena-name", "Colosseum"));

        menu.open(player);

        assertTrue(player.getOpenInventory().getTitle().contains("Colosseum"));
    }

    @Test
    void fallbackItemUsedWhenConfiguredItemIsBroken()
    {
        file.set("test-menu.items.broken.slot", 1);
        file.set("test-menu.items.broken.material", "NOT_A_REAL_MATERIAL");

        ItemStack fallback = new ItemStack(Material.STICK);

        ConfiguredMenu menu = menuManager.menu(file, "test-menu")
                .item("broken", fallback, context -> {});

        menu.open(player);

        ItemStack item = player.getOpenInventory().getTopInventory().getItem(1);

        assertEquals(Material.STICK, item.getType());
    }

    @Test
    void fromConfigurationSectionOverloadWorksWithoutAYamlFile()
    {
        AtomicReference<Player> clickedBy = new AtomicReference<>();

        ConfiguredMenu menu = menuManager.menu(file.getConfig().getConfigurationSection("test-menu"), "test-menu")
                .item("list", context -> clickedBy.set(context.player()));

        menu.open(player);

        menuManager.onInventoryClick(clickEvent(player, 2));

        assertSame(player, clickedBy.get());
    }

    private InventoryClickEvent clickEvent(Player player, int slot)
    {
        InventoryView view = player.getOpenInventory();

        return new InventoryClickEvent(
                view,
                InventoryType.SlotType.CONTAINER,
                slot,
                ClickType.LEFT,
                InventoryAction.PICKUP_ALL
        );
    }
}
