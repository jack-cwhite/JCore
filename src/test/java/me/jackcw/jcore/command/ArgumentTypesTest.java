package me.jackcw.jcore.command;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ArgumentTypesTest
{
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
}