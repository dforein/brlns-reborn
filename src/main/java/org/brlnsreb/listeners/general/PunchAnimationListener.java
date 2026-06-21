package org.brlnsreb.listeners.general;

import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerAnimationEvent;
import cn.nukkit.network.protocol.AnimatePacket;
import org.brlnsreb.MinigameCore;
import org.brlnsreb.utils.CustomPlaySoundPacket;
import java.util.HashSet;
import java.util.UUID;

public class PunchAnimationListener implements Listener {
    //UNUSED LISTENER (here just for future development)

    private final MinigameCore plugin;
    private final HashSet<UUID> hitSoundCooldowns = new HashSet<>();

    public PunchAnimationListener(MinigameCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority=EventPriority.LOW)
    private void onAnimation(PlayerAnimationEvent event) {
        Player player = event.getPlayer();

        if (player != null 
            && event.getAnimationType() == AnimatePacket.Action.SWING_ARM 
            && !this.hitSoundCooldowns.contains(player.getUniqueId())) {

            CustomPlaySoundPacket packet = new CustomPlaySoundPacket();

            packet.sendDirectionalSoundTo(player, "game.player.attack.nodamage");

            this.hitSoundCooldowns.add(player.getUniqueId());
            this.plugin.getServer().getScheduler().scheduleDelayedTask(() -> {
                this.hitSoundCooldowns.remove(player.getUniqueId());
            }, 4);
        }
    }
}