package me.jackcw.jcore;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class JCoreTest
{
    @AfterEach
    void cleanup()
    {
        TestUtils.cleanup();
    }

    @Test
    void createWiresUpAllSystems()
    {
        TestPlugin plugin = TestUtils.mockPlugin();
        JCore jcore = JCore.create(plugin);

        assertNotNull(jcore.database());
        assertNotNull(jcore.migrations());
        assertNotNull(jcore.tasks());
        assertNotNull(jcore.files());
        assertNotNull(jcore.serializers());
        assertNotNull(jcore.messages());
        assertNotNull(jcore.commands());
        assertSame(plugin, jcore.plugin());
        assertFalse(jcore.isInitialized());
    }

    @Test
    void initializeDoesNotEagerlyConnectDatabase()
    {
        TestPlugin plugin = TestUtils.mockPlugin();
        JCore jcore = JCore.create(plugin);

        jcore.initialize();

        assertTrue(jcore.isInitialized());
        assertFalse(jcore.isDatabaseConnected());
    }

    @Test
    void databaseConnectsLazilyOnFirstAccess()
    {
        TestPlugin plugin = TestUtils.mockPlugin();
        JCore jcore = JCore.create(plugin);
        jcore.initialize();

        assertFalse(jcore.isDatabaseConnected());
        assertTrue(jcore.database().isConnected());
        assertTrue(jcore.isDatabaseConnected());

        jcore.shutdown();

        assertFalse(jcore.isInitialized());
        assertFalse(jcore.isDatabaseConnected());
    }

    @Test
    void rejectsNullPlugin()
    {
        assertThrows(
                IllegalArgumentException.class,
                () -> JCore.create(null)
        );
    }
}
