package me.jackcw.jcore.storage;

import me.jackcw.jcore.TestPlugin;
import me.jackcw.jcore.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class YamlFileErrorTest
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
    void invalidDefaultsThrowException()
    {
        YamlFile file = TestUtils.createYaml(plugin);
        assertThrows(RuntimeException.class, () -> file.mergeDefaults("invalid: ["));
    }

    @Test
    void nullDefaultsThrowException()
    {
        YamlFile file = TestUtils.createYaml(plugin);
        assertThrows(IllegalArgumentException.class, () -> file.mergeDefaults(null));
    }
}