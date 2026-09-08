package me.jackcw.jcore.storage;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public final class YamlFile
{
    private final JavaPlugin plugin;
    private final File file;
    private final boolean copyResource;

    private FileConfiguration config;

    public YamlFile(JavaPlugin plugin, String name)
    {
        this(plugin, name, false);
    }

    public YamlFile(JavaPlugin plugin, String name, boolean copyResource)
    {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), name);
        this.copyResource = copyResource;

        load();
    }

    public void load()
    {
        if (!file.exists())
        {
            if (copyResource)
            {
                plugin.saveResource(file.getName(), false);
            }
            else
            {
                try
                {
                    File parent = file.getParentFile();

                    if (parent != null && !parent.exists())
                        parent.mkdirs();

                    file.createNewFile();
                }
                catch (IOException e)
                {
                    throw new RuntimeException(
                            "Could not create " + file.getName(), e
                    );
                }
            }
        }

        config = YamlConfiguration.loadConfiguration(file);
    }

    public void save()
    {
        try
        {
            config.save(file);
        }
        catch (IOException e)
        {
            throw new RuntimeException(
                    "Could not save " + file.getName(), e
            );
        }
    }

    public void reload()
    {
        load();
    }

    public FileConfiguration getConfig()
    {
        if (config == null)
        {
            throw new IllegalStateException(
                    "YamlFile has not been loaded."
            );
        }

        return config;
    }

    public File getFile()
    {
        return file;
    }
}
