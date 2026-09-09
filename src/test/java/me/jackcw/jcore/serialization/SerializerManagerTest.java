package me.jackcw.jcore.serialization;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SerializerManagerTest
{
    @Test
    void serializerCanBeRegistered()
    {
        SerializerManager manager = new SerializerManager();

        Serializer<ItemStackTestData> serializer = new ItemStackTestSerializer();

        manager.register(ItemStackTestData.class, serializer);

        assertSame(serializer, manager.get(ItemStackTestData.class));
    }

    @Test
    void hasReturnsTrueForRegisteredSerializer()
    {
        SerializerManager manager = new SerializerManager();

        manager.register(ItemStackTestData.class, new ItemStackTestSerializer());

        assertTrue(manager.has(ItemStackTestData.class));
    }

    @Test
    void hasReturnsFalseForUnregisteredSerializer()
    {
        SerializerManager manager = new SerializerManager();

        assertFalse(manager.has(ItemStackTestData.class));
    }

    @Test
    void nullTypeIsRejected()
    {
        SerializerManager manager = new SerializerManager();

        assertThrows(
                IllegalArgumentException.class,
                () -> manager.register(null, new ItemStackTestSerializer())
        );
    }

    @Test
    void nullSerializerIsRejected()
    {
        SerializerManager manager = new SerializerManager();

        assertThrows(
                IllegalArgumentException.class,
                () -> manager.register(ItemStackTestData.class, null)
        );
    }

    private static class ItemStackTestData
    {
    }

    private static class ItemStackTestSerializer implements Serializer<ItemStackTestData>
    {
        @Override
        public Object serialize(ItemStackTestData value)
        {
            return null;
        }

        @Override
        public ItemStackTestData deserialize(Object value)
        {
            return null;
        }
    }
}