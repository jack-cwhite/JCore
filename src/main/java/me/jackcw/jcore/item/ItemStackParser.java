package me.jackcw.jcore.item;

import me.jackcw.jcore.command.ArgumentTypes;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public final class ItemStackParser
{
    private static final Logger LOGGER = Logger.getLogger(ItemStackParser.class.getName());

    private ItemStackParser()
    {
    }

    public static ItemStack parse(ConfigurationSection section)
    {
        return parse(section, null);
    }

    public static ItemStack parse(ConfigurationSection section, Map<String, Object> placeholders)
    {
        return parse(section, placeholders, false);
    }

    public static ItemStack parse(ConfigurationSection section, Map<String, Object> placeholders, boolean useAlternate)
    {
        if (section == null)
            throw new IllegalArgumentException("Item configuration section cannot be null");

        String materialKey = alternateKey(section, "material", useAlternate);
        String nameKey = alternateKey(section, "name", useAlternate);
        String loreKey = alternateKey(section, "lore", useAlternate);

        String materialName = substitute(section.getString(materialKey), placeholders);

        if (materialName == null)
            throw new IllegalArgumentException("Item configuration is missing '" + materialKey + "'");

        Material material = ArgumentTypes.material().parse(materialName);
        int amount = Math.max(1, section.getInt("amount", 1));

        ItemStack item = new ItemStack(material, amount);
        ItemMeta meta = item.getItemMeta();

        if (section.contains(nameKey))
            meta.displayName(color(substitute(section.getString(nameKey), placeholders)));

        if (section.contains(loreKey))
        {
            List<Component> lore = new ArrayList<>();

            for (String line : section.getStringList(loreKey))
                lore.add(color(substitute(line, placeholders)));

            meta.lore(lore);
        }

        if (section.contains("custom-model-data"))
            meta.setCustomModelData(section.getInt("custom-model-data"));

        if (section.getBoolean("unbreakable", false))
            meta.setUnbreakable(true);

        for (String flagName : section.getStringList("flags"))
            meta.addItemFlags(parseFlag(flagName));

        ConfigurationSection enchantments = section.getConfigurationSection("enchantments");

        if (enchantments != null)
            for (String key : enchantments.getKeys(false))
                meta.addEnchant(parseEnchantment(key), enchantments.getInt(key), true);

        if (section.getBoolean("glow", false))
        {
            meta.addEnchant(Enchantment.LUCK_OF_THE_SEA, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        if (meta instanceof SkullMeta skullMeta && section.contains("skull-owner"))
            skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(section.getString("skull-owner")));

        item.setItemMeta(meta);

        return item;
    }

    public static ItemStack parseSafely(ConfigurationSection section)
    {
        return parseSafely(section, (Map<String, Object>) null, null);
    }

    public static ItemStack parseSafely(ConfigurationSection section, ItemStack fallback)
    {
        return parseSafely(section, null, fallback);
    }

    public static ItemStack parseSafely(ConfigurationSection section, Map<String, Object> placeholders)
    {
        return parseSafely(section, placeholders, null);
    }

    public static ItemStack parseSafely(ConfigurationSection section, Map<String, Object> placeholders, ItemStack fallback)
    {
        return parseSafely(section, placeholders, false, fallback);
    }

    public static ItemStack parseSafely(ConfigurationSection section, Map<String, Object> placeholders, boolean useAlternate)
    {
        return parseSafely(section, placeholders, useAlternate, null);
    }

    public static ItemStack parseSafely(ConfigurationSection section, Map<String, Object> placeholders, boolean useAlternate, ItemStack fallback)
    {
        try
        {
            return parse(section, placeholders, useAlternate);
        }
        catch (Exception e)
        {
            LOGGER.warning("Invalid item at '" + (section != null ? section.getCurrentPath() : "unknown") + "': " + e.getMessage());
            return fallback != null ? fallback.clone() : brokenItem(section, e);
        }
    }

    private static String alternateKey(ConfigurationSection section, String base, boolean useAlternate)
    {
        String alternate = "alternate-" + base;
        return useAlternate && section.contains(alternate) ? alternate : base;
    }

    public static String substitute(String text, Map<String, Object> placeholders)
    {
        if (text == null || placeholders == null)
            return text;

        for (Map.Entry<String, Object> entry : placeholders.entrySet())
            text = text.replace("{" + entry.getKey() + "}", String.valueOf(entry.getValue()));

        return text;
    }

    private static ItemStack brokenItem(ConfigurationSection section, Exception e)
    {
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(color("&cInvalid menu item"));

        List<Component> lore = new ArrayList<>();
        lore.add(color("&7Path: " + (section != null ? section.getCurrentPath() : "unknown")));
        lore.add(color("&7" + e.getMessage()));

        meta.lore(lore);
        item.setItemMeta(meta);

        return item;
    }

    private static ItemFlag parseFlag(String name)
    {
        try
        {
            return ItemFlag.valueOf(name.toUpperCase());
        }
        catch (IllegalArgumentException e)
        {
            throw new IllegalArgumentException("'" + name + "' is not a valid item flag", e);
        }
    }

    private static Enchantment parseEnchantment(String name)
    {
        Enchantment enchantment = Registry.ENCHANTMENT.get(NamespacedKey.minecraft(name.toLowerCase()));

        if (enchantment == null)
            throw new IllegalArgumentException("'" + name + "' is not a valid enchantment");

        return enchantment;
    }

    private static Component color(String text)
    {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(text);
    }
}
