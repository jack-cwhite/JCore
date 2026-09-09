package me.jackcw.jcore.serialization;

import org.bukkit.inventory.ItemStack;

import java.util.Map;

public final class ItemStackSerializer implements Serializer<ItemStack>
{
    @Override
    public Object serialize(ItemStack value)
    {
        return value.serialize();
    }

    @Override
    public ItemStack deserialize(Object value)
    {
        if (!(value instanceof Map<?, ?> map))
            throw new IllegalArgumentException(
                    "Expected a map when deserializing ItemStack"
            );

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) map;

        return ItemStack.deserialize(data);
    }
}
