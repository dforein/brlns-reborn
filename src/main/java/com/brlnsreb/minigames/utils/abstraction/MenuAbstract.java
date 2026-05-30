package com.brlnsreb.minigames.utils.abstraction;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import cn.nukkit.Player;

public abstract class MenuAbstract {

    protected static final Map<UUID, Long> openingCooldown = new ConcurrentHashMap<>();

    protected static boolean checkCooldown(Player player) {
        long now = System.currentTimeMillis();
        UUID uuid = player.getUniqueId();

        if (openingCooldown.containsKey(uuid)
            && now - openingCooldown.get(uuid) < 500) {
            return true;
        }

        openingCooldown.put(uuid, now);
        return false;
    }

}
