package org.brlnsreb.minigames.mm.match.game.gamedata;

import java.util.EnumMap;

import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.core.player.data.PlayerGameData;
import org.powernukkitx.utils.Config;

public class MMPlayerGameData extends PlayerGameData {

    public static enum MMEvent {
        GOLD,
        KILL,
        MURDERER_KILLS_SHERIFF,
        SHERIFF_KILLS_MURDERER
    };

    private static EnumMap<MMEvent, Integer> expPrizes = new EnumMap<>(MMEvent.class);
    private static EnumMap<MMEvent, Integer> coinsPrizes = new EnumMap<>(MMEvent.class);
    public MMRole role = null;

    //innocents
    public int gold = 0;
    private static final int GOLD_SHERIFF = 5;

    //murderer
    public boolean flashUsed = false;

    public MMPlayerGameData(CustomPlayer player) {
        super(player);
    }

    public boolean canBecomeSheriff() {
        return role == MMRole.INNOCENT && gold >= GOLD_SHERIFF;
    }

    public static void setExpPrizes(Config config) {
        expPrizes.put(MMEvent.GOLD, config.getInt("game.exp.per-gold"));
        expPrizes.put(MMEvent.KILL, config.getInt("game.exp.per-kill"));
        expPrizes.put(MMEvent.MURDERER_KILLS_SHERIFF, config.getInt("game.exp.murderer-kills-sheriff"));
        expPrizes.put(MMEvent.SHERIFF_KILLS_MURDERER, config.getInt("game.exp.sheriff-kills-murderer"));
    }

    public static void setCoinsPrizes(Config config) {
        coinsPrizes.put(MMEvent.KILL, config.getInt("game.coins.per-kill"));
        coinsPrizes.put(MMEvent.MURDERER_KILLS_SHERIFF, config.getInt("game.coins.murderer-kills-sheriff"));
        coinsPrizes.put(MMEvent.SHERIFF_KILLS_MURDERER, config.getInt("game.coins.sheriff-kills-murderer"));
    }

    public static int getExpPrize(MMEvent eventType) {
        return expPrizes.get(eventType);
    }

    public static int getCoinsPrize(MMEvent eventType) {
        return coinsPrizes.get(eventType);
    }

    public void addExp(MMEvent eventType) {
        addExp(expPrizes.get(eventType));
    }

    public void addCoins(MMEvent eventType) {
        addCoins(coinsPrizes.get(eventType));
    }
    
}
