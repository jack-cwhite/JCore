package me.jackcw.jcore.serialization;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ItemStackSerializerTest
{
    private ItemStackSerializer serializer;

    @BeforeEach
    void setup()
    {
        MockBukkit.mock();
        serializer = new ItemStackSerializer();
    }

    @AfterEach
    void cleanup()
    {
        MockBukkit.unmock();
    }

    @Test
    void serializeProducesMap()
    {
        ItemStack item = new ItemStack(Material.DIAMOND_SWORD, 1);

        Object serialized = serializer.serialize(item);

        assertEquals(item.serialize(), serialized);
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
    void roundTripPreservesTypeAndAmount()
    {
        ItemStack item = new ItemStack(Material.GOLDEN_APPLE, 12);

        Object serialized = serializer.serialize(item);
        ItemStack restored = serializer.deserialize(serialized);

        assertEquals(Material.GOLDEN_APPLE, restored.getType());
        assertEquals(12, restored.getAmount());
    }

    @Test
    void deserializeAcceptsRawStringKeyedMap()
    {
        ItemStack item = new ItemStack(Material.IRON_PICKAXE, 1);

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) serializer.serialize(item);

        ItemStack restored = serializer.deserialize(data);

        assertEquals(Material.IRON_PICKAXE, restored.getType());
    }
}
