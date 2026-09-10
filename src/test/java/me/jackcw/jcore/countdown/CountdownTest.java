package me.jackcw.jcore.countdown;

import me.jackcw.jcore.TestPlugin;
import me.jackcw.jcore.TestUtils;
import me.jackcw.jcore.task.TaskManager;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class CountdownTest
{
    private TaskManager taskManager;

    @BeforeEach
    void setup()
    {
        TestPlugin plugin = TestUtils.mockPlugin();
        taskManager = new TaskManager(plugin);
    }

    @AfterEach
    void cleanup()
    {
        taskManager.shutdown();
        TestUtils.cleanup();
    }

    @Test
    void builderRejectsNullTaskManager()
    {
        assertThrows(
                IllegalArgumentException.class,
                () -> Countdown.builder(null, 5)
        );
    }

    @Test
    void builderRejectsNonPositiveSeconds()
    {
        assertThrows(
                IllegalArgumentException.class,
                () -> Countdown.builder(taskManager, 0)
        );
    }

    @Test
    void ticksThroughEverySecondThenCompletes()
    {
        List<Integer> ticks = new ArrayList<>();
        AtomicBoolean completed = new AtomicBoolean();

        Countdown countdown = Countdown.builder(taskManager, 3)
                .onTick(ticks::add)
                .onComplete(() -> completed.set(true))
                .build();

        countdown.start();

        MockBukkit.getMock().getScheduler().performTicks(61);

        assertEquals(List.of(3, 2, 1), ticks);
        assertTrue(completed.get());
        assertFalse(countdown.isRunning());
    }

    @Test
    void cancelStopsFurtherTicks()
    {
        List<Integer> ticks = new ArrayList<>();

        Countdown countdown = Countdown.builder(taskManager, 5)
                .onTick(ticks::add)
                .build();

        countdown.start();

        MockBukkit.getMock().getScheduler().performTicks(21);

        countdown.cancel();

        MockBukkit.getMock().getScheduler().performTicks(60);

        assertEquals(List.of(5, 4), ticks);
        assertFalse(countdown.isRunning());
    }

    @Test
    void startIsIdempotentWhileRunning()
    {
        List<Integer> ticks = new ArrayList<>();

        Countdown countdown = Countdown.builder(taskManager, 5)
                .onTick(ticks::add)
                .build();

        countdown.start();
        countdown.start();

        assertTrue(countdown.isRunning());
    }

    @Test
    void bossBarRejectsNullPlayers()
    {
        Countdown.Builder builder = Countdown.builder(taskManager, 5);

        assertThrows(
                IllegalArgumentException.class,
                () -> builder.bossBar(null, BarColor.RED, BarStyle.SOLID, null)
        );
    }

    @Test
    void bossBarRejectsNullColorOrStyle()
    {
        Player player = MockBukkit.getMock().addPlayer();
        Countdown.Builder builder = Countdown.builder(taskManager, 5);

        assertThrows(
                IllegalArgumentException.class,
                () -> builder.bossBar(Set.of(player), null, BarStyle.SOLID, null)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> builder.bossBar(Set.of(player), BarColor.RED, null, null)
        );
    }

    @Test
    void bossBarRunsWithoutErrorAndCleansUpOnCompletion()
    {
        Player player = MockBukkit.getMock().addPlayer();

        Countdown countdown = Countdown.builder(taskManager, 2)
                .bossBar(Set.of(player), BarColor.RED, BarStyle.SOLID, remaining -> remaining + "s left")
                .build();

        countdown.start();

        assertDoesNotThrow(() -> MockBukkit.getMock().getScheduler().performTicks(61));
        assertFalse(countdown.isRunning());
    }

    @Test
    void titleRejectsNullPlayersOrTitle()
    {
        Player player = MockBukkit.getMock().addPlayer();
        Countdown.Builder builder = Countdown.builder(taskManager, 5);

        assertThrows(
                IllegalArgumentException.class,
                () -> builder.title(null, remaining -> "" + remaining)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> builder.title(Set.of(player), null)
        );
    }

    @Test
    void titleRunsWithoutError()
    {
        Player player = MockBukkit.getMock().addPlayer();

        Countdown countdown = Countdown.builder(taskManager, 2)
                .title(Set.of(player), remaining -> "" + remaining, remaining -> "get ready")
                .build();

        countdown.start();

        assertDoesNotThrow(() -> MockBukkit.getMock().getScheduler().performTicks(61));
    }

    @Test
    void actionBarRejectsNullPlayersOrText()
    {
        Player player = MockBukkit.getMock().addPlayer();
        Countdown.Builder builder = Countdown.builder(taskManager, 5);

        assertThrows(
                IllegalArgumentException.class,
                () -> builder.actionBar(null, remaining -> "" + remaining)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> builder.actionBar(Set.of(player), null)
        );
    }

    @Test
    void actionBarSendsFormattedTextEachTick()
    {
        PlayerMock player = MockBukkit.getMock().addPlayer();

        Countdown countdown = Countdown.builder(taskManager, 2)
                .actionBar(Set.of(player), remaining -> "Starting in " + remaining)
                .build();

        countdown.start();

        MockBukkit.getMock().getScheduler().performTicks(1);

        assertEquals(
                "Starting in 2",
                PlainTextComponentSerializer.plainText().serialize(player.nextActionBar())
        );

        MockBukkit.getMock().getScheduler().performTicks(20);

        assertEquals(
                "Starting in 1",
                PlainTextComponentSerializer.plainText().serialize(player.nextActionBar())
        );
    }
}
