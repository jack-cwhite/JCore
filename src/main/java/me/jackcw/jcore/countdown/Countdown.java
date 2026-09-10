package me.jackcw.jcore.countdown;

import me.jackcw.jcore.task.TaskManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;

public final class Countdown
{
    private static final long TICKS_PER_SECOND = 20L;

    private final TaskManager taskManager;
    private final int startSeconds;
    private final List<IntConsumer> onTick;
    private final List<Runnable> onComplete;
    private final List<Runnable> onEnd;

    private int secondsRemaining;
    private BukkitTask task;
    private boolean running;

    private Countdown(TaskManager taskManager, int startSeconds, List<IntConsumer> onTick, List<Runnable> onComplete, List<Runnable> onEnd)
    {
        this.taskManager = taskManager;
        this.startSeconds = startSeconds;
        this.onTick = onTick;
        this.onComplete = onComplete;
        this.onEnd = onEnd;
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

            for (Runnable listener : onComplete)
                listener.run();

            return;
        }

        for (IntConsumer listener : onTick)
            listener.accept(secondsRemaining);

        secondsRemaining--;
    }

    public void cancel()
    {
        if (!running)
            return;

        running = false;

        if (task != null)
        {
            task.cancel();
            task = null;
        }

        for (Runnable listener : onEnd)
            listener.run();
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

        private final List<IntConsumer> onTick = new ArrayList<>();
        private final List<Runnable> onComplete = new ArrayList<>();
        private final List<Runnable> onEnd = new ArrayList<>();

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
            if (onTick == null)
                throw new IllegalArgumentException("onTick cannot be null");

            this.onTick.add(onTick);
            return this;
        }

        public Builder onComplete(Runnable onComplete)
        {
            if (onComplete == null)
                throw new IllegalArgumentException("onComplete cannot be null");

            this.onComplete.add(onComplete);
            return this;
        }

        public Builder bossBar(Collection<? extends Player> players, BarColor color, BarStyle style, IntFunction<String> title)
        {
            if (players == null)
                throw new IllegalArgumentException("Players cannot be null");

            if (color == null)
                throw new IllegalArgumentException("Color cannot be null");

            if (style == null)
                throw new IllegalArgumentException("Style cannot be null");

            IntFunction<String> titleFormatter = title != null
                    ? title
                    : remaining -> "Starting in " + remaining + "...";

            BossBar bossBar = Bukkit.createBossBar("", color, style);

            for (Player player : players)
                bossBar.addPlayer(player);

            onTick(remaining ->
            {
                bossBar.setTitle(titleFormatter.apply(remaining));
                bossBar.setProgress(Math.max(0.0, Math.min(1.0, (double) remaining / seconds)));
            });

            onEnd.add(bossBar::removeAll);

            return this;
        }

        public Builder title(Collection<? extends Player> players, IntFunction<String> title)
        {
            return title(players, title, null);
        }

        public Builder title(Collection<? extends Player> players, IntFunction<String> title, IntFunction<String> subtitle)
        {
            if (players == null)
                throw new IllegalArgumentException("Players cannot be null");

            if (title == null)
                throw new IllegalArgumentException("Title cannot be null");

            onTick(remaining ->
            {
                Component mainComponent = LegacyComponentSerializer.legacyAmpersand().deserialize(title.apply(remaining));

                Component subComponent = subtitle != null
                        ? LegacyComponentSerializer.legacyAmpersand().deserialize(subtitle.apply(remaining))
                        : Component.empty();

                Title displayedTitle = Title.title(mainComponent, subComponent);

                for (Player player : players)
                    player.showTitle(displayedTitle);
            });

            return this;
        }

        public Builder actionBar(Collection<? extends Player> players, IntFunction<String> text)
        {
            if (players == null)
                throw new IllegalArgumentException("Players cannot be null");

            if (text == null)
                throw new IllegalArgumentException("Text cannot be null");

            onTick(remaining ->
            {
                Component component = LegacyComponentSerializer.legacyAmpersand().deserialize(text.apply(remaining));

                for (Player player : players)
                    player.sendActionBar(component);
            });

            return this;
        }

        public Countdown build()
        {
            return new Countdown(
                    taskManager,
                    seconds,
                    List.copyOf(onTick),
                    List.copyOf(onComplete),
                    List.copyOf(onEnd)
            );
        }
    }
}
