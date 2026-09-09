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

    @Test
    void serializeUsesRegisteredSerializer()
    {
        SerializerManager manager = new SerializerManager();
        ItemStackTestData value = new ItemStackTestData();

        manager.register(ItemStackTestData.class, new ItemStackTestSerializer());

        Object result = manager.serialize(value);

        assertSame(value, result);
    }

    @Test
    void deserializeUsesRegisteredSerializer()
    {
        SerializerManager manager = new SerializerManager();
        ItemStackTestData value = new ItemStackTestData();

        manager.register(ItemStackTestData.class, new ItemStackTestSerializer());

        ItemStackTestData result = manager.deserialize(value, ItemStackTestData.class);

        assertSame(value, result);
    }

    @Test
    void serializeThrowsForUnregisteredType()
    {
        SerializerManager manager = new SerializerManager();
        ItemStackTestData value = new ItemStackTestData();

        assertThrows(IllegalStateException.class, () -> manager.serialize(value));
    }

    @Test
    void deserializeThrowsForUnregisteredType()
    {
        SerializerManager manager = new SerializerManager();
        ItemStackTestData value = new ItemStackTestData();

        assertThrows(IllegalStateException.class, () -> manager.deserialize(value, ItemStackTestData.class));
    }

    @Test
    void serializeNullReturnsNull()
    {
        SerializerManager manager = new SerializerManager();

        assertNull(manager.serialize(null));
    }

    @Test
    void deserializeNullReturnsNull()
    {
        SerializerManager manager = new SerializerManager();

        assertNull(manager.deserialize(null, ItemStackTestData.class));
    }

    private static class ItemStackTestData
    {
    }

    private static class ItemStackTestSerializer implements Serializer<ItemStackTestData>
    {
        @Override
        public Object serialize(ItemStackTestData value)
        {
            return value;
        }

        @Override
        public ItemStackTestData deserialize(Object value)
        {
            return (ItemStackTestData) value;
        }
    }
}