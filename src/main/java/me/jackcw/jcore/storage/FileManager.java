package me.jackcw.jcore.storage;

import org.bukkit.plugin.java.JavaPlugin;

public final class FileManager
{
    private final JavaPlugin plugin;

    public FileManager(JavaPlugin plugin)
    {
        if (plugin == null)
            throw new IllegalArgumentException(
                    "Plugin cannot be null"
            );

        this.plugin = plugin;
    }

    public YamlFile yaml(String name)
    {
        return new YamlFile(plugin, name);
    }

    public YamlFile yaml(String name, boolean copyResource)
    {
        return new YamlFile(plugin, name, copyResource);
    }
}
