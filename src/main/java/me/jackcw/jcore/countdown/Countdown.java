package me.jackcw.jcore.countdown;

import me.jackcw.jcore.task.TaskManager;
import org.bukkit.scheduler.BukkitTask;

import java.util.function.IntConsumer;

public final class Countdown
{
    private static final long TICKS_PER_SECOND = 20L;

    private final TaskManager taskManager;
    private final int startSeconds;
    private final IntConsumer onTick;
    private final Runnable onComplete;

    private int secondsRemaining;
    private BukkitTask task;
    private boolean running;

    private Countdown(TaskManager taskManager, int startSeconds, IntConsumer onTick, Runnable onComplete)
    {
        this.taskManager = taskManager;
        this.startSeconds = startSeconds;
        this.onTick = onTick;
        this.onComplete = onComplete;
    }

    public static Builder builder(TaskManager taskManager, int seconds)
    {
        return new Builder(taskManager, seconds);
    }

    public void start()
    {
        if (running)
            return;

        running = true;
        secondsRemaining = startSeconds;

        task = taskManager.runSyncTimer(this::tick, 0L, TICKS_PER_SECOND);
    }

    private void tick()
    {
        if (secondsRemaining <= 0)
        {
            cancel();

            if (onComplete != null)
                onComplete.run();

            return;
        }

        if (onTick != null)
            onTick.accept(secondsRemaining);

        secondsRemaining--;
    }

    public void cancel()
    {
        running = false;

        if (task != null)
        {
            task.cancel();
            task = null;
        }
    }

    public boolean isRunning()
    {
        return running;
    }

    public int getSecondsRemaining()
    {
        return secondsRemaining;
    }

    public static final class Builder
    {
        private final TaskManager taskManager;
        private final int seconds;

        private IntConsumer onTick;
        private Runnable onComplete;

        private Builder(TaskManager taskManager, int seconds)
        {
            if (taskManager == null)
                throw new IllegalArgumentException(
                        "Task manager cannot be null"
                );

            if (seconds <= 0)
                throw new IllegalArgumentException(
                        "Seconds must be greater than 0"
                );

            this.taskManager = taskManager;
            this.seconds = seconds;
        }

        public Builder onTick(IntConsumer onTick)
        {
            this.onTick = onTick;
            return this;
        }

        public Builder onComplete(Runnable onComplete)
        {
            this.onComplete = onComplete;
            return this;
        }

        public Countdown build()
        {
            return new Countdown(taskManager, seconds, onTick, onComplete);
        }
    }
}
