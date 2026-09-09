package me.jackcw.jcore.storage;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public final class YamlFile
{
    private final JavaPlugin plugin;
    private final String name;
    private final File file;
    private final boolean copyResource;

    private FileConfiguration config;

    public YamlFile(JavaPlugin plugin, String name)
    {
        this(plugin, name, false);
    }

    public YamlFile(JavaPlugin plugin, String name, boolean copyResource)
    {
        if (plugin == null)
            throw new IllegalArgumentException(
                    "Plugin cannot be null"
            );

        if (name == null || name.isBlank())
            throw new IllegalArgumentException(
                    "Name cannot be null or blank"
            );

        this.plugin = plugin;
        this.name = name;
        this.file = new File(plugin.getDataFolder(), name);
        this.copyResource = copyResource;

        load();
    }

    public void load()
    {
        if (!file.exists())
        {
            if (copyResource)
                createFromResource();
            else
                createFile();
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
                    "Could not save " + name, e
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

    private void createFile()
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
                    "Could not create: " + name, e
            );
        }
    }

    private void createFromResource()
    {
        File parent = file.getParentFile();

        if (parent != null && !parent.exists() && !parent.mkdirs())
            throw new RuntimeException(
                    "Could not create directory for " + name
            );

        try
        {
            plugin.saveResource(name, false);
        }
        catch (IllegalArgumentException e)
        {
            throw new RuntimeException(
                    "Could not copy resource " + name, e
            );
        }
    }

    public boolean updateDefaults()
    {
        if (!file.exists())
            createFile();

        try (InputStream resource = plugin.getResource(name))
        {
            if (resource == null)
                return false;

            return new YamlDefaultsMerger().merge(file, resource);
        }
        catch (IOException e)
        {
            throw new RuntimeException(
                    "Could not read defaults for " + name,
                    e
            );
        }
    }

    private boolean mergeDefaults(ConfigurationSection defaults, ConfigurationSection target)
    {
        boolean changed = false;

        for (String key : defaults.getKeys(false))
        {
            if (!target.contains(key))
            {
                target.set(key, defaults.get(key));
                changed = true;
                continue;
            }

            if (defaults.isConfigurationSection(key) && target.isConfigurationSection(key))
            {
                boolean nestedChanged = mergeDefaults(defaults.getConfigurationSection(key), target.getConfigurationSection(key));

                if (nestedChanged)
                    changed = true;
            }
        }

        return changed;
    }
}
