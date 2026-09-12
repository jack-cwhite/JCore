package me.jackcw.jcore.menu;

import me.jackcw.jcore.TestPlugin;
import me.jackcw.jcore.TestUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class MenuManagerTest
{
    private MenuManager menuManager;
    private Player player;

    @BeforeEach
    void setup()
    {
        TestPlugin plugin = TestUtils.mockPlugin();
        menuManager = new MenuManager(plugin);
        player = MockBukkit.getMock().addPlayer();
    }

    @AfterEach
    void cleanup()
    {
        TestUtils.cleanup();
    }

    @Test
    void rejectsNullPlugin()
    {
        assertThrows(
                IllegalArgumentException.class,
                () -> new MenuManager(null)
        );
    }

    @Test
    void clickInMenuIsCancelledAndDispatched()
    {
        AtomicBoolean clicked = new AtomicBoolean();

        Menu menu = menuManager.builder("Test", 1)
                .item(0, new ItemStack(Material.DIAMOND), context -> clicked.set(true))
                .build();

        menu.open(player);

        InventoryClickEvent event = clickEvent(player, 0);

        menuManager.onInventoryClick(event);

        assertTrue(event.isCancelled());
        assertTrue(clicked.get());
    }

    @Test
    void clickOutsideMenuInventoryIsIgnored()
    {
        InventoryClickEvent event = clickEvent(player, 0);

        menuManager.onInventoryClick(event);

        assertFalse(event.isCancelled());
    }

    @Test
    void clickInPlayerInventoryWhileMenuOpenIsCancelledButNotDispatched()
    {
        AtomicBoolean clicked = new AtomicBoolean();

        Menu menu = menuManager.builder("Test", 1)
                .item(0, new ItemStack(Material.DIAMOND), context -> clicked.set(true))
                .build();

        menu.open(player);

        // Raw slot 9 is the first player-inventory slot once a 1-row (9 slot) menu is the top inventory.
        InventoryClickEvent event = clickEvent(player, 9);

        menuManager.onInventoryClick(event);

        assertTrue(event.isCancelled());
        assertFalse(clicked.get());
    }

    @Test
    void dragInMenuIsCancelled()
    {
        Menu menu = menuManager.builder("Test", 1).build();

        menu.open(player);

        InventoryView view = player.getOpenInventory();

        Map<Integer, ItemStack> newItems = new LinkedHashMap<>();
        newItems.put(0, new ItemStack(Material.DIAMOND));

        InventoryDragEvent event = new InventoryDragEvent(
                view,
                null,
                new ItemStack(Material.DIAMOND),
                false,
                newItems
        );

        menuManager.onInventoryDrag(event);

        assertTrue(event.isCancelled());
    }

    @Test
    void closeFiresMenuOnCloseCallback()
    {
        AtomicReference<Player> closedBy = new AtomicReference<>();

        Menu menu = menuManager.builder("Test", 1)
                .onClose(closedBy::set)
                .build();

        menu.open(player);

        InventoryView view = player.getOpenInventory();

        InventoryCloseEvent event = new InventoryCloseEvent(view);

        menuManager.onInventoryClose(event);

        assertSame(player, closedBy.get());
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
