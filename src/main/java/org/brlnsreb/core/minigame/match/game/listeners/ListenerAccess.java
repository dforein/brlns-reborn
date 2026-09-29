package org.brlnsreb.core.minigame.match.game.listeners;

import java.util.Set;

import org.brlnsreb.core.minigame.match.game.Game;
import org.brlnsreb.core.player.CustomPlayer;
import org.powernukkitx.entity.item.EntityItem;
import org.powernukkitx.event.entity.EntityDamageEvent;
import org.powernukkitx.event.entity.ProjectileHitEvent;
import org.powernukkitx.event.player.PlayerChatEvent;
import org.powernukkitx.event.player.PlayerCommandPreprocessEvent;
import org.powernukkitx.event.player.PlayerDropItemEvent;
import org.powernukkitx.event.player.PlayerItemHeldEvent;
import org.powernukkitx.item.Item;

public abstract class ListenerAccess {

    protected Set<CustomPlayer> players;
    protected Set<CustomPlayer> spectators;

    public ListenerAccess(Game game) {
        this.players = game.getPlayers();
        this.spectators = game.getSpectators();
    }
    
    public abstract void onItemUse(CustomPlayer player, Item item);
    public abstract boolean onItemPickup(CustomPlayer player, EntityItem itemEntity);
    public abstract boolean onItemHeld(CustomPlayer player, PlayerItemHeldEvent event);
    public abstract boolean onItemDrop(CustomPlayer player, PlayerDropItemEvent event);
    public abstract void onPlayerDamage(CustomPlayer player, EntityDamageEvent event);
    public abstract void onProjectileHit(CustomPlayer player, ProjectileHitEvent event);
    public abstract boolean onChat(CustomPlayer player, PlayerChatEvent event);
    public abstract boolean onCommandPreprocess(CustomPlayer player, PlayerCommandPreprocessEvent event);

}