package me.jackcw.jcore.message;

import me.jackcw.jcore.storage.YamlFile;
import me.jackcw.jcore.util.StringUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class MessageManager
{
    private static final String PREFIX_PATH = "core.prefix";

    private final JavaPlugin plugin;
    private final YamlFile file;
    private final Map<String, String> messages = new HashMap<>();
    private final Map<String, List<String>> listMessages = new HashMap<>();

    public MessageManager(JavaPlugin plugin, YamlFile file)
    {
        this.plugin = plugin;
        this.file = file;

        file.load();
        load();
    }

    private void load()
    {
        messages.clear();
        listMessages.clear();

        FileConfiguration config = file.getConfig();
        for (String key : config.getKeys(true))
        {
            Object value = config.get(key);

            if (value instanceof ConfigurationSection)
                continue;

            if (value instanceof String string)
                messages.put(key, string);
            else if (value instanceof List<?> list)
            {
                List<String> strings = new ArrayList<>();
                for (Object item : list)
                    strings.add(String.valueOf(item));

                listMessages.put(key, strings);
            }
        }
    }

    public void reload()
    {
        file.reload();
        load();
    }

    public String get(MessageKey key)
    {
        String message = messages.get(key.getPath());

        if (message == null)
            missingMessage(key);

        return message;
    }

    public List<String> getList(MessageKey key)
    {
        List<String> message = listMessages.get(key.getPath());

        if (message == null)
        {
            missingMessage(key);
            return null;
        }

        return message;
    }

    public String getPrefix()
    {
        return messages.get(PREFIX_PATH);
    }

    public void send(CommandSender receiver, MessageKey key, Object... replacers)
    {
        String message = get(key);

        if (message == null)
            return;

        String prefix = getPrefix();

        if (prefix != null && !prefix.isBlank())
            message = prefix + " " + message;

        receiver.sendMessage(StringUtil.replace(message, replacers));
    }

    public void sendList(CommandSender receiver, MessageKey key, Object... replacers)
    {
        List<String> messages = getList(key);

        if (messages == null)
            return;

        for (String message : messages)
            receiver.sendMessage(StringUtil.replace(message, replacers));
    }

    private void missingMessage(MessageKey key)
    {
        plugin.getLogger().warning("Missing message '" + key.getPath() + "' in " + file.getFile().getName());
    }

    public YamlFile getFile()
    {
        return file;
    }
}
