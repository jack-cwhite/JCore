package me.jackcw.jcore.serialization;

import java.util.Map;

public final class TestDataSerializer implements Serializer<TestData>
{
    @Override
    public Object serialize(TestData value)
    {
        return Map.of(
                "value", value.value()
        );
    }

    @Override
    public TestData deserialize(Object value)
    {
        if (!(value instanceof Map<?, ?> map))
            throw new IllegalArgumentException(
                    "Expected a map"
            );

        return new TestData(
                String.valueOf(map.get("value"))
        );
    }
}