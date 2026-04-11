package com.brlnsreb.minigames.listeners;

import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerAnimationEvent;
import cn.nukkit.network.protocol.AnimatePacket;
import com.brlnsreb.minigames.MinigameCore;
import com.brlnsreb.minigames.utils.CustomPlaySoundPacket;
import java.util.HashSet;

//unused
public class PunchAnimationListener implements Listener {

    private final MinigameCore plugin;
    private final HashSet<String> hitSoundCooldowns = new HashSet<>();

    public PunchAnimationListener(MinigameCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority=EventPriority.LOW)
    private void onAnimation(PlayerAnimationEvent event) {
        Player player = event.getPlayer();

        if (player != null 
            && event.getAnimationType() == AnimatePacket.Action.SWING_ARM 
            && !this.hitSoundCooldowns.contains(player.getName())) {

            CustomPlaySoundPacket packet = new CustomPlaySoundPacket();

            packet.sendDirectionalSoundTo(player, "game.player.attack.nodamage");

            this.hitSoundCooldowns.add(player.getName());
            this.plugin.getServer().getScheduler().scheduleDelayedTask(() -> {
                this.hitSoundCooldowns.remove(player.getName());
            }, 4);
        }
    }
}