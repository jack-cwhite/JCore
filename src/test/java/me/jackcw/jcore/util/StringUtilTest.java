package me.jackcw.jcore.util;

import org.bukkit.ChatColor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StringUtilTest
{
    @Test
    void colorReturnsNullForNull()
    {
        assertNull(StringUtil.color(null));
    }

    @Test
    void colorTranslatesAmpersandCodes()
    {
        assertEquals(
                ChatColor.RED + "Hello",
                StringUtil.color("&cHello")
        );
    }

    @Test
    void replaceReturnsNullForNullMessage()
    {
        assertNull(StringUtil.replace(null, "key", "value"));
    }

    @Test
    void replaceColorsMessageWhenReplacersNull()
    {
        assertEquals(
                ChatColor.RED + "Hello",
                StringUtil.replace("&cHello", (Object[]) null)
        );
    }

    @Test
    void replaceSubstitutesSinglePlaceholder()
    {
        assertEquals(
                "Hello Jack",
                StringUtil.replace("Hello {name}", "name", "Jack")
        );
    }

    @Test
    void replaceSubstitutesMultiplePlaceholders()
    {
        assertEquals(
                "Jack has 5 lives",
                StringUtil.replace(
                        "{name} has {lives} lives",
                        "name", "Jack",
                        "lives", 5
                )
        );
    }

    @Test
    void replaceIgnoresTrailingUnpairedKey()
    {
        assertEquals(
                "Hello {name}",
                StringUtil.replace("Hello {name}", "name")
        );
    }

    @Test
    void replaceLeavesUnknownPlaceholdersUntouched()
    {
        assertEquals(
                "Hello {other}",
                StringUtil.replace("Hello {other}", "name", "Jack")
        );
    }

    @Test
    void replaceAlsoAppliesColor()
    {
        assertEquals(
                ChatColor.RED + "Hello Jack",
                StringUtil.replace("&cHello {name}", "name", "Jack")
        );
    }
}
