package me.jackcw.jcore.command;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ArgumentTypesTest
{
    @BeforeEach
    void setup()
    {
        MockBukkit.mock();
    }

    @AfterEach
    void cleanup()
    {
        MockBukkit.unmock();
    }

    @Test
    void parsesString()
    {
        assertEquals(
                "hello",
                ArgumentTypes.string().parse("hello")
        );
    }

    @Test
    void parsesInteger()
    {
        assertEquals(
                42,
                ArgumentTypes.integer().parse("42")
        );
    }

    @Test
    void rejectsInvalidInteger()
    {
        assertThrows(
                IllegalArgumentException.class,
                () -> ArgumentTypes.integer().parse("hello")
        );
    }

    @Test
    void parsesDecimal()
    {
        assertEquals(
                5.75,
                ArgumentTypes.decimal().parse("5.75")
        );
    }

    @Test
    void rejectsInvalidDecimal()
    {
        assertThrows(
                IllegalArgumentException.class,
                () -> ArgumentTypes.decimal().parse("hello")
        );
    }

    @Test
    void parsesBoolean()
    {
        assertTrue(ArgumentTypes.bool().parse("true"));
        assertTrue(ArgumentTypes.bool().parse("TRUE"));
        assertFalse(ArgumentTypes.bool().parse("false"));
        assertFalse(ArgumentTypes.bool().parse("FALSE"));
    }

    @Test
    void rejectsInvalidBoolean()
    {
        assertThrows(
                IllegalArgumentException.class,
                () -> ArgumentTypes.bool().parse("yes")
        );
    }

    @Test
    void parsesOnlinePlayer()
    {
        Player steve = MockBukkit.getMock().addPlayer("Steve");

        assertSame(
                steve,
                ArgumentTypes.player().parse("Steve")
        );
    }

    @Test
    void rejectsOfflinePlayer()
    {
        assertThrows(
                IllegalArgumentException.class,
                () -> ArgumentTypes.player().parse("NotOnline")
        );
    }

    @Test
    void suggestsMatchingOnlinePlayers()
    {
        MockBukkit.getMock().addPlayer("Steve");
        MockBukkit.getMock().addPlayer("Alex");

        assertEquals(
                java.util.List.of("Steve"),
                ArgumentTypes.player().suggest(MockBukkit.getMock().getConsoleSender(), "St")
        );
    }

    @Test
    void parsesExistingWorld()
    {
        World world = MockBukkit.getMock().addSimpleWorld("test-world");

        assertSame(
                world,
                ArgumentTypes.world().parse("test-world")
        );
    }

    @Test
    void rejectsUnknownWorld()
    {
        assertThrows(
                IllegalArgumentException.class,
                () -> ArgumentTypes.world().parse("does-not-exist")
        );
    }

    @Test
    void suggestsMatchingWorlds()
    {
        MockBukkit.getMock().addSimpleWorld("test-world");

        assertEquals(
                java.util.List.of("test-world"),
                ArgumentTypes.world().suggest(MockBukkit.getMock().getConsoleSender(), "test")
        );
    }

    @Test
    void parsesMaterial()
    {
        assertEquals(
                Material.DIAMOND_SWORD,
                ArgumentTypes.material().parse("DIAMOND_SWORD")
        );

        assertEquals(
                Material.DIAMOND_SWORD,
                ArgumentTypes.material().parse("diamond_sword")
        );
    }

    @Test
    void rejectsInvalidMaterial()
    {
        assertThrows(
                IllegalArgumentException.class,
                () -> ArgumentTypes.material().parse("not_a_real_material")
        );
    }

    @Test
    void suggestsMatchingMaterials()
    {
        assertTrue(
                ArgumentTypes.material()
                        .suggest(MockBukkit.getMock().getConsoleSender(), "DIAMOND_SW")
                        .contains("DIAMOND_SWORD")
        );
    }

    @Test
    void parsesEnum()
    {
        assertEquals(
                TestEnum.ALPHA,
                ArgumentTypes.enumType(TestEnum.class).parse("alpha")
        );
    }

    @Test
    void rejectsInvalidEnumValue()
    {
        assertThrows(
                IllegalArgumentException.class,
                () -> ArgumentTypes.enumType(TestEnum.class).parse("nope")
        );
    }

    @Test
    void suggestsMatchingEnumValues()
    {
        assertEquals(
                java.util.List.of("ALPHA"),
                ArgumentTypes.enumType(TestEnum.class)
                        .suggest(MockBukkit.getMock().getConsoleSender(), "AL")
        );
    }

    @Test
    void parsesUuid()
    {
        UUID uuid = UUID.randomUUID();

        assertEquals(
                uuid,
                ArgumentTypes.uuid().parse(uuid.toString())
        );
    }

    @Test
    void rejectsInvalidUuid()
    {
        assertThrows(
                IllegalArgumentException.class,
                () -> ArgumentTypes.uuid().parse("not-a-uuid")
        );
    }

    private enum TestEnum
    {
        ALPHA,
        BETA
    }
}
