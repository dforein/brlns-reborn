package org.brlnsreb.utils.abstraction;

import org.brlnsreb.utils.Cooldown;
import org.powernukkitx.Player;

public abstract class FormAbstract {

    protected static final Cooldown openingCooldown = Cooldown.seconds(0.5);

    protected static boolean checkCooldown(Player player) {
        boolean check = openingCooldown.checkOrAdd(player.getUniqueId());
        if (check) player.getInventory().setHeldItemIndex(0);
        
        return check;
    }

}
