package me.jackcw.jcore.cooldown;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public final class CooldownManager
{
    private final Map<UUID, Map<String, Long>> cooldowns = new ConcurrentHashMap<>();

    public void set(UUID id, String key, long duration, TimeUnit unit)
    {
        if (id == null)
            throw new IllegalArgumentException("Id cannot be null");

        if (key == null)
            throw new IllegalArgumentException("Key cannot be null");

        if (duration < 0)
            throw new IllegalArgumentException("Duration cannot be negative");

        long expiresAt = System.currentTimeMillis() + unit.toMillis(duration);

        cooldowns.computeIfAbsent(id, ignored -> new ConcurrentHashMap<>())
                .put(key, expiresAt);
    }

    public boolean isOnCooldown(UUID id, String key)
    {
        Long expiresAt = getExpiry(id, key);

        return expiresAt != null && expiresAt > System.currentTimeMillis();
    }

    public long getRemaining(UUID id, String key, TimeUnit unit)
    {
        Long expiresAt = getExpiry(id, key);

        if (expiresAt == null)
            return 0;

        long remainingMillis = expiresAt - System.currentTimeMillis();

        if (remainingMillis <= 0)
            return 0;

        return unit.convert(remainingMillis, TimeUnit.MILLISECONDS);
    }

    public void clear(UUID id, String key)
    {
        Map<String, Long> keyed = cooldowns.get(id);

        if (keyed != null)
            keyed.remove(key);
    }

    public void clearAll(UUID id)
    {
        cooldowns.remove(id);
    }

    private Long getExpiry(UUID id, String key)
    {
        Map<String, Long> keyed = cooldowns.get(id);

        return keyed == null ? null : keyed.get(key);
    }
}
