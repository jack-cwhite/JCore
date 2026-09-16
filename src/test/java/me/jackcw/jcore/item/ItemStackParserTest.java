package me.jackcw.jcore.item;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ItemStackParserTest
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
    void rejectsNullSection()
    {
        assertThrows(
                IllegalArgumentException.class,
                () -> ItemStackParser.parse(null)
        );
    }

    @Test
    void rejectsMissingMaterial()
    {
        YamlConfiguration config = new YamlConfiguration();

        assertThrows(
                IllegalArgumentException.class,
                () -> ItemStackParser.parse(config)
        );
    }

    @Test
    void rejectsInvalidMaterial()
    {
        YamlConfiguration config = new YamlConfiguration();
        config.set("material", "NOT_A_MATERIAL");

        assertThrows(
                IllegalArgumentException.class,
                () -> ItemStackParser.parse(config)
        );
    }

    @Test
    void parsesMaterialAndDefaultsAmountToOne()
    {
        YamlConfiguration config = new YamlConfiguration();
        config.set("material", "DIAMOND_SWORD");

        ItemStack item = ItemStackParser.parse(config);

        assertEquals(Material.DIAMOND_SWORD, item.getType());
        assertEquals(1, item.getAmount());
    }

    @Test
    void parsesAmount()
    {
        YamlConfiguration config = new YamlConfiguration();
        config.set("material", "DIAMOND");
        config.set("amount", 5);

        assertEquals(5, ItemStackParser.parse(config).getAmount());
    }

    @Test
    void parsesNameWithColorCodes()
    {
        YamlConfiguration config = new YamlConfiguration();
        config.set("material", "DIAMOND_SWORD");
        config.set("name", "&cWarrior Kit");

        ItemMeta meta = ItemStackParser.parse(config).getItemMeta();

        assertEquals(
                "Warrior Kit",
                PlainTextComponentSerializer.plainText().serialize(meta.displayName())
        );
    }

    @Test
    void parsesLore()
    {
        YamlConfiguration config = new YamlConfiguration();
        config.set("material", "DIAMOND_SWORD");
        config.set("lore", List.of("&7Line one", "&7Line two"));

        ItemMeta meta = ItemStackParser.parse(config).getItemMeta();

        assertEquals(2, meta.lore().size());
        assertEquals(
                "Line one",
                PlainTextComponentSerializer.plainText().serialize(meta.lore().get(0))
        );
    }

    @Test
    void parsesCustomModelData()
    {
        YamlConfiguration config = new YamlConfiguration();
        config.set("material", "DIAMOND_SWORD");
        config.set("custom-model-data", 1001);

        ItemMeta meta = ItemStackParser.parse(config).getItemMeta();

        assertEquals(1001, meta.getCustomModelData());
    }

    @Test
    void parsesUnbreakable()
    {
        YamlConfiguration config = new YamlConfiguration();
        config.set("material", "DIAMOND_SWORD");
        config.set("unbreakable", true);

        assertTrue(ItemStackParser.parse(config).getItemMeta().isUnbreakable());
    }

    @Test
    void parsesFlags()
    {
        YamlConfiguration config = new YamlConfiguration();
        config.set("material", "DIAMOND_SWORD");
        config.set("flags", List.of("HIDE_ATTRIBUTES"));

        ItemMeta meta = ItemStackParser.parse(config).getItemMeta();

        assertTrue(meta.hasItemFlag(ItemFlag.HIDE_ATTRIBUTES));
    }

    @Test
    void rejectsInvalidFlag()
    {
        YamlConfiguration config = new YamlConfiguration();
        config.set("material", "DIAMOND_SWORD");
        config.set("flags", List.of("NOT_A_FLAG"));

        assertThrows(
                IllegalArgumentException.class,
                () -> ItemStackParser.parse(config)
        );
    }

    @Test
    void parsesEnchantments()
    {
        YamlConfiguration config = new YamlConfiguration();
        config.set("material", "DIAMOND_SWORD");
        config.set("enchantments.sharpness", 5);

        ItemMeta meta = ItemStackParser.parse(config).getItemMeta();

        assertEquals(5, meta.getEnchantLevel(Enchantment.SHARPNESS));
    }

    @Test
    void rejectsInvalidEnchantment()
    {
        YamlConfiguration config = new YamlConfiguration();
        config.set("material", "DIAMOND_SWORD");
        config.set("enchantments.not-a-real-enchantment", 1);

        assertThrows(
                IllegalArgumentException.class,
                () -> ItemStackParser.parse(config)
        );
    }

    @Test
    void glowAddsHiddenEnchant()
    {
        YamlConfiguration config = new YamlConfiguration();
        config.set("material", "DIAMOND_SWORD");
        config.set("glow", true);

        ItemMeta meta = ItemStackParser.parse(config).getItemMeta();

        assertTrue(meta.hasEnchants());
        assertTrue(meta.hasItemFlag(ItemFlag.HIDE_ENCHANTS));
    }

    @Test
    void substitutesPlaceholdersInMaterialNameAndLore()
    {
        YamlConfiguration config = new YamlConfiguration();
        config.set("material", "{material}");
        config.set("name", "&ePage {current}/{total}");
        config.set("lore", List.of("&7You are on page {current}"));

        ItemStack item = ItemStackParser.parse(config, Map.of(
                "material", "PAPER", "current", 2, "total", 5
        ));

        assertEquals(Material.PAPER, item.getType());

        ItemMeta meta = item.getItemMeta();

        assertEquals("Page 2/5", PlainTextComponentSerializer.plainText().serialize(meta.displayName()));
        assertEquals("You are on page 2", PlainTextComponentSerializer.plainText().serialize(meta.lore().get(0)));
    }

    @Test
    void nullPlaceholdersLeavesTextUnchanged()
    {
        YamlConfiguration config = new YamlConfiguration();
        config.set("material", "DIAMOND_SWORD");
        config.set("name", "&aLiteral {brace}");

        ItemStack item = ItemStackParser.parse(config, null);

        assertEquals(
                "Literal {brace}",
                PlainTextComponentSerializer.plainText().serialize(item.getItemMeta().displayName())
        );
    }

    @Test
    void parseSafelyWithPlaceholdersFallsBackOnFailure()
    {
        YamlConfiguration config = new YamlConfiguration();
        config.set("material", "{material}");

        ItemStack item = ItemStackParser.parseSafely(config, Map.of("material", "NOT_REAL"));

        assertEquals(Material.BARRIER, item.getType());
    }

    @Test
    void parseSafelyReturnsBrokenItemOnInvalidMaterial()
    {
        YamlConfiguration config = new YamlConfiguration();
        config.set("material", "NOT_A_MATERIAL");

        ItemStack item = ItemStackParser.parseSafely(config);

        assertEquals(Material.BARRIER, item.getType());
        assertNotNull(item.getItemMeta().lore());
    }

    @Test
    void parseSafelyReturnsBrokenItemOnNullSection()
    {
        ItemStack item = ItemStackParser.parseSafely(null);

        assertEquals(Material.BARRIER, item.getType());
    }

    @Test
    void parseSafelyReturnsValidItemUnchanged()
    {
        YamlConfiguration config = new YamlConfiguration();
        config.set("material", "DIAMOND_SWORD");

        ItemStack item = ItemStackParser.parseSafely(config);

        assertEquals(Material.DIAMOND_SWORD, item.getType());
    }

    @Test
    void parseSafelyUsesProvidedFallback()
    {
        YamlConfiguration config = new YamlConfiguration();
        config.set("material", "NOT_A_MATERIAL");

        ItemStack fallback = new ItemStack(Material.STICK);
        ItemStack item = ItemStackParser.parseSafely(config, fallback);

        assertEquals(Material.STICK, item.getType());
        assertNotSame(fallback, item);
    }

    @Test
    void parsesSkullOwner()
    {
        YamlConfiguration config = new YamlConfiguration();
        config.set("material", "PLAYER_HEAD");
        config.set("skull-owner", "Notch");

        ItemStack item = ItemStackParser.parse(config);

        assertEquals(Material.PLAYER_HEAD, item.getType());

        SkullMeta meta = (SkullMeta) item.getItemMeta();

        assertNotNull(meta.getOwningPlayer());
        assertEquals("Notch", meta.getOwningPlayer().getName());
    }
}
