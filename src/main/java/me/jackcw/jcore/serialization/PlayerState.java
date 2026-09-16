package me.jackcw.jcore.serialization;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class PlayerState
{
    private final UUID uniqueId;

    private final ItemStack[] storageContents;
    private final ItemStack[] armorContents;
    private final ItemStack offhand;

    private final int selectedSlot;

    private final Location location;
    private final Vector velocity;
    private final float fallDistance;

    private final double health;
    private final double absorption;

    private final int foodLevel;
    private final float saturation;
    private final float exhaustion;

    private final int totalExperience;

    private final GameMode gameMode;
    private final boolean allowFlight;
    private final boolean flying;
    private final float flySpeed;
    private final float walkSpeed;

    private final boolean healthScaled;
    private final double healthScale;

    private final int fireTicks;
    private final int freezeTicks;
    private final int remainingAir;

    private final boolean invulnerable;
    private final boolean gravity;
    private final boolean glowing;
    private final boolean silent;

    private final boolean canPickupItems;
    private final boolean collidable;

    private final int noDamageTicks;
    private final int maximumNoDamageTicks;

    private final Collection<PotionEffect> potionEffects;
    private final Map<Attribute, AttributeState> attributes;

    private PlayerState(Builder builder)
    {
        this.uniqueId = builder.uniqueId;
        this.storageContents = cloneItems(builder.storageContents);
        this.armorContents = cloneItems(builder.armorContents);
        this.offhand = builder.offhand == null ? null : builder.offhand.clone();

        this.selectedSlot = builder.selectedSlot;

        this.location = builder.location.clone();
        this.velocity = builder.velocity.clone();
        this.fallDistance = builder.fallDistance;

        this.health = builder.health;
        this.absorption = builder.absorption;

        this.foodLevel = builder.foodLevel;
        this.saturation = builder.saturation;
        this.exhaustion = builder.exhaustion;

        this.totalExperience = builder.totalExperience;

        this.gameMode = builder.gameMode;
        this.allowFlight = builder.allowFlight;
        this.flying = builder.flying;
        this.flySpeed = builder.flySpeed;
        this.walkSpeed = builder.walkSpeed;

        this.healthScaled = builder.healthScaled;
        this.healthScale = builder.healthScale;

        this.fireTicks = builder.fireTicks;
        this.freezeTicks = builder.freezeTicks;
        this.remainingAir = builder.remainingAir;

        this.invulnerable = builder.invulnerable;
        this.gravity = builder.gravity;
        this.glowing = builder.glowing;
        this.silent = builder.silent;

        this.canPickupItems = builder.canPickupItems;
        this.collidable = builder.collidable;

        this.noDamageTicks = builder.noDamageTicks;
        this.maximumNoDamageTicks = builder.maximumNoDamageTicks;

        this.potionEffects = new ArrayList<>(builder.potionEffects);
        this.attributes = new HashMap<>(builder.attributes);
    }

    public static Builder builder()
    {
        return new Builder();
    }

    public static PlayerState capture(Player player)
    {
        if (player == null)
            throw new IllegalArgumentException("Player cannot be null");

        Map<Attribute, AttributeState> attributes = new HashMap<>();

        Registry.ATTRIBUTE.stream().forEach(attribute ->
        {
            AttributeInstance instance = player.getAttribute(attribute);

            if (instance == null)
                return;

            attributes.put(attribute, AttributeState.capture(instance));
        });

        return builder()
                .uniqueId(player.getUniqueId())
                .storageContents(player.getInventory().getStorageContents())
                .armorContents(player.getInventory().getArmorContents())
                .offhand(player.getInventory().getItemInOffHand())
                .selectedSlot(player.getInventory().getHeldItemSlot())
                .location(player.getLocation())
                .velocity(player.getVelocity())
                .fallDistance(player.getFallDistance())
                .health(player.getHealth())
                .absorption(player.getAbsorptionAmount())
                .foodLevel(player.getFoodLevel())
                .saturation(player.getSaturation())
                .exhaustion(player.getExhaustion())
                .totalExperience(player.calculateTotalExperiencePoints())
                .gameMode(player.getGameMode())
                .allowFlight(player.getAllowFlight())
                .flying(player.isFlying())
                .flySpeed(player.getFlySpeed())
                .walkSpeed(player.getWalkSpeed())
                .healthScaled(player.isHealthScaled())
                .healthScale(player.getHealthScale())
                .fireTicks(player.getFireTicks())
                .freezeTicks(player.getFreezeTicks())
                .remainingAir(player.getRemainingAir())
                .invulnerable(player.isInvulnerable())
                .gravity(player.hasGravity())
                .glowing(player.isGlowing())
                .silent(player.isSilent())
                .canPickupItems(player.getCanPickupItems())
                .collidable(player.isCollidable())
                .noDamageTicks(player.getNoDamageTicks())
                .maximumNoDamageTicks(player.getMaximumNoDamageTicks())
                .potionEffects(player.getActivePotionEffects())
                .attributes(attributes)
                .build();
    }

    public void apply(Player player)
    {
        if (player == null)
            throw new IllegalArgumentException("Player cannot be null");

        if (!player.getUniqueId().equals(uniqueId))
            throw new IllegalArgumentException("Player UUID does not match captured state");

        player.getInventory().setStorageContents(cloneItems(storageContents));
        player.getInventory().setArmorContents(cloneItems(armorContents));
        player.getInventory().setItemInOffHand(offhand == null ? null : offhand.clone());
        player.getInventory().setHeldItemSlot(selectedSlot);

        player.teleport(location);
        player.setVelocity(velocity.clone());
        player.setFallDistance(fallDistance);

        restoreAttributes(player);

        player.setHealth(Math.min(health, player.getMaxHealth()));
        player.setAbsorptionAmount(absorption);

        player.setFoodLevel(foodLevel);
        player.setSaturation(saturation);
        player.setExhaustion(exhaustion);

        player.setExperienceLevelAndProgress(totalExperience);

        player.setGameMode(gameMode);

        player.setAllowFlight(allowFlight);
        player.setFlying(allowFlight && flying);

        player.setFlySpeed(flySpeed);
        player.setWalkSpeed(walkSpeed);

        player.setHealthScaled(healthScaled);

        if (healthScaled)
            player.setHealthScale(healthScale);

        player.setFireTicks(fireTicks);
        player.setFreezeTicks(freezeTicks);
        player.setRemainingAir(remainingAir);

        player.setInvulnerable(invulnerable);
        player.setGravity(gravity);
        player.setGlowing(glowing);
        player.setSilent(silent);

        player.setCanPickupItems(canPickupItems);
        player.setCollidable(collidable);

        player.setMaximumNoDamageTicks(maximumNoDamageTicks);
        player.setNoDamageTicks(noDamageTicks);

        player.clearActivePotionEffects();

        for (PotionEffect effect : potionEffects)
            player.addPotionEffect(effect);
    }

    private void restoreAttributes(Player player)
    {
        for (Map.Entry<Attribute, AttributeState> entry : attributes.entrySet())
        {
            AttributeInstance instance = player.getAttribute(entry.getKey());

            if (instance == null)
                continue;

            entry.getValue().apply(instance);
        }
    }

    public UUID getUniqueId()
    {
        return uniqueId;
    }

    public ItemStack[] getStorageContents()
    {
        return cloneItems(storageContents);
    }

    public ItemStack[] getArmorContents()
    {
        return cloneItems(armorContents);
    }

    public ItemStack getOffhand()
    {
        return offhand == null ? null : offhand.clone();
    }

    public int getSelectedSlot()
    {
        return selectedSlot;
    }

    public Location getLocation()
    {
        return location.clone();
    }

    public Vector getVelocity()
    {
        return velocity.clone();
    }

    public float getFallDistance()
    {
        return fallDistance;
    }

    public double getHealth()
    {
        return health;
    }

    public double getAbsorption()
    {
        return absorption;
    }

    public int getFoodLevel()
    {
        return foodLevel;
    }

    public float getSaturation()
    {
        return saturation;
    }

    public float getExhaustion()
    {
        return exhaustion;
    }

    public int getTotalExperience()
    {
        return totalExperience;
    }

    public GameMode getGameMode()
    {
        return gameMode;
    }

    public boolean isAllowFlight()
    {
        return allowFlight;
    }

    public boolean isFlying()
    {
        return flying;
    }

    public float getFlySpeed()
    {
        return flySpeed;
    }

    public float getWalkSpeed()
    {
        return walkSpeed;
    }

    public boolean isHealthScaled()
    {
        return healthScaled;
    }

    public double getHealthScale()
    {
        return healthScale;
    }

    public int getFireTicks()
    {
        return fireTicks;
    }

    public int getFreezeTicks()
    {
        return freezeTicks;
    }

    public int getRemainingAir()
    {
        return remainingAir;
    }

    public boolean isInvulnerable()
    {
        return invulnerable;
    }

    public boolean hasGravity()
    {
        return gravity;
    }

    public boolean isGlowing()
    {
        return glowing;
    }

    public boolean isSilent()
    {
        return silent;
    }

    public boolean canPickupItems()
    {
        return canPickupItems;
    }

    public boolean isCollidable()
    {
        return collidable;
    }

    public int getNoDamageTicks()
    {
        return noDamageTicks;
    }

    public int getMaximumNoDamageTicks()
    {
        return maximumNoDamageTicks;
    }

    public Collection<PotionEffect> getPotionEffects()
    {
        return new ArrayList<>(potionEffects);
    }

    public Map<Attribute, AttributeState> getAttributes()
    {
        return new HashMap<>(attributes);
    }

    private static ItemStack[] cloneItems(ItemStack[] items)
    {
        if (items == null)
            return null;

        ItemStack[] clone = new ItemStack[items.length];

        for (int i = 0; i < items.length; i++)
        {
            if (items[i] != null)
                clone[i] = items[i].clone();
        }

        return clone;
    }

    public static final class Builder
    {
        private UUID uniqueId;

        private ItemStack[] storageContents;
        private ItemStack[] armorContents;
        private ItemStack offhand;

        private int selectedSlot;

        private Location location;
        private Vector velocity = new Vector(0, 0, 0);
        private float fallDistance;

        private double health;
        private double absorption;

        private int foodLevel;
        private float saturation;
        private float exhaustion;

        private int totalExperience;

        private GameMode gameMode = GameMode.SURVIVAL;
        private boolean allowFlight;
        private boolean flying;
        private float flySpeed = 0.1f;
        private float walkSpeed = 0.2f;

        private boolean healthScaled;
        private double healthScale = 1.0;

        private int fireTicks;
        private int freezeTicks;
        private int remainingAir;

        private boolean invulnerable;
        private boolean gravity = true;
        private boolean glowing;
        private boolean silent;

        private boolean canPickupItems = true;
        private boolean collidable = true;

        private int noDamageTicks;
        private int maximumNoDamageTicks;

        private Collection<PotionEffect> potionEffects = new ArrayList<>();
        private Map<Attribute, AttributeState> attributes = new HashMap<>();

        private Builder()
        {
        }

        public Builder uniqueId(UUID uniqueId)
        {
            this.uniqueId = uniqueId;
            return this;
        }

        public Builder storageContents(ItemStack[] storageContents)
        {
            this.storageContents = storageContents;
            return this;
        }

        public Builder armorContents(ItemStack[] armorContents)
        {
            this.armorContents = armorContents;
            return this;
        }

        public Builder offhand(ItemStack offhand)
        {
            this.offhand = offhand;
            return this;
        }

        public Builder selectedSlot(int selectedSlot)
        {
            this.selectedSlot = selectedSlot;
            return this;
        }

        public Builder location(Location location)
        {
            this.location = location;
            return this;
        }

        public Builder velocity(Vector velocity)
        {
            this.velocity = velocity;
            return this;
        }

        public Builder fallDistance(float fallDistance)
        {
            this.fallDistance = fallDistance;
            return this;
        }

        public Builder health(double health)
        {
            this.health = health;
            return this;
        }

        public Builder absorption(double absorption)
        {
            this.absorption = absorption;
            return this;
        }

        public Builder foodLevel(int foodLevel)
        {
            this.foodLevel = foodLevel;
            return this;
        }

        public Builder saturation(float saturation)
        {
            this.saturation = saturation;
            return this;
        }

        public Builder exhaustion(float exhaustion)
        {
            this.exhaustion = exhaustion;
            return this;
        }

        public Builder totalExperience(int totalExperience)
        {
            this.totalExperience = totalExperience;
            return this;
        }

        public Builder gameMode(GameMode gameMode)
        {
            this.gameMode = gameMode;
            return this;
        }

        public Builder allowFlight(boolean allowFlight)
        {
            this.allowFlight = allowFlight;
            return this;
        }

        public Builder flying(boolean flying)
        {
            this.flying = flying;
            return this;
        }

        public Builder flySpeed(float flySpeed)
        {
            this.flySpeed = flySpeed;
            return this;
        }

        public Builder walkSpeed(float walkSpeed)
        {
            this.walkSpeed = walkSpeed;
            return this;
        }

        public Builder healthScaled(boolean healthScaled)
        {
            this.healthScaled = healthScaled;
            return this;
        }

        public Builder healthScale(double healthScale)
        {
            this.healthScale = healthScale;
            return this;
        }

        public Builder fireTicks(int fireTicks)
        {
            this.fireTicks = fireTicks;
            return this;
        }

        public Builder freezeTicks(int freezeTicks)
        {
            this.freezeTicks = freezeTicks;
            return this;
        }

        public Builder remainingAir(int remainingAir)
        {
            this.remainingAir = remainingAir;
            return this;
        }

        public Builder invulnerable(boolean invulnerable)
        {
            this.invulnerable = invulnerable;
            return this;
        }

        public Builder gravity(boolean gravity)
        {
            this.gravity = gravity;
            return this;
        }

        public Builder glowing(boolean glowing)
        {
            this.glowing = glowing;
            return this;
        }

        public Builder silent(boolean silent)
        {
            this.silent = silent;
            return this;
        }

        public Builder canPickupItems(boolean canPickupItems)
        {
            this.canPickupItems = canPickupItems;
            return this;
        }

        public Builder collidable(boolean collidable)
        {
            this.collidable = collidable;
            return this;
        }

        public Builder noDamageTicks(int noDamageTicks)
        {
            this.noDamageTicks = noDamageTicks;
            return this;
        }

        public Builder maximumNoDamageTicks(int maximumNoDamageTicks)
        {
            this.maximumNoDamageTicks = maximumNoDamageTicks;
            return this;
        }

        public Builder potionEffects(Collection<PotionEffect> potionEffects)
        {
            this.potionEffects = potionEffects;
            return this;
        }

        public Builder attributes(Map<Attribute, AttributeState> attributes)
        {
            this.attributes = attributes;
            return this;
        }

        public PlayerState build()
        {
            if (uniqueId == null)
                throw new IllegalArgumentException("Unique id cannot be null");

            if (location == null)
                throw new IllegalArgumentException("Location cannot be null");

            return new PlayerState(this);
        }
    }

    public static final class AttributeState
    {
        private final double baseValue;
        private final Collection<AttributeModifier> modifiers;

        private AttributeState(double baseValue, Collection<AttributeModifier> modifiers)
        {
            this.baseValue = baseValue;
            this.modifiers = new ArrayList<>(modifiers);
        }

        private static AttributeState capture(AttributeInstance instance)
        {
            return new AttributeState(instance.getBaseValue(), instance.getModifiers());
        }

        private void apply(AttributeInstance instance)
        {
            instance.setBaseValue(baseValue);

            for (AttributeModifier modifier : instance.getModifiers())
                instance.removeModifier(modifier);

            for (AttributeModifier modifier : modifiers)
                instance.addModifier(modifier);
        }

        public double getBaseValue()
        {
            return baseValue;
        }

        public Collection<AttributeModifier> getModifiers()
        {
            return new ArrayList<>(modifiers);
        }

        static AttributeState create(double baseValue, Collection<AttributeModifier> modifiers)
        {
            return new AttributeState(baseValue, modifiers);
        }
    }
}
