package me.jackcw.jcore.serialization;

import org.bukkit.Location;
import org.bukkit.World;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class LocationSerializerTest
{
    private LocationSerializer serializer;
    private World world;

    @BeforeEach
    void setup()
    {
        MockBukkit.mock();
        world = MockBukkit.getMock().addSimpleWorld("test-world");
        serializer = new LocationSerializer();
    }

    @AfterEach
    void cleanup()
    {
        MockBukkit.unmock();
    }

    @Test
    void serializeNullReturnsNull()
    {
        assertNull(serializer.serialize(null));
    }

    @Test
    void serializeRejectsLocationWithNoWorld()
    {
        Location location = new Location(null, 1, 2, 3);

        assertThrows(
                IllegalArgumentException.class,
                () -> serializer.serialize(location)
        );
    }

    @Test
    void serializeProducesExpectedMap()
    {
        Location location = new Location(world, 1.5, 64.0, -3.25, 90.0f, 45.0f);

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) serializer.serialize(location);

        assertEquals("test-world", data.get("world"));
        assertEquals(1.5, data.get("x"));
        assertEquals(64.0, data.get("y"));
        assertEquals(-3.25, data.get("z"));
        assertEquals(90.0f, data.get("yaw"));
        assertEquals(45.0f, data.get("pitch"));
    }

    @Test
    void deserializeRejectsNonMap()
    {
        assertThrows(
                IllegalArgumentException.class,
                () -> serializer.deserialize("not-a-map")
        );
    }

    @Test
    void deserializeRejectsMissingWorld()
    {
        Map<String, Object> data = Map.of(
                "x", 1.0,
                "y", 2.0,
                "z", 3.0,
                "yaw", 0.0f,
                "pitch", 0.0f
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> serializer.deserialize(data)
        );
    }

    @Test
    void deserializeRejectsUnknownWorld()
    {
        Map<String, Object> data = Map.of(
                "world", "does-not-exist",
                "x", 1.0,
                "y", 2.0,
                "z", 3.0,
                "yaw", 0.0f,
                "pitch", 0.0f
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> serializer.deserialize(data)
        );
    }

    @Test
    void roundTripPreservesLocation()
    {
        Location location = new Location(world, 10.0, 65.0, -20.0, 180.0f, -30.0f);

        Object serialized = serializer.serialize(location);
        Location restored = serializer.deserialize(serialized);

        assertEquals(world, restored.getWorld());
        assertEquals(location.getX(), restored.getX());
        assertEquals(location.getY(), restored.getY());
        assertEquals(location.getZ(), restored.getZ());
        assertEquals(location.getYaw(), restored.getYaw());
        assertEquals(location.getPitch(), restored.getPitch());
    }
}
