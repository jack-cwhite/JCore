package me.jackcw.jcore.menu;

import me.jackcw.jcore.TestUtils;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MenuNavigatorTest
{
    private Player player;
    private MenuNavigator navigator;

    @BeforeEach
    void setup()
    {
        TestUtils.mockPlugin();
        player = MockBukkit.getMock().addPlayer();
        navigator = new MenuNavigator();
    }

    @AfterEach
    void cleanup()
    {
        TestUtils.cleanup();
    }

    @Test
    void openPathRendersOnlyDestinationAndBuildsBackHistory()
    {
        List<String> rendered = new ArrayList<>();

        navigator.openPath(player, List.of(
                () -> rendered.add("root"),
                () -> rendered.add("list"),
                () -> rendered.add("detail")
        ));

        assertEquals(List.of("detail"), rendered);
        assertTrue(navigator.hasHistory(player));

        assertTrue(navigator.back(player));
        assertEquals(List.of("detail", "list"), rendered);
    }

    @Test
    void multiLevelBackSkipsIntermediateRenderer()
    {
        List<String> rendered = new ArrayList<>();

        navigator.openPath(player, List.of(
                () -> rendered.add("root"),
                () -> rendered.add("list"),
                () -> rendered.add("detail")
        ));

        assertTrue(navigator.back(player, 2));
        assertEquals(List.of("detail", "root"), rendered);
        assertFalse(navigator.hasHistory(player));
    }
}
