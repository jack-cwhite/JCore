package me.jackcw.jcore.task;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public final class TaskManager
{
    private static final int ASYNC_THREADS = Math.max(2, Math.min(8, Runtime.getRuntime().availableProcessors()));
    private static final AtomicInteger THREAD_NUMBER = new AtomicInteger();

    private final JavaPlugin plugin;
    private final ExecutorService asyncExecutor;

    public TaskManager(JavaPlugin plugin)
    {
        this.plugin = plugin;
        this.asyncExecutor = Executors.newFixedThreadPool(ASYNC_THREADS, runnable ->
        {
            Thread thread = new Thread(runnable, "JCore-Async-" + THREAD_NUMBER.incrementAndGet());
            thread.setDaemon(true);
            return thread;
        });
    }

    public void runAsync(Runnable task)
    {
        asyncExecutor.execute(task);
    }

    public CompletableFuture<Void> runAsyncFuture(Runnable task)
    {
        return CompletableFuture.runAsync(
                task,
                asyncExecutor
        );
    }

    public <T> CompletableFuture<T> submitAsync(Callable<T> task)
    {
        return CompletableFuture.supplyAsync(
                () ->
                {
                    try
                    {
                        return task.call();
                    }
                    catch (Exception e)
                    {
                        throw new CompletionException(e);
                    }
                },
                asyncExecutor
        );
    }

    public void runSync(Runnable task)
    {
        plugin.getServer().getScheduler().runTask(
                plugin,
                task
        );
    }

    public BukkitTask runSyncTimer(Runnable task, long delayTicks, long periodTicks)
    {
        return plugin.getServer().getScheduler().runTaskTimer(
                plugin,
                task,
                delayTicks,
                periodTicks
        );
    }

    public BukkitTask runSyncLater(Runnable task, long delayTicks)
    {
        return plugin.getServer().getScheduler().runTaskLater(
                plugin,
                task,
                delayTicks
        );
    }

    public ExecutorService executorService()
    {
        return asyncExecutor;
    }

    public void shutdown()
    {
        asyncExecutor.shutdown();

        try
        {
            if (!asyncExecutor.awaitTermination(5, TimeUnit.SECONDS))
                asyncExecutor.shutdownNow();
        }
        catch (InterruptedException e)
        {
            asyncExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
