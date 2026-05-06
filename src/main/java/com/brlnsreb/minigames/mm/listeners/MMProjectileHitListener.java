package com.brlnsreb.minigames.mm.listeners;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.entity.ProjectileHitEvent;
import cn.nukkit.utils.TextFormat;

import com.brlnsreb.minigames.core.minigame.GameStateType;
import com.brlnsreb.minigames.mm.MurderMysteryGame;
import com.brlnsreb.minigames.mm.config.MMConfig;
import com.brlnsreb.minigames.mm.entities.ThrownSwordEntity;
import com.brlnsreb.minigames.mm.roles.GamePlayer;
import com.brlnsreb.minigames.mm.roles.MMRole;

public class MMProjectileHitListener implements Listener {
    
    private final MurderMysteryGame game;
    
    public MMProjectileHitListener(MurderMysteryGame game) {
        this.game = game;
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onProjectileHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof ThrownSwordEntity)) return;

        ThrownSwordEntity thrownSword = (ThrownSwordEntity) event.getEntity();
        Entity hit = event.getMovingObjectPosition().entityHit;

        if (!(hit instanceof Player)) return;
        if (game.getState() != GameStateType.IN_GAME) return;

        Player victim = (Player) hit;
        GamePlayer victimGp = game.getRoleManager().getGamePlayer(victim);
        MMConfig config = game.getConfig();
        
        if (victimGp == null || !victimGp.isAlive()) return;
        
        //murderer and game handling
        Player murderer = (Player) thrownSword.shootingEntity;
        GamePlayer murdererGp = game.getRoleManager().getGamePlayer(murderer);

        if (game.isFirstKill()) {
            murderer.sendMessage(TextFormat.colorize(config.getMessage("murderer-warning")));
        }

        String message = config.getMessage("killed").replace("{killer}", config.getMessageNoPrefix("murderer"));

        if (murdererGp != null) {
            if (victimGp.getRole() == MMRole.SHERIFF) {
                murdererGp.addExp(config.getExpSheriffKilled());

                murderer.sendMessage(TextFormat.colorize(config.getMessage("murderer-kill-sheriff")));

                message = message.replace("{killed}", config.getMessageNoPrefix("sheriff-lowercase"));
                for (Player p : game.getPlayers()) {
                    p.sendMessage(TextFormat.colorize(message));
                    p.sendMessage(TextFormat.colorize(config.getMessage("sheriff-gun-dropped")));
                    p.sendMessage(TextFormat.colorize(config.getMessage("sheriff-dead-instructions")));
                }

            } else if (victimGp.getRole() == MMRole.INNOCENT) {
                murdererGp.addExp(config.getExpPerKill());

                message = message.replace("{killed}", victim.getName());
                for (Player p : game.getPlayers()) {
                    p.sendMessage(TextFormat.colorize(message));
                }

                message = config.getMessage("murderer-kill").replace("{player}", victim.getName());
                murderer.sendMessage(TextFormat.colorize(message));
            }
        }

        //victim handling
        victimGp.setAlive(false);

        if (victimGp.getRole() == MMRole.SHERIFF) {
            game.getDeath().kill(victim, true);
            game.getRoleManager().checkGoldRewards(game);
        } else if (victimGp.getRole() == MMRole.INNOCENT) {
            game.getDeath().kill(victim, false);
        }
    
        game.checkWinCondition();
    }
}