package me.jackcw.jcore.menu;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public final class ConfirmMenuBuilder
{
    private static final int CONFIRM_SLOT = 2;
    private static final int INFO_SLOT = 4;
    private static final int CANCEL_SLOT = 6;

    private final MenuManager menuManager;
    private String title = "&8Are you sure?";
    private List<String> description = List.of();
    private MenuClickHandler onConfirm = context -> {};
    private MenuClickHandler onCancel = MenuContext::back;

    ConfirmMenuBuilder(MenuManager menuManager)
    {
        this.menuManager = menuManager;
    }

    public ConfirmMenuBuilder title(String title)
    {
        this.title = title;
        return this;
    }

    public ConfirmMenuBuilder description(List<String> description)
    {
        this.description = description;
        return this;
    }

    public ConfirmMenuBuilder onConfirm(MenuClickHandler onConfirm)
    {
        this.onConfirm = onConfirm;
        return this;
    }

    public ConfirmMenuBuilder onCancel(MenuClickHandler onCancel)
    {
        this.onCancel = onCancel;
        return this;
    }

    public void open(Player player)
    {
        MenuNavigationStyle style = menuManager.navigationStyle();

        Menu menu = menuManager.builder(title, 1)
                .item(CONFIRM_SLOT, style.confirm(), onConfirm)
                .item(INFO_SLOT, infoItem())
                .item(CANCEL_SLOT, style.cancel(), onCancel)
                .build();

        menu.open(player);
    }

    private ItemStack infoItem()
    {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(color("&eConfirmation Required"));

        List<Component> lore = new ArrayList<>();

        for (String line : description)
            lore.add(color(line));

        meta.lore(lore);
        item.setItemMeta(meta);

        return item;
    }

    private Component color(String text)
    {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(text);
    }
}
