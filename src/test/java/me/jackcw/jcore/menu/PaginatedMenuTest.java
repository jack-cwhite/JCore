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

import static org.junit.jupiter.api.Assertions.*;

class PaginatedMenuTest
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
    void builderRejectsTooFewRows()
    {
        assertThrows(
                IllegalArgumentException.class,
                () -> menuManager.paginatedBuilder("Test", 1)
        );
    }

    @Test
    void hasSinglePageWithNoEntries()
    {
        PaginatedMenu menu = menuManager.paginatedBuilder("Test", 2).build();

        assertEquals(1, menu.getTotalPages());
        assertEquals(0, menu.getCurrentPage());
    }

    @Test
    void totalPagesAccountsForContentSlotsPerPage()
    {
        // 2 rows = 18 slots, last row reserved for nav = 9 content slots per page.
        PaginatedMenuBuilder builder = menuManager.paginatedBuilder("Test", 2);

        for (int i = 0; i < 10; i++)
            builder.addItem(entry(i), null);

        PaginatedMenu menu = builder.build();

        assertEquals(2, menu.getTotalPages());
    }

    @Test
    void firstPageRendersFirstBatchOfEntries()
    {
        PaginatedMenuBuilder builder = menuManager.paginatedBuilder("Test", 2);

        for (int i = 0; i < 10; i++)
            builder.addItem(entry(i), null);

        PaginatedMenu menu = builder.build();

        assertEquals(0, menu.getMenu().getInventory().getItem(0).getAmount() - 1);
        assertEquals(8, menu.getMenu().getInventory().getItem(8).getAmount() - 1);
    }

    @Test
    void nextPageRendersRemainingEntries()
    {
        PaginatedMenuBuilder builder = menuManager.paginatedBuilder("Test", 2);

        for (int i = 0; i < 10; i++)
            builder.addItem(entry(i), null);

        PaginatedMenu menu = builder.build();

        menu.nextPage();

        assertEquals(1, menu.getCurrentPage());
        assertEquals(9, menu.getMenu().getInventory().getItem(0).getAmount() - 1);
        assertNull(menu.getMenu().getInventory().getItem(1));
    }

    @Test
    void nextPageAtLastPageIsNoOp()
    {
        PaginatedMenu menu = menuManager.paginatedBuilder("Test", 2).build();

        menu.nextPage();

        assertEquals(0, menu.getCurrentPage());
    }

    @Test
    void previousPageAtFirstPageIsNoOp()
    {
        PaginatedMenu menu = menuManager.paginatedBuilder("Test", 2).build();

        menu.previousPage();

        assertEquals(0, menu.getCurrentPage());
    }

    @Test
    void clickingNextButtonAdvancesPage()
    {
        PaginatedMenuBuilder builder = menuManager.paginatedBuilder("Test", 2);

        for (int i = 0; i < 10; i++)
            builder.addItem(entry(i), null);

        PaginatedMenu menu = builder.build();

        menu.open(player);

        // Nav row is the last row (slots 9-17); next button defaults to the rightmost slot (17).
        menuManager.onInventoryClick(clickEvent(player, 17));

        assertEquals(1, menu.getCurrentPage());
    }

    @Test
    void clickingPreviousButtonGoesBack()
    {
        PaginatedMenuBuilder builder = menuManager.paginatedBuilder("Test", 2);

        for (int i = 0; i < 10; i++)
            builder.addItem(entry(i), null);

        PaginatedMenu menu = builder.build();
        menu.nextPage();

        menu.open(player);

        // Previous button defaults to the leftmost slot of the nav row (9).
        menuManager.onInventoryClick(clickEvent(player, 9));

        assertEquals(0, menu.getCurrentPage());
    }

    @Test
    void fillAppliesToBackgroundSlots()
    {
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);

        PaginatedMenu menu = menuManager.paginatedBuilder("Test", 2)
                .addItem(entry(0), null)
                .fill(filler)
                .build();

        assertEquals(filler, menu.getMenu().getInventory().getItem(1));
    }

    private ItemStack entry(int index)
    {
        return new ItemStack(Material.DIAMOND, index + 1);
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
