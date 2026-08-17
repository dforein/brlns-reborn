package org.brlnsreb.minigames.mm.match.game.items;

import org.brlnsreb.BrlnsReb;
import org.brlnsreb.core.minigame.match.GameStateType;
import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.core.player.PlayerUtils;
import org.brlnsreb.minigames.mm.match.game.MMGame;
import org.brlnsreb.minigames.mm.match.game.gamedata.MMPlayerGameData;
import org.brlnsreb.minigames.mm.match.game.gamedata.MMRole;
import org.brlnsreb.utils.ItemManager;
import org.powernukkitx.Player;
import org.powernukkitx.entity.effect.EffectType;
import org.powernukkitx.inventory.Inventory;
import org.powernukkitx.item.Item;
import org.powernukkitx.item.enchantment.Enchantment;
import org.powernukkitx.scheduler.ServerScheduler;

public class MMItemManager extends ItemManager {

    public static final String PATH = "game.items.";
    public final MMGame game;

    public MMItemManager(MMGame game) {
        super(game.getConfig());
        
        this.game = game;
    }

    public void giveItemsByRole(Player player, MMPlayerGameData gameData) {
        PlayerUtils.clearInventory(player);

        switch (gameData.role) {
            case MURDERER -> giveMurdererItems(player);
            case SHERIFF -> giveSheriffItems(player);
            case INNOCENT -> {}
        }
    }

    private void giveMurdererItems(Player player) {
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
        player.getInventory().setHeldItemIndex(0);

        giveItem(
            player, 
            1, 
            Item.GOLDEN_HOE, getStr(PATH + "hoe.name")
        );
    }

    public void giveYellowDye() {
        Item dye = buildItem(Item.YELLOW_DYE, getStr(PATH + "yellow-dye.name"));
        
        for (CustomPlayer p : game.getPlayers()) {
            if (game.getGameData(p).role != MMRole.INNOCENT) continue;

            Inventory inventory = p.getInventory();
            if (inventory.contains(dye) || inventory.contains(Item.get(Item.GOLDEN_HOE))) continue;

            inventory.setItem(2, dye);
        }
    }

    public void removeYellowDye() {
        for (CustomPlayer p : game.getPlayers()) {
            if (game.getGameData(p).role != MMRole.INNOCENT) continue;
            PlayerUtils.clearItem(p, Item.YELLOW_DYE);
        }
    }

    public void useFlash(CustomPlayer murderer, ServerScheduler scheduler) {
        PlayerUtils.clearItem(murderer, Item.BLAZE_ROD);

        int blindnessDuration = config.getInt(PATH + "blaze-rod.blindness-duration");
        Integer[] placeholder = { blindnessDuration };
        blindnessDuration *= 20;

        for (CustomPlayer p : game.getPlayers()) {
            if (p.equals(murderer)) continue;
            giveBlindness(p, blindnessDuration, placeholder);
        }

        for (CustomPlayer s : game.getSpectators()) {
            giveBlindness(s, blindnessDuration, placeholder);
        }

        game.getMsgUtil().sendPresetMessagePrefix(murderer, "lights-out-murderer", placeholder);

        if (game.getCurrentState() == GameStateType.ENDING) return;
        
        scheduler.scheduleDelayedTask(BrlnsReb.instance,
            () -> {
                if (game.getCurrentState() == GameStateType.IN_GAME) {
                    game.getMsgUtil().sendPresetMessagePrefix(murderer, "lights-out-over");
                }
            },
            blindnessDuration
        );
    }

    private void giveBlindness(CustomPlayer player, int blindnessDuration, Integer[] placeholder) {
        PlayerUtils.giveEffect(
            player, 
            EffectType.BLINDNESS, 
            blindnessDuration,
            0, 
            false
        );

        game.getMsgUtil().sendPresetMessagePrefix(player, "lights-out-others", placeholder);
    }

}
