package org.brlnsreb.utils;

import cn.nukkit.Player;
import cn.nukkit.item.Item;
import cn.nukkit.item.enchantment.Enchantment;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.TextFormat;

public class ItemManager {

    protected Config config;

    public ItemManager(Config config) {
        this.config = config;
    }

    protected String getStr(String path) {
        return YamlUtil.getStr(path, this.config);
    }

    public static void giveItem(Player player, int slot, String itemId, String itemName) {
        //give player an item (with name)
        Item item = buildItem(itemId, itemName);
        player.getInventory().setItem(slot, item);
    }

    public static void giveItem(Player player, int slot, String itemId, String itemName, int enchantmentId, int enchantmentLevel) {
        //give player an item (with name + enchantment)
        Item item = buildItem(itemId, itemName, enchantmentId, enchantmentLevel);
        player.getInventory().setItem(slot, item);
    }

    //custom giveItem with name + enchantment + other, using buildItem to prepare an item object

    public static Item buildItem(String itemId, String itemName) {
        Item item = Item.get(itemId);
        item.setCustomName(TextFormat.colorize(itemName));

        return item;
    }

    public static Item buildItem(String itemId, String itemName, int enchantmentId, int enchantmentLevel) {
        Item item = Item.get(itemId);
        item.setCustomName(TextFormat.colorize(itemName));
        item.addEnchantment(Enchantment.get(enchantmentId).setLevel(enchantmentLevel));

        return item;
    }
    
}