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
