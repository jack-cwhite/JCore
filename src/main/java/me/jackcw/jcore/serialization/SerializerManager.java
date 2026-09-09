package me.jackcw.jcore.serialization;

import java.io.Serial;
import java.util.HashMap;
import java.util.Map;

public final class SerializerManager
{
    private final Map<Class<?>, Serializer<?>> serializers = new HashMap<>();

    public <T> void register(Class<T> type, Serializer<T> serializer)
    {
        if (type == null)
            throw new IllegalArgumentException("Type cannot be null");

        if (serializer == null)
            throw new IllegalArgumentException("Serializer cannot be null");

        serializers.put(type, serializer);
    }

    public Object serialize(Object value)
    {
        if (value == null)
            return null;

        Serializer<Object> serializer = getSerializer(value.getClass());

        return serializer.serialize(value);
    }

    public <T> T deserialize(Object value, Class<T> type)
    {
        if (type == null)
            throw new IllegalArgumentException(
                    "Type cannote be null"
            );

        if (value == null)
            return null;

        Serializer<T> serializer = get(type);

        if (serializer == null)
            throw new IllegalStateException(
                    "No serializer registered for " + type.getName()
            );

        return serializer.deserialize(value);
    }

    @SuppressWarnings("unchecked")
    public Serializer<Object> getSerializer(Class<?> type)
    {
        Serializer<?> serializer = serializers.get(type);

        if (serializer == null)
            throw new IllegalStateException(
                    "No serializer registered for " + type.getName()
            );

        return (Serializer<Object>) serializer;
    }

    @SuppressWarnings("unchecked")
    public <T> Serializer<T> get(Class<T> type)
    {
        return (Serializer<T>) serializers.get(type);
    }

    public boolean has(Class<?> type)
    {
        return serializers.containsKey(type);
    }
}
