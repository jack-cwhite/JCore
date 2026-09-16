package me.jackcw.jcore.serialization;

/*
    Many serializers can be created as its of T type
    Example using Arena
        Serlaizer<Arena>
        Object serialize(Arena arena)
        Arena deserialize(Object value)

    Object in this instance will be a Map<?, ?>
    Typically this map will have a String as the first, being the key
    and a value of whatever that key stored.

    This allows for nesting as a map could like this Map<String, Map<String, ?>>
    and would allow for nested yaml keys
 */


public interface Serializer<T>
{
    Object serialize(T value);

    T deserialize(Object value);
}
