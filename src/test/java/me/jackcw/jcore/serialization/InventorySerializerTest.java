package me.jackcw.jcore.serialization;

import me.jackcw.jcore.TestPlugin;
import me.jackcw.jcore.storage.YamlFile;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class InventorySerializerTest
{
    private TestPlugin plugin;
    private Player player;
    private InventorySerializer serializer;

    @BeforeEach
    void setup()
    {
        MockBukkit.mock();
        plugin = MockBukkit.load(TestPlugin.class);
        player = MockBukkit.getMock().addPlayer();
        serializer = new InventorySerializer();
    }

    @AfterEach
    void cleanup()
    {
        MockBukkit.unmock();
    }

    @Test
    void playerInventoryRoundTripWorks()
    {
        PlayerInventory inventory = player.getInventory();

        ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
        ItemStack apples = new ItemStack(Material.GOLDEN_APPLE, 8);
        ItemStack helmet = new ItemStack(Material.DIAMOND_HELMET);
        ItemStack shield =new ItemStack(Material.SHIELD);

        inventory.clear();

        inventory.setItem(0, sword);
        inventory.setItem(10, apples);
        inventory.setHelmet(helmet);
        inventory.setItemInOffHand(shield);

        Object serialized = serializer.serialize(inventory);

        inventory.clear();

        serializer.deserializeInto(serialized, inventory);

        assertItem(inventory.getItem(0), Material.DIAMOND_SWORD, 1);
        assertItem(inventory.getItem(10), Material.GOLDEN_APPLE, 8);
        assertItem(inventory.getHelmet(), Material.DIAMOND_HELMET, 1);
        assertItem(inventory.getItemInOffHand(), Material.SHIELD, 1);
    }

    @Test
    void playerInventoryYamlRoundTripWorks()
    {
        PlayerInventory inventory = player.getInventory();

        inventory.clear();

        inventory.setItem(0, new ItemStack(Material.DIAMOND_SWORD));
        inventory.setHelmet(new ItemStack(Material.DIAMOND_HELMET));
        inventory.setItemInOffHand(new ItemStack(Material.SHIELD));

        Object serialized = serializer.serialize(inventory);

        SerializerManager serializerManager = new SerializerManager();

        YamlFile file = new YamlFile(plugin, serializerManager, "inventory-test.yml");

        file.set("inventory", serialized);
        file.save();
        file.reload();

        inventory.clear();

        Map<String, Object> yamlValue = file.get("inventory", Map.class);

        serializer.deserializeInto(yamlValue, inventory);

        assertItem(inventory.getItem(0), Material.DIAMOND_SWORD, 1);
        assertItem(inventory.getHelmet(), Material.DIAMOND_HELMET, 1);
        assertItem(inventory.getItemInOffHand(), Material.SHIELD, 1);
    }

    @Test
    void itemMetaSurvivesRoundTrip()
    {
        PlayerInventory inventory = player.getInventory();
        inventory.clear();

        ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta meta = sword.getItemMeta();
        meta.displayName(Component.text("Champion Sword"));
        meta.lore(List.of(Component.text("Winner of tournament"), Component.text("Forged by JCore")));
        sword.setItemMeta(meta);

        inventory.setItem(0, sword);

        Object serialized = serializer.serialize(inventory);

        inventory.clear();

        serializer.deserializeInto(serialized, inventory);

        ItemStack restored = inventory.getItem(0);

        assertNotNull(restored);
        assertEquals(Material.DIAMOND_SWORD, restored.getType());

        ItemMeta restoredMeta = restored.getItemMeta();

        assertNotNull(restoredMeta);
        assertEquals("Champion Sword", PlainTextComponentSerializer.plainText().serialize(restoredMeta.displayName()));
        assertEquals(2, restoredMeta.lore().size());
        assertEquals("Winner of tournament", PlainTextComponentSerializer.plainText().serialize(restoredMeta.lore().get(0)));
    }

    @Test
    void enchantmentsSurviveRoundTrip()
    {
        PlayerInventory inventory = player.getInventory();
        inventory.clear();

        ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
        sword.addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.SHARPNESS, 5);

        inventory.setItem(0, sword);

        Object serialized = serializer.serialize(inventory);

        inventory.clear();

        serializer.deserializeInto(serialized, inventory);

        ItemStack restored = inventory.getItem(0);

        assertNotNull(restored);
        assertEquals(Material.DIAMOND_SWORD, restored.getType());
        assertEquals(5, restored.getEnchantmentLevel(org.bukkit.enchantments.Enchantment.SHARPNESS));
    }

    private void assertItem(ItemStack item, Material material, int amount)
    {
        assertNotNull(item);
        assertEquals(material, item.getType());
        assertEquals(amount, item.getAmount());
    }
}