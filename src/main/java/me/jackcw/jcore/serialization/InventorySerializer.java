package me.jackcw.jcore.serialization;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.LinkedHashMap;
import java.util.Map;

public final class InventorySerializer implements Serializer<Inventory>
{
    @Override
    public Object serialize(Inventory value)
    {
        if (value == null)
            return null;

        Map<String, Object> data = new LinkedHashMap<>();

        data.put("type", value.getType().name());
        data.put("size", value.getSize());

        Map<String, Object> contents = new LinkedHashMap<>();

        if (value instanceof PlayerInventory playerInventory)
        {
            ItemStack[] storage = playerInventory.getStorageContents();

            for (int slot = 0; slot < storage.length; slot++)
            {
                ItemStack item = storage[slot];

                if (item != null && item.getType() != Material.AIR)
                {
                    contents.put(
                            String.valueOf(slot),
                            item.serialize()
                    );
                }
            }

            data.put("contents", contents);

            Map<String, Object> armor = new LinkedHashMap<>();

            ItemStack[] armorContents = playerInventory.getArmorContents();

            if (armorContents.length > 0 && armorContents[0] != null
                    && armorContents[0].getType() != Material.AIR)
            {
                armor.put(
                        "boots",
                        armorContents[0].serialize()
                );
            }

            if (armorContents.length > 1 && armorContents[1] != null
                    && armorContents[1].getType() != Material.AIR)
            {
                armor.put(
                        "leggings",
                        armorContents[1].serialize()
                );
            }

            if (armorContents.length > 2 && armorContents[2] != null
                    && armorContents[2].getType() != Material.AIR)
            {
                armor.put(
                        "chestplate",
                        armorContents[2].serialize()
                );
            }

            if (armorContents.length > 3 && armorContents[3] != null
                    && armorContents[3].getType() != Material.AIR)
            {
                armor.put(
                        "helmet",
                        armorContents[3].serialize()
                );
            }

            data.put("armor", armor);

            ItemStack offhand = playerInventory.getItemInOffHand();

            if (offhand != null && offhand.getType() != Material.AIR)
            {
                data.put(
                        "offhand",
                        offhand.serialize()
                );
            }

            return data;
        }

        ItemStack[] inventoryContents = value.getContents();

        for (int slot = 0; slot < inventoryContents.length; slot++)
        {
            ItemStack item = inventoryContents[slot];

            if (item != null && item.getType() != Material.AIR)
            {
                contents.put(
                        String.valueOf(slot),
                        item.serialize()
                );
            }
        }

        data.put("contents", contents);

        return data;
    }

    @Override
    public Inventory deserialize(Object value)
    {
        if (!(value instanceof Map<?, ?> map))
            throw new IllegalArgumentException(
                    "Expected a map when deserializing Inventory"
            );

        InventoryType type = getInventoryType(map);

        int size = getSize(map);

        Inventory inventory;

        try
        {
            if (type == InventoryType.CHEST)
            {
                inventory = Bukkit.createInventory(
                        null,
                        size
                );
            }
            else
            {
                inventory = Bukkit.createInventory(
                        null,
                        type
                );
            }
        }
        catch (IllegalArgumentException e)
        {
            throw new IllegalArgumentException(
                    "Could not create inventory of type " + type
                            + " with size " + size,
                    e
            );
        }

        restoreContents(
                map.get("contents"),
                inventory
        );

        return inventory;
    }

    public void deserializeInto(
            Object value,
            PlayerInventory inventory
    )
    {
        if (value == null)
            throw new IllegalArgumentException(
                    "Inventory data cannot be null"
            );

        if (inventory == null)
            throw new IllegalArgumentException(
                    "PlayerInventory cannot be null"
            );

        if (!(value instanceof Map<?, ?> map))
            throw new IllegalArgumentException(
                    "Expected a map when deserializing PlayerInventory"
            );

        restoreContents(
                map.get("contents"),
                inventory
        );

        restoreArmor(
                map.get("armor"),
                inventory
        );

        restoreOffhand(
                map.get("offhand"),
                inventory
        );
    }

    private InventoryType getInventoryType(Map<?, ?> map)
    {
        Object typeValue = map.get("type");

        if (typeValue == null)
            throw new IllegalArgumentException(
                    "Inventory is missing type"
            );

        try
        {
            return InventoryType.valueOf(
                    String.valueOf(typeValue)
            );
        }
        catch (IllegalArgumentException e)
        {
            throw new IllegalArgumentException(
                    "Unknown inventory type: " + typeValue,
                    e
            );
        }
    }

    private int getSize(Map<?, ?> map)
    {
        Object sizeValue = map.get("size");

        if (!(sizeValue instanceof Number number))
            throw new IllegalArgumentException(
                    "Inventory is missing a valid size"
            );

        int size = number.intValue();

        if (size <= 0)
            throw new IllegalArgumentException(
                    "Inventory size must be greater than zero"
            );

        return size;
    }

    private void restoreContents(
            Object value,
            Inventory inventory
    )
    {
        if (!(value instanceof Map<?, ?> contents))
            return;

        for (Map.Entry<?, ?> entry : contents.entrySet())
        {
            int slot;

            try
            {
                slot = Integer.parseInt(
                        String.valueOf(entry.getKey())
                );
            }
            catch (NumberFormatException e)
            {
                continue;
            }

            if (slot < 0 || slot >= inventory.getSize())
                continue;

            ItemStack item = deserializeItem(
                    entry.getValue()
            );

            inventory.setItem(
                    slot,
                    item
            );
        }
    }

    private void restoreArmor(
            Object value,
            PlayerInventory inventory
    )
    {
        ItemStack[] armor = new ItemStack[4];

        if (value instanceof Map<?, ?> map)
        {
            armor[0] = getItem(map.get("boots"));
            armor[1] = getItem(map.get("leggings"));
            armor[2] = getItem(map.get("chestplate"));
            armor[3] = getItem(map.get("helmet"));
        }

        inventory.setArmorContents(armor);
    }

    private void restoreOffhand(
            Object value,
            PlayerInventory inventory
    )
    {
        ItemStack item = getItem(value);

        if (item == null)
            item = new ItemStack(Material.AIR);

        inventory.setItemInOffHand(item);
    }

    private ItemStack getItem(Object value)
    {
        if (!(value instanceof Map<?, ?> map))
            return null;

        return deserializeItem(map);
    }

    private ItemStack deserializeItem(Object value)
    {
        if (!(value instanceof Map<?, ?> map))
            throw new IllegalArgumentException(
                    "Expected an item map"
            );

        @SuppressWarnings("unchecked")
        Map<String, Object> itemData =
                (Map<String, Object>) map;

        return ItemStack.deserialize(itemData);
    }
}