package me.jackcw.jcore;

import me.jackcw.jcore.serialization.SerializerManager;
import me.jackcw.jcore.storage.YamlFile;
import org.mockbukkit.mockbukkit.MockBukkit;

public final class TestUtils
{
    private TestUtils()
    {
    }

    public static TestPlugin mockPlugin()
    {
        MockBukkit.mock();
        return MockBukkit.load(TestPlugin.class);
    }

    public static void cleanup()
    {
        MockBukkit.unmock();
    }

    public static SerializerManager createSerializerManager()
    {
        return new SerializerManager();
    }

    public static YamlFile createYaml(TestPlugin plugin)
    {
        return createYaml(plugin, createSerializerManager());
    }

    public static YamlFile createYaml(TestPlugin plugin, SerializerManager serializerManager)
    {
        return new YamlFile(plugin, serializerManager, "test-" + System.nanoTime() + ".yml");
    }

    public static YamlFile createYaml(TestPlugin plugin, String name)
    {
        return createYaml(plugin, createSerializerManager(), name);
    }

    public static YamlFile createYaml(TestPlugin plugin, SerializerManager serializerManager, String name)
    {
        return new YamlFile(plugin, serializerManager, name);
    }

    public static YamlFile createYamlWithDefaults(TestPlugin plugin, String defaults)
    {
        YamlFile file = createYaml(plugin);
        file.mergeDefaults(defaults);

        return file;
    }
}