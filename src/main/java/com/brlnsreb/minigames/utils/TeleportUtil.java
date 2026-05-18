package com.brlnsreb.minigames.utils;

import com.brlnsreb.minigames.MinigameCore;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.level.Location;
import cn.nukkit.scheduler.ServerScheduler;

public class TeleportUtil {

    private static MinigameCore plugin = MinigameCore.getInstance();
    private static ServerScheduler scheduler = Server.getInstance().getScheduler();

    public static void tp(Player p, Location loc) {
        try {
            int viewDistance = p.getViewDistance();

            p.setViewDistance(2);
            p.despawnFromAll();

            p.teleport(loc);

            scheduler.scheduleDelayedTask(plugin, () -> {
                if (p.isOnline()) { 
                    p.spawnToAll(); 
                    p.setViewDistance(viewDistance);
                }
            }, 20);
        } catch (Exception e) {
            plugin.getLogger().error("Error teleporting player: " + e.getMessage());
        }
    }

}
