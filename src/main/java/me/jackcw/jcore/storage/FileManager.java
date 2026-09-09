package me.jackcw.jcore.storage;

import me.jackcw.jcore.serialization.SerializerManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class FileManager
{
    private final JavaPlugin plugin;
    private final SerializerManager serializerManager;

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
        return new YamlFile(plugin, serializerManager, name);
    }

    public YamlFile yaml(String name, boolean copyResource)
    {
        return new YamlFile(plugin, serializerManager, name, copyResource);
    }
}
