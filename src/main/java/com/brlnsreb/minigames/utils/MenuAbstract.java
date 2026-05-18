package com.brlnsreb.minigames.utils;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import cn.nukkit.Player;

public abstract class MenuAbstract {

    protected final Map<UUID, Long> openingCooldown;

    public MenuAbstract() {
        this.openingCooldown = new ConcurrentHashMap<>();
    }

    protected boolean checkCooldown(Player player) {
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
