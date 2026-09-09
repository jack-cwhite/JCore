package me.jackcw.jcore.storage;

import me.jackcw.jcore.serialization.SerializerManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public final class FileManager
{
    private final JavaPlugin plugin;
    private final SerializerManager serializerManager;
    private final Map<String, YamlFile> files = new HashMap<>();

    public FileManager(JavaPlugin plugin, SerializerManager serializerManager)
    {
        if (plugin == null)
            throw new IllegalArgumentException(
                    "Plugin cannot be null"
            );

        if (serializerManager == null)
            throw new IllegalArgumentException(
                    "Serialization manager cannot be null"
            );

        this.plugin = plugin;
        this.serializerManager = serializerManager;
    }

    public YamlFile yaml(String name)
    {
        return files.computeIfAbsent(name, key -> new YamlFile(plugin, serializerManager, key));
    }

    public YamlFile yaml(String name, boolean copyResource)
    {
        return files.computeIfAbsent(name, key -> new YamlFile(plugin, serializerManager, key, copyResource));
    }

    public void saveAll()
    {
        files.values().forEach(YamlFile::save);
    }

    public void reloadAll()
    {
        files.values().forEach(YamlFile::reload);
    }

    public void close()
    {
        files.clear();
    }
}
