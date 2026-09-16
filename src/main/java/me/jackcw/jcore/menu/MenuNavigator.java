package me.jackcw.jcore.menu;

import org.bukkit.entity.Player;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.List;
import java.util.UUID;

public final class MenuNavigator
{
    private final Map<UUID, Runnable> current = new HashMap<>();
    private final Map<UUID, Deque<Runnable>> history = new HashMap<>();

    public void open(Player player, Runnable render)
    {
        Objects.requireNonNull(player, "Player cannot be null");
        Objects.requireNonNull(render, "Render action cannot be null");

        UUID id = player.getUniqueId();

        history.remove(id);
        current.put(id, render);

        render.run();
    }

    public void openPath(Player player, List<Runnable> path)
    {
        Objects.requireNonNull(player, "Player cannot be null");
        Objects.requireNonNull(path, "Navigation path cannot be null");

        if (path.isEmpty() || path.stream().anyMatch(Objects::isNull))
            throw new IllegalArgumentException("Navigation path must contain non-null screens");

        UUID id = player.getUniqueId();
        Deque<Runnable> stack = new ArrayDeque<>();

        for (int i = 0; i < path.size() - 1; i++)
            stack.push(path.get(i));

        if (stack.isEmpty())
            history.remove(id);
        else
            history.put(id, stack);

        Runnable render = path.getLast();
        current.put(id, render);

        render.run();
    }

    public void openChild(Player player, Runnable render)
    {
        Objects.requireNonNull(player, "Player cannot be null");
        Objects.requireNonNull(render, "Render action cannot be null");

        UUID id = player.getUniqueId();
        Runnable previous = current.get(id);

        if (previous != null)
            history.computeIfAbsent(id, key -> new ArrayDeque<>()).push(previous);

        current.put(id, render);

        render.run();
    }

    public boolean back(Player player)
    {
        return back(player, 1);
    }

    public boolean back(Player player, int levels)
    {
        Objects.requireNonNull(player, "Player cannot be null");

        if (levels < 1)
            throw new IllegalArgumentException("Levels must be at least 1");

        UUID id = player.getUniqueId();
        Deque<Runnable> stack = history.get(id);

        if (stack == null || stack.size() < levels)
            return false;

        Runnable render = null;

        for (int i = 0; i < levels; i++)
            render = stack.pop();

        if (stack.isEmpty())
            history.remove(id);

        current.put(id, render);
        render.run();

        return true;
    }

    public void reopen(Player player)
    {
        Objects.requireNonNull(player, "Player cannot be null");

        Runnable render = current.get(player.getUniqueId());

        if (render != null)
            render.run();
    }

    public boolean hasHistory(Player player)
    {
        Deque<Runnable> stack = history.get(player.getUniqueId());

        return stack != null && !stack.isEmpty();
    }

    public void clear(Player player)
    {
        UUID id = player.getUniqueId();

        history.remove(id);
        current.remove(id);
    }
}
