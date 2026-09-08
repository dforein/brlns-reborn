package org.brlnsreb.utils.abstraction;

import org.brlnsreb.utils.Cooldown;
import org.powernukkitx.Player;

public abstract class MenuAbstract {

    protected static final Cooldown openingCooldown = Cooldown.seconds(0.5);

    protected static boolean checkCooldown(Player player) {
        boolean check = openingCooldown.check(player.getUniqueId());
        if (check) player.getInventory().setHeldItemIndex(0);
        
        return check;
    }

}
