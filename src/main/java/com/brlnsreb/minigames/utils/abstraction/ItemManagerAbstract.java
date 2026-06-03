package com.brlnsreb.minigames.utils.abstraction;

import cn.nukkit.Player;
import cn.nukkit.item.Item;
import cn.nukkit.item.enchantment.Enchantment;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.TextFormat;

public abstract class ItemManagerAbstract {

    protected Config config;

    public ItemManagerAbstract(Config config) {
        this.config = config;
    }

    public void reloadConfig(Config config) {
        this.config = config;
    }

    public static void giveItem(Player player, int slot, String itemId, String itemName) {
        Item item = buildItem(itemId, itemName);
        player.getInventory().setItem(slot, item);
    }

    public static void giveItem(Player player, int slot, String itemId, String itemName, int enchantmentId, int enchantmentLevel) {
        Item item = buildItem(itemId, itemName, enchantmentId, enchantmentLevel);
        player.getInventory().setItem(slot, item);
    }

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