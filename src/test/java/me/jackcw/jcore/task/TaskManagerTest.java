package me.jackcw.jcore.task;

import me.jackcw.jcore.TestPlugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;

import java.util.concurrent.CompletionException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class TaskManagerTest
{
    private TestPlugin plugin;
    private TaskManager taskManager;

    @BeforeEach
    void setup()
    {
        MockBukkit.mock();
        plugin = MockBukkit.load(TestPlugin.class);
        taskManager = new TaskManager(plugin);
    }

    @AfterEach
    void cleanup()
    {
        taskManager.shutdown();
        MockBukkit.unmock();
    }

    @Test
    void runAsyncExecutesTask() throws InterruptedException
    {
        CountDownLatch latch = new CountDownLatch(1);

        taskManager.runAsync(latch::countDown);

        assertTrue(latch.await(2, TimeUnit.SECONDS));
    }

    @Test
    void runAsyncFutureCompletes()
    {
        AtomicBoolean ran = new AtomicBoolean();

        taskManager.runAsyncFuture(() -> ran.set(true)).join();

        assertTrue(ran.get());
    }

    @Test
    void submitAsyncReturnsValue()
    {
        int result = taskManager.submitAsync(() -> 21 * 2).join();

        assertEquals(42, result);
    }

    @Test
    void submitAsyncWrapsExceptions()
    {
        var future = taskManager.submitAsync(() ->
        {
            throw new Exception("boom");
        });

        CompletionException exception = assertThrows(
                CompletionException.class,
                future::join
        );

        assertEquals("boom", exception.getCause().getMessage());
    }

    @Test
    void runSyncExecutesOnServerScheduler()
    {
        AtomicBoolean ran = new AtomicBoolean();

        taskManager.runSync(() -> ran.set(true));

        MockBukkit.getMock().getScheduler().performOneTick();

        assertTrue(ran.get());
    }

    @Test
    void runSyncTimerFiresRepeatedly()
    {
        AtomicInteger runs = new AtomicInteger();

        var task = taskManager.runSyncTimer(runs::incrementAndGet, 0L, 1L);

        MockBukkit.getMock().getScheduler().performTicks(3);

        assertTrue(runs.get() >= 3);

        task.cancel();
    }

    @Test
    void executorServiceIsExposed()
    {
        assertNotNull(taskManager.executorService());
    }

    @Test
    void shutdownStopsAcceptingNewTasks()
    {
        taskManager.shutdown();

        assertTrue(taskManager.executorService().isShutdown());
    }
}
