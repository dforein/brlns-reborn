package org.brlnsreb.minigames.mm.listeners;

import org.powernukkitx.Player;
import org.powernukkitx.entity.Entity;
import org.powernukkitx.event.EventHandler;
import org.powernukkitx.event.EventPriority;
import org.powernukkitx.event.Listener;
import org.powernukkitx.event.entity.ProjectileHitEvent;
import org.powernukkitx.utils.TextFormat;

import org.brlnsreb.core.minigame.match.GameStateType;
import org.brlnsreb.minigames.mm.MurderMysteryGame;
import org.brlnsreb.minigames.mm.config.MMConfig;
import org.brlnsreb.minigames.mm.match.game.entities.ThrownSwordEntity;
import org.brlnsreb.minigames.mm.match.game.gamedata.MMRole;
import org.brlnsreb.minigames.mm.roles.GamePlayer;

public class MMProjectileHitListener implements Listener {
    
    private final MurderMysteryGame game;
    
    public MMProjectileHitListener(MurderMysteryGame game) {
        this.game = game;
    }
    
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