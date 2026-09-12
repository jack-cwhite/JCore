package me.jackcw.jcore.menu;

import me.jackcw.jcore.TestPlugin;
import me.jackcw.jcore.TestUtils;
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

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class MenuTest
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
    void builderRejectsNullTitle()
    {
        assertThrows(
                IllegalArgumentException.class,
                () -> menuManager.builder(null, 1)
        );
    }

    @Test
    void builderRejectsInvalidRows()
    {
        assertThrows(
                IllegalArgumentException.class,
                () -> menuManager.builder("Test", 0)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> menuManager.builder("Test", 7)
        );
    }

    @Test
    void itemRejectsOutOfBoundsSlot()
    {
        MenuBuilder builder = menuManager.builder("Test", 1);

        assertThrows(
                IllegalArgumentException.class,
                () -> builder.item(9, new ItemStack(Material.STONE))
        );
    }

    @Test
    void buildCreatesInventoryWithCorrectSize()
    {
        Menu menu = menuManager.builder("Test", 2).build();

        assertEquals(18, menu.getInventory().getSize());
    }

    @Test
    void itemsArePlacedInInventory()
    {
        ItemStack stack = new ItemStack(Material.DIAMOND);

        Menu menu = menuManager.builder("Test", 1)
                .item(3, stack)
                .build();

        assertEquals(stack, menu.getInventory().getItem(3));
    }

    @Test
    void fillPopulatesEmptySlotsOnly()
    {
        ItemStack content = new ItemStack(Material.DIAMOND);
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);

        Menu menu = menuManager.builder("Test", 1)
                .item(0, content)
                .fill(filler)
                .build();

        assertEquals(content, menu.getInventory().getItem(0));
        assertEquals(filler, menu.getInventory().getItem(1));
        assertEquals(filler, menu.getInventory().getItem(8));
    }

    @Test
    void handleClickInvokesRegisteredHandler()
    {
        AtomicBoolean clicked = new AtomicBoolean();
        AtomicReference<Integer> slotReceived = new AtomicReference<>();

        Menu menu = menuManager.builder("Test", 1)
                .item(2, new ItemStack(Material.DIAMOND), context ->
                {
                    clicked.set(true);
                    slotReceived.set(context.getSlot());
                })
                .build();

        menu.open(player);

        InventoryClickEvent event = clickEvent(player, 2);

        menu.handleClick(event);

        assertTrue(clicked.get());
        assertEquals(2, slotReceived.get());
    }

    @Test
    void handleClickDoesNothingForSlotWithoutHandler()
    {
        Menu menu = menuManager.builder("Test", 1)
                .item(0, new ItemStack(Material.DIAMOND))
                .build();

        menu.open(player);

        assertDoesNotThrow(() -> menu.handleClick(clickEvent(player, 0)));
    }

    @Test
    void removeItemClearsSlotAndHandler()
    {
        AtomicBoolean clicked = new AtomicBoolean();

        Menu menu = menuManager.builder("Test", 1)
                .item(0, new ItemStack(Material.DIAMOND), context -> clicked.set(true))
                .build();

        menu.removeItem(0);

        assertNull(menu.getInventory().getItem(0));

        menu.open(player);
        menu.handleClick(clickEvent(player, 0));

        assertFalse(clicked.get());
    }

    @Test
    void handleCloseFiresOnCloseCallback()
    {
        AtomicReference<Player> closedBy = new AtomicReference<>();

        Menu menu = menuManager.builder("Test", 1)
                .onClose(closedBy::set)
                .build();

        menu.handleClose(player);

        assertSame(player, closedBy.get());
    }

    @Test
    void handleCloseDoesNothingWithoutCallback()
    {
        Menu menu = menuManager.builder("Test", 1).build();

        assertDoesNotThrow(() -> menu.handleClose(player));
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
