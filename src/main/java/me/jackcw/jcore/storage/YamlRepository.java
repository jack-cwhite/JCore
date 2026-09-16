package me.jackcw.jcore.storage;

import me.jackcw.jcore.serialization.SerializerManager;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.logging.Logger;

public final class YamlRepository<T>
{
    private static final Logger LOGGER = Logger.getLogger(YamlRepository.class.getName());

    private final YamlFile file;
    private final SerializerManager serializerManager;
    private final String rootPath;
    private final Class<T> type;
    private final Function<T, Integer> idExtractor;

    public YamlRepository(YamlFile file, SerializerManager serializerManager, String rootPath, Class<T> type, Function<T, Integer> idExtractor)
    {
        if (file == null)
            throw new IllegalArgumentException("File cannot be null");

        if (serializerManager == null)
            throw new IllegalArgumentException("Serializer manager cannot be null");

        if (rootPath == null || rootPath.isBlank())
            throw new IllegalArgumentException("Root path cannot be null or blank");

        if (type == null)
            throw new IllegalArgumentException("Type cannot be null");

        if (idExtractor == null)
            throw new IllegalArgumentException("Id extractor cannot be null");

        this.file = file;
        this.serializerManager = serializerManager;
        this.rootPath = rootPath;
        this.type = type;
        this.idExtractor = idExtractor;
    }

    public void save(T value)
    {
        if (value == null)
            throw new IllegalArgumentException("Value cannot be null");

        int id = idExtractor.apply(value);

        file.set(path(String.valueOf(id)), serializerManager.serialize(value));
        file.save();
    }

    public void delete(int id)
    {
        file.set(path(String.valueOf(id)), null);
        file.save();
    }

    /**
     * Reserves an ID that will not be handed out again, even if the object
     * using it is later deleted or the plugin restarts. This is useful when
     * IDs are stored in logs or relational data outside this repository.
     * Synchronized because the read-then-write of the next-id counter below
     * is not otherwise atomic - two overlapping calls on this instance could
     * hand out the same id.
     */
    public synchronized int reserveId()
    {
        int highestExisting = 0;

        ConfigurationSection root = file.getConfig().getConfigurationSection(rootPath);

        if (root != null)
            for (String key : root.getKeys(false))
                try
                {
                    highestExisting = Math.max(highestExisting, Integer.parseInt(key));
                }
                catch (NumberFormatException ignored)
                {
                }

        String nextIdPath = rootPath + "-meta.next-id";
        int nextId = Math.max(highestExisting + 1, file.getInt(nextIdPath, 1));

        file.set(nextIdPath, nextId + 1);
        file.save();

        return nextId;
    }

    public Optional<T> find(int id)
    {
        if (!file.contains(path(String.valueOf(id))))
            return Optional.empty();

        return Optional.ofNullable(file.get(path(String.valueOf(id)), type, id));
    }

    public List<T> findAll()
    {
        List<T> results = new ArrayList<>();

        ConfigurationSection root = file.getConfig().getConfigurationSection(rootPath);

        if (root == null)
            return results;

        for (String key : root.getKeys(false))
        {
            int id;

            try
            {
                id = Integer.parseInt(key);
            }
            catch (NumberFormatException e)
            {
                LOGGER.warning(
                        "Skipping invalid id '" + key + "' in repository at path '" + rootPath + "'"
                );

                continue;
            }

            try
            {
                find(id).ifPresent(results::add);
            }
            catch (RuntimeException e)
            {
                LOGGER.warning(
                        "Skipping unreadable entry '" + id + "' in repository at path '"
                                + rootPath + "': " + e.getMessage()
                );
            }
        }

        return results;
    }

    private String path(String id)
    {
        return rootPath + "." + id;
    }
}
