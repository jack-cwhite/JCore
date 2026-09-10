package me.jackcw.jcore.cooldown;

import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class CooldownManagerTest
{
    @Test
    void notOnCooldownByDefault()
    {
        CooldownManager manager = new CooldownManager();

        assertFalse(manager.isOnCooldown(UUID.randomUUID(), "ability"));
    }

    @Test
    void isOnCooldownAfterSet()
    {
        CooldownManager manager = new CooldownManager();
        UUID id = UUID.randomUUID();

        manager.set(id, "ability", 1, TimeUnit.MINUTES);

        assertTrue(manager.isOnCooldown(id, "ability"));
    }

    @Test
    void isNotOnCooldownAfterExpiry()
    {
        CooldownManager manager = new CooldownManager();
        UUID id = UUID.randomUUID();

        manager.set(id, "ability", 0, TimeUnit.MILLISECONDS);

        assertFalse(manager.isOnCooldown(id, "ability"));
    }

    @Test
    void remainingReflectsSetDuration()
    {
        CooldownManager manager = new CooldownManager();
        UUID id = UUID.randomUUID();

        manager.set(id, "ability", 30, TimeUnit.SECONDS);

        long remaining = manager.getRemaining(id, "ability", TimeUnit.SECONDS);

        assertTrue(remaining > 0 && remaining <= 30);
    }

    @Test
    void remainingIsZeroWhenNotSet()
    {
        CooldownManager manager = new CooldownManager();

        assertEquals(
                0,
                manager.getRemaining(UUID.randomUUID(), "ability", TimeUnit.SECONDS)
        );
    }

    @Test
    void clearRemovesSpecificKey()
    {
        CooldownManager manager = new CooldownManager();
        UUID id = UUID.randomUUID();

        manager.set(id, "ability", 1, TimeUnit.MINUTES);
        manager.set(id, "other", 1, TimeUnit.MINUTES);

        manager.clear(id, "ability");

        assertFalse(manager.isOnCooldown(id, "ability"));
        assertTrue(manager.isOnCooldown(id, "other"));
    }

    @Test
    void clearAllRemovesEveryKeyForId()
    {
        CooldownManager manager = new CooldownManager();
        UUID id = UUID.randomUUID();

        manager.set(id, "ability", 1, TimeUnit.MINUTES);
        manager.set(id, "other", 1, TimeUnit.MINUTES);

        manager.clearAll(id);

        assertFalse(manager.isOnCooldown(id, "ability"));
        assertFalse(manager.isOnCooldown(id, "other"));
    }

    @Test
    void cooldownsAreIsolatedPerId()
    {
        CooldownManager manager = new CooldownManager();
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();

        manager.set(first, "ability", 1, TimeUnit.MINUTES);

        assertTrue(manager.isOnCooldown(first, "ability"));
        assertFalse(manager.isOnCooldown(second, "ability"));
    }

    @Test
    void rejectsNullId()
    {
        CooldownManager manager = new CooldownManager();

        assertThrows(
                IllegalArgumentException.class,
                () -> manager.set(null, "ability", 1, TimeUnit.MINUTES)
        );
    }

    @Test
    void rejectsNegativeDuration()
    {
        CooldownManager manager = new CooldownManager();

        assertThrows(
                IllegalArgumentException.class,
                () -> manager.set(UUID.randomUUID(), "ability", -1, TimeUnit.MINUTES)
        );
    }
}
