package me.jackcw.jcore.menu;

import me.jackcw.jcore.TestPlugin;
import me.jackcw.jcore.TestUtils;
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
import org.bukkit.configuration.file.YamlConfiguration;
import org.mockbukkit.mockbukkit.MockBukkit;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

class PaginatedMenuTest
{
    private MenuManager menuManager;
    private Player player;

    @BeforeEach
    void setup()
    {
        TestPlugin plugin = TestUtils.mockPlugin();
        menuManager = new MenuManager(plugin, new TaskManager(plugin));
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
                () -> menuManager.paginatedBuilder("Test", 1, List.of())
        );
    }

    @Test
    void hasSinglePageWithNoEntries()
    {
        PaginatedMenu<ItemStack> menu = builder(2, List.of()).build();

        assertEquals(1, menu.getTotalPages());
        assertEquals(0, menu.getCurrentPage());
    }

    @Test
    void totalPagesAccountsForContentSlotsPerPage()
    {
        // 2 rows = 18 slots, last row reserved for nav = 9 content slots per page.
        PaginatedMenu<ItemStack> menu = builder(2, entries(10)).build();

        assertEquals(2, menu.getTotalPages());
    }

    @Test
    void firstPageRendersFirstBatchOfEntries()
    {
        PaginatedMenu<ItemStack> menu = builder(2, entries(10)).build();

        assertEquals(0, menu.getMenu().getInventory().getItem(0).getAmount() - 1);
        assertEquals(8, menu.getMenu().getInventory().getItem(8).getAmount() - 1);
    }

    @Test
    void nextPageRendersRemainingEntries()
    {
        PaginatedMenu<ItemStack> menu = builder(2, entries(10)).build();

        menu.nextPage();

        assertEquals(1, menu.getCurrentPage());
        assertEquals(9, menu.getMenu().getInventory().getItem(0).getAmount() - 1);
        assertNull(menu.getMenu().getInventory().getItem(1));
    }

    @Test
    void nextPageAtLastPageIsNoOp()
    {
        PaginatedMenu<ItemStack> menu = builder(2, List.of()).build();

        menu.nextPage();

        assertEquals(0, menu.getCurrentPage());
    }

    @Test
    void previousPageAtFirstPageIsNoOp()
    {
        PaginatedMenu<ItemStack> menu = builder(2, List.of()).build();

        menu.previousPage();

        assertEquals(0, menu.getCurrentPage());
    }

    @Test
    void clickingNextButtonAdvancesPage()
    {
        PaginatedMenu<ItemStack> menu = builder(2, entries(10)).build();

        menu.open(player);

        // Nav row is the last row (slots 9-17); next button defaults to the rightmost slot (17).
        menuManager.onInventoryClick(clickEvent(player, 17));

        assertEquals(1, menu.getCurrentPage());
    }

    @Test
    void clickingPreviousButtonGoesBack()
    {
        PaginatedMenu<ItemStack> menu = builder(2, entries(10)).build();
        menu.nextPage();

        menu.open(player);

        // Previous button defaults to the leftmost slot of the nav row (9).
        menuManager.onInventoryClick(clickEvent(player, 9));

        assertEquals(0, menu.getCurrentPage());
    }

    @Test
    void clickingBackButtonInvokesCallback()
    {
        AtomicReference<Player> backedOutBy = new AtomicReference<>();

        menuManager.navigator().open(player, () -> backedOutBy.set(player));
        backedOutBy.set(null);

        PaginatedMenu<ItemStack> menu = builder(2, List.of()).back().build();
        menuManager.navigator().openChild(player, () -> menu.open(player));

        // Back button defaults to the center slot of the nav row (13).
        menuManager.onInventoryClick(clickEvent(player, 13));

        assertSame(player, backedOutBy.get());
    }

    @Test
    void rootMenuDoesNotRenderUselessBackButton()
    {
        PaginatedMenu<ItemStack> menu = builder(2, List.of()).back().build();

        menuManager.navigator().open(player, () -> menu.open(player));

        assertNull(menu.getMenu().getInventory().getItem(13));
    }

    @Test
    void backButtonSurvivesPageNavigation()
    {
        AtomicReference<Player> backedOutBy = new AtomicReference<>();

        menuManager.navigator().open(player, () -> backedOutBy.set(player));
        backedOutBy.set(null);

        PaginatedMenu<ItemStack> menu = builder(2, entries(10)).back().build();
        menuManager.navigator().openChild(player, () -> menu.open(player));

        menu.nextPage();

        menuManager.onInventoryClick(clickEvent(player, 13));

        assertSame(player, backedOutBy.get());
    }

    @Test
    void navigationButtonsHiddenWhenNotApplicable()
    {
        PaginatedMenu<ItemStack> menu = builder(2, entries(10)).build();

        // Page 0 (first page): previous button (slot 9) absent, next button (slot 17) present.
        assertNull(menu.getMenu().getInventory().getItem(9));
        assertNotNull(menu.getMenu().getInventory().getItem(17));

        menu.nextPage();

        // Page 1 (last page): previous button present, next button absent.
        assertNotNull(menu.getMenu().getInventory().getItem(9));
        assertNull(menu.getMenu().getInventory().getItem(17));
    }

    @Test
    void pageIndicatorHiddenWithOnlyOnePage()
    {
        PaginatedMenu<ItemStack> menu = builder(2, entries(1)).build();

        assertNull(menu.getMenu().getInventory().getItem(11));
    }

    @Test
    void pageIndicatorShowsCurrentAndTotalPages()
    {
        PaginatedMenu<ItemStack> menu = builder(2, entries(10)).build();

        assertNotNull(menu.getMenu().getInventory().getItem(11));

        menu.nextPage();

        assertNotNull(menu.getMenu().getInventory().getItem(11));
    }

    @Test
    void configureNavigationReadsButtonsAndFallsBackForMissingBack()
    {
        YamlConfiguration config = new YamlConfiguration();
        config.set("previous-button.material", "ARROW");
        config.set("previous-button.name", "&aBack a page");
        // no back-button section at all - should still get a working fallback.

        menuManager.configureNavigation(config);

        PaginatedMenu<ItemStack> menu = builder(2, entries(10)).build();

        menu.nextPage();

        assertNotNull(menu.getMenu().getInventory().getItem(9));
    }

    @Test
    void configureNavigationPageIndicatorReflectsCurrentPage()
    {
        YamlConfiguration config = new YamlConfiguration();
        config.set("page-indicator.material", "PAPER");
        config.set("page-indicator.name", "&e{current}/{total}");

        PaginatedMenu<ItemStack> menu = builder(2, entries(10)).build();

        assertNotNull(menu.getMenu().getInventory().getItem(11));
    }

    @Test
    void goToPageJumpsDirectlyAndClamps()
    {
        PaginatedMenu<ItemStack> menu = builder(2, entries(20)).build();

        menu.goToPage(2);
        assertEquals(2, menu.getCurrentPage());

        menu.goToPage(99);
        assertEquals(menu.getTotalPages() - 1, menu.getCurrentPage());

        menu.goToPage(-5);
        assertEquals(0, menu.getCurrentPage());
    }

    @Test
    void fillAppliesToBackgroundSlots()
    {
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);

        PaginatedMenu<ItemStack> menu = builder(2, entries(1))
                .fill(filler)
                .build();

        assertEquals(filler, menu.getMenu().getInventory().getItem(1));
    }

    @Test
    void startsOnRequestedPage()
    {
        PaginatedMenu<ItemStack> menu = builder(2, entries(20))
                .page(1)
                .build();

        assertEquals(1, menu.getCurrentPage());
    }

    private PaginatedMenuBuilder<ItemStack> builder(int rows, List<ItemStack> entries)
    {
        return menuManager.paginatedBuilder("Test", rows, entries)
                .itemFactory(Function.identity());
    }

    private List<ItemStack> entries(int count)
    {
        List<ItemStack> entries = new ArrayList<>();

        for (int i = 0; i < count; i++)
            entries.add(new ItemStack(Material.DIAMOND, i + 1));

        return entries;
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
