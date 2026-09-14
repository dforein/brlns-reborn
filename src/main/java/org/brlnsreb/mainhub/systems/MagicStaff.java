package org.brlnsreb.mainhub.systems;

import org.brlnsreb.BrlnsReb;
import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.utils.Cooldown;
import org.brlnsreb.utils.SoundUtil;
import org.brlnsreb.utils.config.Configs;
import org.brlnsreb.utils.messages.ChatMsgs;
import org.powernukkitx.entity.Entity;
import org.powernukkitx.entity.projectile.EntitySnowball;
import org.powernukkitx.level.Position;
import org.powernukkitx.level.Sound;
import org.powernukkitx.level.particle.LavaParticle;
import org.powernukkitx.math.Vector3;
import org.powernukkitx.utils.Config;

public class MagicStaff {

    private static Cooldown cooldown;
    private static double snowballSpeed;

    public static void init() {
        onConfigReload();
    }

    public static void onConfigReload() {
        Config config = Configs.getGlobalConfig();
        cooldown = Cooldown.seconds(config.getInt("lobby.items.magic-staff.cooldown"));
        snowballSpeed = config.getDouble("lobby.items.magic-staff.snowball-speed");
    }

    public static void doMagic(CustomPlayer player) {
        if (!cooldown.check(player.getUniqueId())) {
            player.sendMessage(ChatMsgs.ERROR_PFX + "Slow down!");
            player.sendMessage(ChatMsgs.ERROR_PFX + "Recharging power... wait §e" 
                    + cooldown.getSecondsRemaining(player.getUniqueId()) + " §cseconds");
            return;
        }

        Vector3 startVect = player.getDirectionVector().divide(2.0).add(
            player.x,
            player.y + player.getEyeHeight(),
            player.z
        );

        EntitySnowball snowball = new EntitySnowball(
            player.getChunk(), 
            Entity.getDefaultNBT(startVect)
        );

        snowball.shootingEntity = player;
        Vector3 direction = player.getDirectionVector();
        snowball.setMotion(direction.multiply(snowballSpeed));

        snowball.spawnToAll();

        SoundUtil.sendSoundTo(player, Sound.MOB_GHAST_FIREBALL.getSound());
    }

    public static void onSnowballHit(Position pos) {
        for (int i = 0; i < 20; i++) {
            BrlnsReb.getScheduler().scheduleDelayedTask(BrlnsReb.instance,
                () -> pos.level.addParticle(new LavaParticle(pos)), 
            i);
        }
    }
    
}
