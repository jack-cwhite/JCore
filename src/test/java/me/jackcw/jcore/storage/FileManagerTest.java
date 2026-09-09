package me.jackcw.jcore.storage;

import me.jackcw.jcore.TestPlugin;
import me.jackcw.jcore.TestUtils;
import me.jackcw.jcore.serialization.SerializerManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FileManagerTest
{
    private TestPlugin plugin;
    private FileManager fileManager;

    @BeforeEach
    void setup()
    {
        plugin = TestUtils.mockPlugin();
        fileManager = new FileManager(plugin, TestUtils.createSerializerManager());
    }

    @AfterEach
    void cleanup()
    {
        TestUtils.cleanup();
    }

    @Test
    void createsYamlFileWhenMissing()
    {
        YamlFile file = fileManager.yaml("config.yml");

        assertNotNull(file);
        assertTrue(file.getFile().exists());
    }

    @Test
    void returnsSameYamlInstance()
    {
        YamlFile first = fileManager.yaml("config.yml");
        YamlFile second = fileManager.yaml("config.yml");

        assertSame(first, second);
    }

    @Test
    void managesMultipleFiles()
    {
        YamlFile config = fileManager.yaml("config.yml");
        YamlFile messages = fileManager.yaml("messages.yml");

        assertNotSame(config, messages);
        assertNotEquals(config.getFile(), messages.getFile());
    }

    @Test
    void savesAllFiles()
    {
        YamlFile file = fileManager.yaml("config.yml");
        file.set("test", "value");

        fileManager.saveAll();

        YamlFile loaded = new YamlFile(plugin, TestUtils.createSerializerManager(), "config.yml");

        assertEquals("value", loaded.getString("test"));
    }

    @Test
    void reloadsAllFiles()
    {
        YamlFile file = fileManager.yaml("config.yml");

        file.set("test", "value");
        file.save();

        file.set("test", "changed");

        fileManager.reloadAll();

        assertEquals("value", file.getString("test"));
    }

    @Test
    void closeClearsManagedFiles()
    {
        fileManager.yaml("config.yml");
        fileManager.close();

        YamlFile file = fileManager.yaml("config.yml");

        assertNotNull(file);
    }
}