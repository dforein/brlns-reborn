package com.brlnsreb.minigames.mm.listeners;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.projectile.EntitySnowball;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.entity.ProjectileHitEvent;
import cn.nukkit.utils.TextFormat;

import com.brlnsreb.minigames.core.GameState;
import com.brlnsreb.minigames.mm.MurderMysteryGame;
import com.brlnsreb.minigames.mm.config.MMConfig;
import com.brlnsreb.minigames.mm.roles.GamePlayer;
import com.brlnsreb.minigames.mm.roles.MMRole;

public class MMProjectileHitListener implements Listener {
    
    private final MurderMysteryGame game;
    
    public MMProjectileHitListener(MurderMysteryGame game) {
        this.game = game;
    }
    
    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (game.getState() != GameState.IN_GAME && game.getState() != GameState.ENDING) return;
        
        Entity entity = event.getEntity();
        if (!(entity instanceof EntitySnowball)) return;
        
        EntitySnowball snowball = (EntitySnowball) entity;
        
        if (!snowball.namedTag.exist("mm_projectile")) return;
        if (!snowball.namedTag.getString("mm_projectile").equals("sword")) return;

        if (game.getState() != GameState.ENDING) return;
        
        Entity hit = event.getMovingObjectPosition().entityHit;
        if (hit instanceof Player) {
            Player victim = (Player) hit;
            GamePlayer gp = game.getRoleManager().getGamePlayer(victim);
            MMConfig config = game.getConfig();
            
            if (gp != null && gp.isAlive()) {
                gp.setAlive(false);

                if (gp.getRole() == MMRole.SHERIFF) {
                    game.getDeath().kill(victim, true);
                    game.getRoleManager().checkGoldRewards(game);
                } else if (gp.getRole() == MMRole.INNOCENT) {
                    game.getDeath().kill(victim, false);
                }
                
                if (snowball.shootingEntity instanceof Player) {
                    Player murderer = (Player) snowball.shootingEntity;
                    GamePlayer murdererGp = game.getRoleManager().getGamePlayer(murderer);

                    if (game.isFirstKill()) {
                        murderer.sendMessage(TextFormat.colorize(config.getMessage("murderer-warning")));
                    }

                    String message = config.getMessage("killed").replace("{killer}", config.getMessageNoPrefix("murderer"));

                    if (murdererGp != null) {
                        if (gp.getRole() == MMRole.SHERIFF) {
                            murdererGp.addExp(config.getExpSheriffKilled());

                            murderer.sendMessage(TextFormat.colorize(config.getMessage("murderer-kill-sheriff")));

                            message = message.replace("{killed}", config.getMessageNoPrefix("sheriff-lowercase"));
                            for (Player p : game.getPlayers()) {
                                p.sendMessage(TextFormat.colorize(message));
                                p.sendMessage(TextFormat.colorize(config.getMessage("sheriff-gun-dropped")));
                                p.sendMessage(TextFormat.colorize(config.getMessage("sheriff-dead-instructions")));
                            }
                        } else if (gp.getRole() == MMRole.INNOCENT) {
                            murdererGp.addExp(config.getExpPerKill());

                            message = message.replace("{killed}", victim.getName());
                            for (Player p : game.getPlayers()) {
                                p.sendMessage(TextFormat.colorize(message));
                            }

                            message = config.getMessage("murderer-kill").replace("{player}", victim.getName());
                            murderer.sendMessage(TextFormat.colorize(message));
                        }
                    }
                }
            
                game.checkWinCondition();
            }
        }
    }
}