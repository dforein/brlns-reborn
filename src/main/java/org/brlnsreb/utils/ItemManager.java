package org.brlnsreb.utils;

import org.powernukkitx.Player;
import org.powernukkitx.item.Item;
import org.powernukkitx.item.enchantment.Enchantment;
import org.powernukkitx.nbt.tag.CompoundTag;
import org.powernukkitx.utils.Config;
import org.powernukkitx.utils.TextFormat;

public class ItemManager {

    protected Config config;

    public ItemManager(Config config) {
        this.config = config;
    }

    protected String getStr(String path) {
        return YamlUtil.getStr(path, this.config);
    }

    public static void giveItem(Player player, int slot, Item item) {
        player.getInventory().setItem(slot, item);
    }

    public static void giveItem(Player player, int slot, String itemId, String itemName) {
        //give player an item (with name)
        Item item = buildItem(itemId, itemName);
        player.getInventory().setItem(slot, item);
    }

    public static void giveItem(Player player, int slot, String itemId, String itemName, boolean unbreakable) {
        //give player an item (with name + unbreakable)
        Item item = buildItem(itemId, itemName, unbreakable);
        player.getInventory().setItem(slot, item);
    }

    public static void giveItem(Player player, int slot, String itemId, String itemName, int enchantmentId, int enchantmentLevel) {
        //give player an item (with name + enchantment)
        Item item = buildItem(itemId, itemName, enchantmentId, enchantmentLevel);
        player.getInventory().setItem(slot, item);
    }

    public static void giveItem(Player player, int slot, String itemId, String itemName, int enchantmentId, int enchantmentLevel, boolean unbreakable) {
        //give player an item (with name + enchantment + unbreakable)
        Item item = buildItem(itemId, itemName, enchantmentId, enchantmentLevel, unbreakable);
        player.getInventory().setItem(slot, item);
    }

    //custom giveItem with name + enchantment + other, using buildItem to prepare an item object

    public static Item buildItem(String itemId, String itemName) {
        Item item = Item.get(itemId);
        item.setCustomName(TextFormat.colorize(itemName));

        return item;
    }

    public static Item buildItem(String itemId, String itemName, boolean unbreakable) {
        Item item = buildItem(itemId, itemName);
        if (unbreakable) makeUnbreakable(item);

        return item;
    }

    public static Item buildItem(String itemId, String itemName, int enchantmentId, int enchantmentLevel) {
        Item item = buildItem(itemId, itemName);
        item.addEnchantment(Enchantment.get(enchantmentId).setLevel(enchantmentLevel));

        return item;
    }

    public static Item buildItem(String itemId, String itemName, int enchantmentId, int enchantmentLevel, boolean unbreakable) {
        Item item = buildItem(itemId, itemName, enchantmentId, enchantmentLevel);
        if (unbreakable) makeUnbreakable(item);

        return item;
    }

    public static void makeUnbreakable(Item item) {
        CompoundTag tag = item.getNbt();
        tag.putByte("Unbreakable", 1);
        item.setNbt(tag);
    }
    
}