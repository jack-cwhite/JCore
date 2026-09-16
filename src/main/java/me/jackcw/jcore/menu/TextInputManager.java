package me.jackcw.jcore.menu;

import io.papermc.paper.event.player.AsyncChatEvent;
import me.jackcw.jcore.task.TaskManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Generic chat-based text capture for menu actions that need arbitrary
 * player input (renaming something, naming a new object, etc). Closes the
 * player's open inventory, waits for their next chat message, and resumes
 * on the main thread - either via {@code onSubmit} or, if the player types
 * the cancel keyword, {@code onCancel}.
 */
public final class TextInputManager implements Listener
{
    private static final String CANCEL_KEYWORD = "cancel";

    private final TaskManager taskManager;
    // Chat events are asynchronous while requests are created on the server
    // thread, so this map must support access from both.
    private final Map<UUID, Request> pending = new ConcurrentHashMap<>();

    public TextInputManager(JavaPlugin plugin, TaskManager taskManager)
    {
        this.taskManager = taskManager;

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void request(Player player, Component prompt, Consumer<String> onSubmit, Runnable onCancel)
    {
        pending.put(player.getUniqueId(), new Request(onSubmit, onCancel));

        player.closeInventory();
        player.sendMessage(prompt);
    }

    @EventHandler
    public void onChat(AsyncChatEvent event)
    {
        Player player = event.getPlayer();
        Request request = pending.remove(player.getUniqueId());

        if (request == null)
            return;

        event.setCancelled(true);

        String text = PlainTextComponentSerializer.plainText().serialize(event.message()).trim();

        taskManager.runSync(() ->
        {
            if (text.equalsIgnoreCase(CANCEL_KEYWORD))
            {
                if (request.onCancel != null)
                    request.onCancel.run();

                return;
            }

            request.onSubmit.accept(text);
        });
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event)
    {
        pending.remove(event.getPlayer().getUniqueId());
    }

    void clear()
    {
        pending.clear();
    }

    private record Request(Consumer<String> onSubmit, Runnable onCancel)
    {
    }
}
