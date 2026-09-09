package me.jackcw.jcore.storage;

import me.jackcw.jcore.TestPlugin;
import me.jackcw.jcore.TestUtils;
import me.jackcw.jcore.serialization.Serializer;
import me.jackcw.jcore.serialization.SerializerManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class YamlFileTest
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
    void createsFileWhenMissing()
    {
        YamlFile file = TestUtils.createYaml(plugin);

        assertNotNull(file);
        assertTrue(file.getFile().exists());
    }

    @Test
    void writeAndReadValues()
    {
        YamlFile file = TestUtils.createYaml(plugin);

        file.set("test", true);
        file.set("name", "Jack");
        file.save();
        file.reload();

        assertTrue(file.getBoolean("test"));
        assertEquals("Jack", file.getString("name"));
    }

    @Test
    void missingValuesReturnDefaults()
    {
        YamlFile file = TestUtils.createYaml(plugin);

        assertNull(file.getString("missing"));
        assertEquals(5, file.getInt("missing", 5));
    }

    @Test
    void nestedPathsWork()
    {
        YamlFile file = TestUtils.createYaml(plugin);
        file.set("inventory.contents.0.id", "minecraft:diamond_sword");

        assertEquals("minecraft:diamond_sword", file.getString("inventory.contents.0.id"));
    }

    @Test
    void serializerRoundTripWorks()
    {
        SerializerManager serializerManager = TestUtils.createSerializerManager();
        serializerManager.register(TestData.class, new TestDataSerializer());

        YamlFile file = TestUtils.createYaml(plugin, serializerManager);
        TestData original = new TestData("Jack", 25);

        file.set("data", serializerManager.serialize(original));
        file.save();
        file.reload();

        TestData restored = file.get("data", TestData.class);

        assertEquals(original.name(), restored.name());
        assertEquals(original.age(), restored.age());
    }

    public record TestData(String name, int age)
    {
    }

    public static class TestDataSerializer implements Serializer<TestData>
    {
        @Override
        public Object serialize(TestData value)
        {
            return Map.of("name", value.name(), "age", value.age());
        }

        @Override
        public TestData deserialize(Object value)
        {
            if (!(value instanceof Map<?, ?> map))
            {
                throw new IllegalArgumentException(
                        "Expected a map"
                );
            }

            return new TestData(String.valueOf(map.get("name")), ((Number) map.get("age")).intValue());
        }
    }
}