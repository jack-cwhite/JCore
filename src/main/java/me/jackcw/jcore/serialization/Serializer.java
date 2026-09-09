package me.jackcw.jcore.serialization;

public interface Serializer<T>
{
    Object serialize(T value);

    T deserialize(Object value);
}
