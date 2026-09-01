package org.brlnsreb.minigames.mm.match.game.listeners;

import org.brlnsreb.core.minigame.match.game.listeners.ListenerAccess;
import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.minigames.mm.match.game.MMGame;
import org.brlnsreb.minigames.mm.match.game.entities.ThrownSwordEntity;
import org.brlnsreb.minigames.mm.match.game.gamedata.MMPlayerGameData;
import org.brlnsreb.minigames.mm.match.game.gamedata.MMRole;
import org.powernukkitx.entity.item.EntityItem;
import org.powernukkitx.event.entity.EntityDamageByEntityEvent;
import org.powernukkitx.event.entity.EntityDamageEvent;
import org.powernukkitx.event.entity.ProjectileHitEvent;
import org.powernukkitx.event.player.PlayerChatEvent;
import org.powernukkitx.event.player.PlayerCommandPreprocessEvent;
import org.powernukkitx.event.player.PlayerDropItemEvent;
import org.powernukkitx.event.player.PlayerItemHeldEvent;
import org.powernukkitx.item.Item;
import org.powernukkitx.item.ItemGoldIngot;
import org.powernukkitx.item.ItemGoldenHoe;

public class MMListenerAccess extends ListenerAccess {

    private static final String[] blockedChatCommands = {
        "grm",
        "frm",
        "say",
        "whisper",
        "tell",
        "msg",
        "me"
    };

    private final MMGame game;

    private static float damageMultiplier;

    public MMListenerAccess(MMGame game) {
        super(game);
        this.game = game;

        damageMultiplier = (float) this.game.getConfig().getDouble("game.murderer-damage-multiplier");
    }

    //items

    public void onItemUse(CustomPlayer player, Item item) {
        switch (item.getId()) {
            //sheriff
            case Item.GOLDEN_HOE -> {
                CustomPlayer victim = game.shoot(player);
                if (victim != null) game.getMatch().onDeath(victim, player);
            }

            //murderer
            case Item.IRON_SWORD -> game.throwSword(player);
            case Item.BLAZE_ROD -> game.useFlash(player);

            //innocent
            case Item.YELLOW_DYE -> game.newSheriff(player, true);
            
            //spectator
            case Item.COMPASS -> game.getSpectatorMenu().openSpectateMenu(player);
            case Item.CLOCK -> game.getSpectatorMenu().openActionsMenu(player);
        }
    }

    public boolean onItemPickup(CustomPlayer player, EntityItem itemEntity) {
        if (!game.isInGame()) return false;

        if (itemEntity.getItem() instanceof ItemGoldIngot) {
            if (game.collectGold(player)) itemEntity.close();
        }

        if (itemEntity.getItem() instanceof ItemGoldenHoe) {
            if (game.newSheriff(player, false)) itemEntity.close();
        }

        return false;
    }

    public boolean onItemHeld(CustomPlayer player, PlayerItemHeldEvent event) { return true; }
    public boolean onItemDrop(CustomPlayer player, PlayerDropItemEvent event) { return false; }


    //damage

    public void onPlayerDamage(CustomPlayer player, EntityDamageEvent event) {
        if (event instanceof EntityDamageByEntityEvent e) {
            MMPlayerGameData damagerGameData = game.getGameData(e.getDamager());
            if (damagerGameData == null || damagerGameData.role != MMRole.MURDERER) return;

            event.setDamage(event.getFinalDamage() * damageMultiplier);
        }
    }


    //projectile

    public void onProjectileHit(CustomPlayer player, ProjectileHitEvent event) {
        if (!game.isInGame()) return;
        if (!(event.getEntity() instanceof ThrownSwordEntity thrownSword)) return;
        if (player == game.getMurderer()) return;

        game.getMatch().onDeath(player, (CustomPlayer) thrownSword.shootingEntity);
    }


    //chat

    public boolean onChat(CustomPlayer player, PlayerChatEvent event) {
        if (!game.isInGame()) return true;

        if (players.contains(player)) return roleCheckOnChat(player);
        if (spectators.contains(player)) {
            event.getRecipients().removeIf(recipient -> players.contains(recipient));
            return true;
        }

        return false;
    }

    public boolean onCommandPreprocess(CustomPlayer player, PlayerCommandPreprocessEvent event) {
        if (!game.isInGame()) return true;

        String command = event.getMessage()
            .substring(1)
            .trim()
            .split(" ")[0];

        for (String blocked : blockedChatCommands) {
            if (!command.equals(blocked)) continue;

            if (players.contains(player)) return roleCheckOnChat(player);
            if (spectators.contains(player)) {
                game.getMsgUtil().sendPresetMessagePrefix(
                    player, 
                    "no-chat-spectators", 
                    new String[] { "spectators" }
                );
                return false;
            }
        }
        
        return true;
    }

    private boolean roleCheckOnChat(CustomPlayer player) {
        switch (game.getGameData(player).role) {
            case MURDERER:
                game.getMsgUtil().sendPresetMessagePrefix(
                    player, 
                    "no-chat", 
                    new String[] { "the murderer" }
                );
                return false;

            case SHERIFF:
                game.getMsgUtil().sendPresetMessagePrefix(
                    player, 
                    "no-chat", 
                    new String[] { "the sheriff" }
                );
                return false;

            case INNOCENT: return true;
            default: return true;
        }
    }
    
}
