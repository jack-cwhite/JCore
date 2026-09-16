package me.jackcw.jcore.serialization;

import org.bukkit.GameMode;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class PlayerStateSerializer implements Serializer<PlayerState>
{
    private final SerializerManager serializerManager;

    public PlayerStateSerializer(SerializerManager serializerManager)
    {
        if (serializerManager == null)
            throw new IllegalArgumentException("Serializer manager cannot be null");

        this.serializerManager = serializerManager;
    }

    @Override
    public Object serialize(PlayerState value)
    {
        if (value == null)
            return null;

        Map<String, Object> data = new LinkedHashMap<>();

        data.put("uuid", value.getUniqueId().toString());
        data.put("inventory", serializeInventory(value));
        data.put("location", serializerManager.serialize(value.getLocation()));
        data.put("velocity", serializeVector(value.getVelocity()));
        data.put("fall-distance", value.getFallDistance());
        data.put("health", value.getHealth());
        data.put("absorption", value.getAbsorption());
        data.put("food-level", value.getFoodLevel());
        data.put("saturation", value.getSaturation());
        data.put("exhaustion", value.getExhaustion());
        data.put("total-experience", value.getTotalExperience());
        data.put("game-mode", value.getGameMode().name());
        data.put("allow-flight", value.isAllowFlight());
        data.put("flying", value.isFlying());
        data.put("fly-speed", value.getFlySpeed());
        data.put("walk-speed", value.getWalkSpeed());
        data.put("health-scaled", value.isHealthScaled());
        data.put("health-scale", value.getHealthScale());
        data.put("fire-ticks", value.getFireTicks());
        data.put("freeze-ticks", value.getFreezeTicks());
        data.put("remaining-air", value.getRemainingAir());
        data.put("invulnerable", value.isInvulnerable());
        data.put("gravity", value.hasGravity());
        data.put("glowing", value.isGlowing());
        data.put("silent", value.isSilent());
        data.put("can-pickup-items", value.canPickupItems());
        data.put("collidable", value.isCollidable());
        data.put("no-damage-ticks", value.getNoDamageTicks());
        data.put("maximum-no-damage-ticks", value.getMaximumNoDamageTicks());
        data.put("potion-effects", serializePotionEffects(value.getPotionEffects()));
        data.put("attributes", serializeAttributes(value.getAttributes()));

        return data;
    }

    @Override
    public PlayerState deserialize(Object value)
    {
        if (!(value instanceof Map<?, ?> map))
            throw new IllegalArgumentException("Expected a map when deserializing PlayerState");

        UUID uuid = UUID.fromString(getString(map, "uuid"));

        Map<?, ?> inventoryData = getMap(map, "inventory");

        ItemStack[] storageContents = deserializeItems(inventoryData.get("contents"));
        ItemStack[] armorContents = deserializeArmor(inventoryData.get("armor"));
        ItemStack offhand = deserializeItem(inventoryData.get("offhand"));
        int selectedSlot = getInt(inventoryData, "selected-slot");

        Vector velocity = deserializeVector(map.get("velocity"));

        Collection<PotionEffect> potionEffects =
                deserializePotionEffects(map.get("potion-effects"));

        Map<Attribute, PlayerState.AttributeState> attributes =
                deserializeAttributes(map.get("attributes"));

        return PlayerState.builder()
                .uniqueId(uuid)
                .storageContents(storageContents)
                .armorContents(armorContents)
                .offhand(offhand)
                .selectedSlot(selectedSlot)
                .location(serializerManager.deserialize(map.get("location"), org.bukkit.Location.class))
                .velocity(velocity)
                .fallDistance(getFloat(map, "fall-distance"))
                .health(getDouble(map, "health"))
                .absorption(getDouble(map, "absorption"))
                .foodLevel(getInt(map, "food-level"))
                .saturation(getFloat(map, "saturation"))
                .exhaustion(getFloat(map, "exhaustion"))
                .totalExperience(getInt(map, "total-experience"))
                .gameMode(GameMode.valueOf(getString(map, "game-mode")))
                .allowFlight(getBoolean(map, "allow-flight"))
                .flying(getBoolean(map, "flying"))
                .flySpeed(getFloat(map, "fly-speed"))
                .walkSpeed(getFloat(map, "walk-speed"))
                .healthScaled(getBoolean(map, "health-scaled"))
                .healthScale(getDouble(map, "health-scale"))
                .fireTicks(getInt(map, "fire-ticks"))
                .freezeTicks(getInt(map, "freeze-ticks"))
                .remainingAir(getInt(map, "remaining-air"))
                .invulnerable(getBoolean(map, "invulnerable"))
                .gravity(getBoolean(map, "gravity"))
                .glowing(getBoolean(map, "glowing"))
                .silent(getBoolean(map, "silent"))
                .canPickupItems(getBoolean(map, "can-pickup-items"))
                .collidable(getBoolean(map, "collidable"))
                .noDamageTicks(getInt(map, "no-damage-ticks"))
                .maximumNoDamageTicks(getInt(map, "maximum-no-damage-ticks"))
                .potionEffects(potionEffects)
                .attributes(attributes)
                .build();
    }

    private Map<String, Object> serializeInventory(PlayerState state)
    {
        Map<String, Object> data = new LinkedHashMap<>();
        Map<String, Object> contents = new LinkedHashMap<>();

        ItemStack[] storage = state.getStorageContents();

        for (int slot = 0; slot < storage.length; slot++)
        {
            ItemStack item = storage[slot];

            if (item == null)
                continue;

            contents.put(String.valueOf(slot), serializerManager.serialize(item));
        }

        data.put("contents", contents);

        Map<String, Object> armor = new LinkedHashMap<>();
        ItemStack[] armorContents = state.getArmorContents();

        putItem(armor, "boots", armorContents, 0);
        putItem(armor, "leggings", armorContents, 1);
        putItem(armor, "chestplate", armorContents, 2);
        putItem(armor, "helmet", armorContents, 3);

        data.put("armor", armor);

        ItemStack offhand = state.getOffhand();

        if (offhand != null)
            data.put("offhand", serializerManager.serialize(offhand));

        data.put("selected-slot", state.getSelectedSlot());

        return data;
    }

    private void putItem(Map<String, Object> data, String key, ItemStack[] items, int index)
    {
        if (index >= items.length)
            return;

        ItemStack item = items[index];

        if (item == null)
            return;

        data.put(key, serializerManager.serialize(item));
    }

    private ItemStack[] deserializeItems(Object value)
    {
        ItemStack[] items = new ItemStack[36];

        if (!(value instanceof Map<?, ?> map))
            return items;

        for (Map.Entry<?, ?> entry : map.entrySet())
        {
            int slot;

            try
            {
                slot = Integer.parseInt(String.valueOf(entry.getKey()));
            }
            catch (NumberFormatException ignored)
            {
                continue;
            }

            if (slot < 0 || slot >= items.length)
                continue;

            items[slot] = deserializeItem(entry.getValue());
        }

        return items;
    }

    private ItemStack[] deserializeArmor(Object value)
    {
        ItemStack[] armor = new ItemStack[4];

        if (!(value instanceof Map<?, ?> map))
            return armor;

        armor[0] = deserializeItem(map.get("boots"));
        armor[1] = deserializeItem(map.get("leggings"));
        armor[2] = deserializeItem(map.get("chestplate"));
        armor[3] = deserializeItem(map.get("helmet"));

        return armor;
    }

    private ItemStack deserializeItem(Object value)
    {
        if (value == null)
            return null;

        return serializerManager.deserialize(value, ItemStack.class);
    }

    private Map<String, Object> serializeVector(Vector vector)
    {
        Map<String, Object> data = new LinkedHashMap<>();

        data.put("x", vector.getX());
        data.put("y", vector.getY());
        data.put("z", vector.getZ());

        return data;
    }

    private Vector deserializeVector(Object value)
    {
        Map<?, ?> map = requireMap(value, "velocity");

        return new Vector(
                getDouble(map, "x"),
                getDouble(map, "y"),
                getDouble(map, "z")
        );
    }

    private List<Object> serializePotionEffects(Collection<PotionEffect> effects)
    {
        List<Object> data = new ArrayList<>();

        for (PotionEffect effect : effects)
        {
            Map<String, Object> entry = new LinkedHashMap<>();

            entry.put("type", effect.getType().getKey().toString());
            entry.put("duration", effect.getDuration());
            entry.put("amplifier", effect.getAmplifier());
            entry.put("ambient", effect.isAmbient());
            entry.put("particles", effect.hasParticles());
            entry.put("icon", effect.hasIcon());

            data.add(entry);
        }

        return data;
    }

    private Collection<PotionEffect> deserializePotionEffects(Object value)
    {
        List<PotionEffect> effects = new ArrayList<>();

        if (!(value instanceof Collection<?> collection))
            return effects;

        for (Object entryValue : collection)
        {
            if (!(entryValue instanceof Map<?, ?> map))
                continue;

            Object typeValue = map.get("type");

            if (typeValue == null)
                continue;

            PotionEffectType type = Registry.MOB_EFFECT.get(
                    NamespacedKey.fromString(String.valueOf(typeValue))
            );

            if (type == null)
                continue;

            effects.add(new PotionEffect(
                    type,
                    getInt(map, "duration"),
                    getInt(map, "amplifier"),
                    getBoolean(map, "ambient"),
                    getBoolean(map, "particles"),
                    getBoolean(map, "icon")
            ));
        }

        return effects;
    }

    private Map<String, Object> serializeAttributes(
            Map<Attribute, PlayerState.AttributeState> attributes
    )
    {
        Map<String, Object> data = new LinkedHashMap<>();

        for (Map.Entry<Attribute, PlayerState.AttributeState> entry : attributes.entrySet())
        {
            PlayerState.AttributeState state = entry.getValue();
            Map<String, Object> attributeData = new LinkedHashMap<>();
            List<Object> modifiers = new ArrayList<>();

            attributeData.put("base-value", state.getBaseValue());

            for (AttributeModifier modifier : state.getModifiers())
                modifiers.add(modifier.serialize());

            attributeData.put("modifiers", modifiers);

            data.put(entry.getKey().getKey().toString(), attributeData);
        }

        return data;
    }

    private Map<Attribute, PlayerState.AttributeState> deserializeAttributes(Object value)
    {
        Map<Attribute, PlayerState.AttributeState> attributes = new HashMap<>();

        if (!(value instanceof Map<?, ?> map))
            return attributes;

        for (Map.Entry<?, ?> entry : map.entrySet())
        {
            NamespacedKey key = NamespacedKey.fromString(
                    String.valueOf(entry.getKey())
            );

            if (key == null)
                continue;

            Attribute attribute = Registry.ATTRIBUTE.get(key);

            if (attribute == null)
                continue;

            if (!(entry.getValue() instanceof Map<?, ?> attributeMap))
                continue;

            double baseValue = getDouble(attributeMap, "base-value");
            Collection<AttributeModifier> modifiers = deserializeModifiers(
                    attributeMap.get("modifiers")
            );

            attributes.put(
                    attribute,
                    PlayerState.AttributeState.create(baseValue, modifiers)
            );
        }

        return attributes;
    }

    private Collection<AttributeModifier> deserializeModifiers(Object value)
    {
        List<AttributeModifier> modifiers = new ArrayList<>();

        if (!(value instanceof Collection<?> collection))
            return modifiers;

        for (Object modifierValue : collection)
        {
            if (!(modifierValue instanceof Map<?, ?> map))
                continue;

            Map<String, Object> modifierMap = new HashMap<>();

            for (Map.Entry<?, ?> entry : map.entrySet())
                modifierMap.put(String.valueOf(entry.getKey()), entry.getValue());

            modifiers.add(AttributeModifier.deserialize(modifierMap));
        }

        return modifiers;
    }

    private String getString(Map<?, ?> map, String key)
    {
        Object value = map.get(key);

        if (value == null)
            throw new IllegalArgumentException("PlayerState is missing '" + key + "'");

        return String.valueOf(value);
    }

    private int getInt(Map<?, ?> map, String key)
    {
        Object value = map.get(key);

        if (!(value instanceof Number number))
            throw new IllegalArgumentException("PlayerState is missing a valid '" + key + "'");

        return number.intValue();
    }

    private float getFloat(Map<?, ?> map, String key)
    {
        Object value = map.get(key);

        if (!(value instanceof Number number))
            throw new IllegalArgumentException("PlayerState is missing a valid '" + key + "'");

        return number.floatValue();
    }

    private double getDouble(Map<?, ?> map, String key)
    {
        Object value = map.get(key);

        if (!(value instanceof Number number))
            throw new IllegalArgumentException("PlayerState is missing a valid '" + key + "'");

        return number.doubleValue();
    }

    private boolean getBoolean(Map<?, ?> map, String key)
    {
        Object value = map.get(key);

        if (!(value instanceof Boolean booleanValue))
            throw new IllegalArgumentException("PlayerState is missing a valid '" + key + "'");

        return booleanValue;
    }

    private Map<?, ?> getMap(Map<?, ?> map, String key)
    {
        return requireMap(map.get(key), key);
    }

    private Map<?, ?> requireMap(Object value, String name)
    {
        if (!(value instanceof Map<?, ?> map))
            throw new IllegalArgumentException("PlayerState is missing a valid '" + name + "'");

        return map;
    }
}