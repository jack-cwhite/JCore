package me.jackcw.jcore.serialization;

public interface RepositorySerializer<T> extends Serializer<T>
{
    @Override
    default T deserialize(Object value)
    {
        throw new IllegalStateException(
                "RepositorySerializer requires an ID when deserializing"
        );
    }

    T deserialize(int id, Object value);
}
