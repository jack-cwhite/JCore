package me.jackcw.jcore.serialization;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.Map;

public final class LocationSerializer implements Serializer<Location>
{
    @Override
    public Object serialize(Location value)
    {
        if (value == null)
            return null;

        if (value.getWorld() == null)
            throw new IllegalArgumentException(
                    "Cannot serialize a Location with no world"
            );

        return Map.of(
                "world", value.getWorld().getName(),
                "x", value.getX(),
                "y", value.getY(),
                "z", value.getZ(),
                "yaw", value.getYaw(),
                "pitch", value.getPitch()
        );
    }

    @Override
    public Location deserialize(Object value)
    {
        if (!(value instanceof Map<?, ?> map))
            throw new IllegalArgumentException(
                    "Expected a map when deserializing Location"
            );

        Object worldValue = map.get("world");

        if (worldValue == null)
            throw new IllegalArgumentException(
                    "Location is missing world"
            );

        World world = Bukkit.getWorld(String.valueOf(worldValue));

        if (world == null)
            throw new IllegalArgumentException(
                    "World '" + worldValue + "' could not be found"
            );

        double x = ((Number) map.get("x")).doubleValue();
        double y = ((Number) map.get("y")).doubleValue();
        double z = ((Number) map.get("z")).doubleValue();

        float yaw = ((Number) map.get("yaw")).floatValue();
        float pitch = ((Number) map.get("pitch")).floatValue();

        return new Location(
                world,
                x,
                y,
                z,
                yaw,
                pitch
        );
    }
}