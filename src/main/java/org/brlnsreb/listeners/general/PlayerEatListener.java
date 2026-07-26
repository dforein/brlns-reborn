package org.brlnsreb.listeners.general;

import java.util.HashSet;
import java.util.UUID;

import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.core.player.PlayerUtils;
import org.powernukkitx.event.EventHandler;
import org.powernukkitx.event.Listener;
import org.powernukkitx.event.player.PlayerFoodLevelChangeEvent;
import org.powernukkitx.plugin.annotation.EventListener;

@EventListener
public class PlayerEatListener implements Listener {

    private final HashSet<UUID> idSet = new HashSet<>();
    
    @EventHandler
    public void onPlayerFoodLevelChange(PlayerFoodLevelChangeEvent event) {
        CustomPlayer player = (CustomPlayer) event.getPlayer();
        if (idSet.remove(player.getUniqueId())) return;

        idSet.add(player.getUniqueId());
        PlayerUtils.setFood(player, 18);
    }

}
