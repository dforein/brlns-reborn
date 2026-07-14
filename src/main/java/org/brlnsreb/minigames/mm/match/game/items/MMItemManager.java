package org.brlnsreb.minigames.mm.match.game.items;

import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.core.player.PlayerUtils;
import org.brlnsreb.minigames.mm.match.game.MMPlayerGameData;
import org.brlnsreb.utils.ItemManager;
import org.powernukkitx.Player;
import org.powernukkitx.item.Item;
import org.powernukkitx.item.enchantment.Enchantment;
import org.powernukkitx.utils.Config;

public class MMItemManager extends ItemManager {

    public static final String PATH = "game.items.";

    public MMItemManager(Config config) {
        super(config);
    }

    public void giveItemsAtStart(Player player, MMPlayerGameData gameData) {
        switch (gameData.role) {
            case MURDERER -> giveMurdererItems(player);
            case SHERIFF -> giveSheriffItems(player);
            case INNOCENT -> {}
        }
    }

    private void giveMurdererItems(Player player) {
        PlayerUtils.clearInventory(player);

        player.getInventory().setHeldItemIndex(1);
        
        giveItem(
            player, 
            0, 
            Item.IRON_SWORD, getStr(PATH + "sword.name"),
            Enchantment.ID_DAMAGE_ALL, 5, 
            true
        );

        giveItem(
            player, 
            2, 
            Item.BLAZE_ROD, 
            getStr(PATH + "blaze-rod.name")
        );
    }
    
    private void giveSheriffItems(Player player) {
        PlayerUtils.clearInventory(player);
        
        player.getInventory().setHeldItemIndex(0);

        giveItem(
            player, 
            1, 
            Item.GOLDEN_HOE, getStr(PATH + "hoe.name"),
            Enchantment.ID_DAMAGE_ALL, 5, 
            true
        );
    }

    public void giveYellowDye(Player player) {
        Item dye = buildItem(Item.YELLOW_DYE, getStr(PATH + "yellow-dye.name"));
        
        if (player.getInventory().contains(dye)
            || player.getInventory().contains(Item.get(Item.GOLDEN_HOE))) {
            return;
        }

        player.getInventory().setItem(2, dye);
    }

    public void useFlash(CustomPlayer murderer) {
        PlayerUtils.clearItem(murderer, Item.BLAZE_ROD);
    }

}
