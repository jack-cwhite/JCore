package me.jackcw.jcore.task;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.concurrent.*;

public final class TaskManager
{
    private final JavaPlugin plugin;
    private final ExecutorService asyncExecutor;

    public TaskManager(JavaPlugin plugin)
    {
        this.plugin = plugin;
        this.asyncExecutor = Executors.newCachedThreadPool();
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

    public ExecutorService executorService()
    {
        return asyncExecutor;
    }

    public void shutdown()
    {
        asyncExecutor.shutdown();
    }
}
