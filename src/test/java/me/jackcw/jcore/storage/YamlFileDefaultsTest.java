package me.jackcw.jcore.storage;

import me.jackcw.jcore.TestPlugin;
import me.jackcw.jcore.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class YamlFileDefaultsTest
{
    private TestPlugin plugin;

    @BeforeEach
    void setup()
    {
        plugin = TestUtils.mockPlugin();
    }

    @AfterEach
    void cleanup()
    {
        TestUtils.cleanup();
    }

    @Test
    void defaultsMergeThroughYamlFile()
    {
        YamlFile file = TestUtils.createYaml(plugin);

        file.set("database.host", "custom-host");
        file.save();
        file.mergeDefaults("database:\n" + "  host: localhost\n" + "  port: 3306\n");

        assertEquals("custom-host", file.getString("database.host"));
        assertEquals(3306, file.getInt("database.port"));
    }
}