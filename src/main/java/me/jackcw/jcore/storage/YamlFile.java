package me.jackcw.jcore.storage;

import me.jackcw.jcore.serialization.Serializer;
import me.jackcw.jcore.serialization.SerializerManager;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class YamlFile
{
    private final JavaPlugin plugin;
    private final SerializerManager serializerManager;
    private final String name;
    private final File file;
    private final boolean copyResource;

    private FileConfiguration config;

    public YamlFile(JavaPlugin plugin, SerializerManager serializerManager, String name)
    {
        this(plugin, serializerManager, name, false);
    }

    public YamlFile(JavaPlugin plugin, SerializerManager serializerManager, String name, boolean copyResource)
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
        this.serializerManager = serializerManager;

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

    public String getString(String path)
    {
        return getConfig().getString(path);
    }

    public String getString(String path, String def)
    {
        return getConfig().getString(path, def);
    }

    public int getInt(String path)
    {
        return getConfig().getInt(path);
    }

    public int getInt(String path, int def)
    {
        return getConfig().getInt(path, def);
    }

    public boolean getBoolean(String path)
    {
        return getConfig().getBoolean(path);
    }

    public boolean getBoolean(String path, boolean def)
    {
        return getConfig().getBoolean(path, def);
    }

    public long getLong(String path)
    {
        return getConfig().getLong(path);
    }

    public long getLong(String path, long def)
    {
        return getConfig().getLong(path, def);
    }

    public double getDouble(String path)
    {
        return getConfig().getDouble(path);
    }

    public double getDouble(String path, double def)
    {
        return getConfig().getDouble(path, def);
    }

    public boolean contains(String path)
    {
        return getConfig().contains(path);
    }

    public List<String> getStringList(String path)
    {
        return getConfig().getStringList(path);
    }

    public void set(String path, Object value)
    {
        getConfig().set(path, value);
    }

    public <T> T get(String path, Class<T> type)
    {
        Object value = getConfig().get(path);

        if (value == null)
            return null;

        if (value instanceof ConfigurationSection section)
            value = sectionToMap(section);

        if (type.isInstance(value))
            return type.cast(value);

        Serializer<T> serializer = serializerManager.get(type);

        if (serializer != null)
            return serializer.deserialize(value);

        throw new IllegalStateException(
                "Value at '" + path + "'is not of type " + type.getName()
        );
    }

    private Map<String, Object> sectionToMap(ConfigurationSection section)
    {
        Map<String, Object> map = new HashMap<>();

        for (String key : section.getKeys(false))
        {
            Object value = section.get(key);

            if (value instanceof ConfigurationSection child)
                value = sectionToMap(child);
            else if (value instanceof List<?> list)
                value = convertList(list);

            map.put(key, value);
        }

        return map;
    }

    private List<Object> convertList(List<?> list)
    {
        List<Object> converted = new ArrayList<>();

        for (Object value : list)
        {
            if (value instanceof ConfigurationSection section)
                converted.add(sectionToMap(section));
            else if (value instanceof List<?> nestedList)
                converted.add(convertList(nestedList));
            else
                converted.add(value);
        }

        return converted;
    }
}
