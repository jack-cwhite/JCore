package me.jackcw.jcore.countdown;

import me.jackcw.jcore.TestPlugin;
import me.jackcw.jcore.TestUtils;
import me.jackcw.jcore.task.TaskManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;

import java.util.ArrayList;
import java.util.List;
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
}
