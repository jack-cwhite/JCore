package me.jackcw.jcore.menu;

import me.jackcw.jcore.item.ItemStackParser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public final class MenuNavigationStyle
{
    private final ItemStack previous;
    private final ItemStack next;
    private final ItemStack back;
    private final ItemStack confirm;
    private final ItemStack cancel;
    private final ItemStack pageIndicatorBase;
    private final String pageIndicatorName;
    private final List<String> pageIndicatorLore;

    private MenuNavigationStyle(ItemStack previous, ItemStack next, ItemStack back, ItemStack confirm, ItemStack cancel, ItemStack pageIndicatorBase, String pageIndicatorName, List<String> pageIndicatorLore)
    {
        this.previous = previous;
        this.next = next;
        this.back = back;
        this.confirm = confirm;
        this.cancel = cancel;
        this.pageIndicatorBase = pageIndicatorBase;
        this.pageIndicatorName = pageIndicatorName;
        this.pageIndicatorLore = pageIndicatorLore;
    }

    public static MenuNavigationStyle defaults()
    {
        return new MenuNavigationStyle(
                namedItem(Material.ARROW, "&ePrevious Page"),
                namedItem(Material.ARROW, "&eNext Page"),
                namedItem(Material.BARRIER, "&cBack"),
                namedItem(Material.LIME_WOOL, "&aConfirm"),
                namedItem(Material.RED_WOOL, "&cCancel"),
                null, null, null
        );
    }

    public static MenuNavigationStyle from(ConfigurationSection section)
    {
        if (section == null)
            return defaults();

        ItemStack previous = ItemStackParser.parseSafely(section.getConfigurationSection("previous"), namedItem(Material.ARROW, "&ePrevious Page"));

        ItemStack next = ItemStackParser.parseSafely(section.getConfigurationSection("next"), namedItem(Material.ARROW, "&eNext Page"));

        ItemStack back = ItemStackParser.parseSafely(section.getConfigurationSection("back"), namedItem(Material.BARRIER, "&cBack"));

        ItemStack confirm = ItemStackParser.parseSafely(section.getConfigurationSection("confirm"), namedItem(Material.LIME_WOOL, "&aConfirm"));

        ItemStack cancel = ItemStackParser.parseSafely(section.getConfigurationSection("cancel"), namedItem(Material.RED_WOOL, "&cCancel"));

        ConfigurationSection pageIndicator = section.getConfigurationSection("page-indicator");

        ItemStack pageIndicatorBase = null;
        String pageIndicatorName = null;
        List<String> pageIndicatorLore = null;

        if (pageIndicator != null)
        {
            pageIndicatorBase = ItemStackParser.parseSafely(pageIndicator);
            pageIndicatorName = pageIndicator.getString("name");
            pageIndicatorLore = pageIndicator.getStringList("lore");
        }

        return new MenuNavigationStyle(previous, next, back, confirm, cancel, pageIndicatorBase, pageIndicatorName, pageIndicatorLore);
    }

    public ItemStack previous()
    {
        return previous.clone();
    }

    public ItemStack next()
    {
        return next.clone();
    }

    public ItemStack back()
    {
        return back.clone();
    }

    public ItemStack confirm()
    {
        return confirm.clone();
    }

    public ItemStack cancel()
    {
        return cancel.clone();
    }

    public ItemStack pageIndicator(int current, int total)
    {
        if (pageIndicatorBase == null)
            return namedItem(Material.PAPER, "&ePage " + current + "/" + total);

        ItemStack item = pageIndicatorBase.clone();
        ItemMeta meta = item.getItemMeta();

        if (pageIndicatorName != null)
            meta.displayName(color(substitute(pageIndicatorName, current, total)));

        if (pageIndicatorLore != null && !pageIndicatorLore.isEmpty())
        {
            List<Component> lore = new ArrayList<>();

            for (String line : pageIndicatorLore)
                lore.add(color(substitute(line, current, total)));

            meta.lore(lore);
        }

        item.setItemMeta(meta);

        return item;
    }

    private static String substitute(String text, int current, int total)
    {
        return text.replace("{current}", String.valueOf(current)).replace("{total}", String.valueOf(total));
    }

    private static Component color(String text)
    {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(text);
    }

    private static ItemStack namedItem(Material material, String name)
    {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(color(name));

        item.setItemMeta(meta);

        return item;
    }
}
