package com.brlnsreb.minigames.mm.listeners;

import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemIronSword;
import cn.nukkit.utils.TextFormat;

import com.brlnsreb.minigames.core.minigame.GameState;
import com.brlnsreb.minigames.mm.MurderMysteryGame;
import com.brlnsreb.minigames.mm.config.MMConfig;
import com.brlnsreb.minigames.mm.roles.GamePlayer;
import com.brlnsreb.minigames.mm.roles.MMRole;

public class MMPlayerAttackListener implements Listener {
    
    private final MurderMysteryGame game;
    
    public MMPlayerAttackListener(MurderMysteryGame game) {
        this.game = game;
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onDamage(EntityDamageEvent event) {

        if (game.getState() != GameState.IN_GAME) return;

        if (!(event.getEntity() instanceof Player)) return;
        
        Player victim = (Player) event.getEntity();
        GamePlayer gp = game.getRoleManager().getGamePlayer(victim);

        event.setCancelled(true);
        
        if (gp == null || !gp.isAlive()) return;
        
        if (event instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) event;
            
            if (!(e.getDamager() instanceof Player)) return;
            
            Player attacker = (Player) e.getDamager();
            GamePlayer attackerGp = game.getRoleManager().getGamePlayer(attacker);
            
            if (attackerGp == null || !attackerGp.isAlive()) return;
            
            Item weapon = attacker.getInventory().getItemInMainHand();
            
            if (weapon instanceof ItemIronSword && attackerGp.getRole() == MMRole.MURDERER) {

                MMConfig config = game.getConfig();

                if (game.isFirstKill()) {
                    attacker.sendMessage(TextFormat.colorize(config.getMessage("murderer-warning")));
                }

                gp.setAlive(false);
                game.getDeath().kill(victim, gp.getRole() == MMRole.SHERIFF);

                String message = config.getMessage("killed").replace("{killer}", config.getMessageNoPrefix("murderer"));
                
                if (gp.getRole() == MMRole.SHERIFF) {
                    attackerGp.addExp(config.getExpSheriffKilled());

                    attacker.sendMessage(TextFormat.colorize(config.getMessage("murderer-kill-sheriff")));

                    message = message.replace("{killed}", config.getMessageNoPrefix("sheriff-lowercase"));
                    for (Player p : game.getPlayers()) {
                        p.sendMessage(TextFormat.colorize(message));
                        p.sendMessage(TextFormat.colorize(config.getMessage("sheriff-gun-dropped")));
                        p.sendMessage(TextFormat.colorize(config.getMessage("sheriff-dead-instructions")));
                    }
                    
                    game.getRoleManager().checkGoldRewards(game);
                } else if (gp.getRole() == MMRole.INNOCENT) {
                    attackerGp.addExp(config.getExpPerKill());

                    message = message.replace("{killed}", victim.getName());
                    for (Player p : game.getPlayers()) {
                        p.sendMessage(TextFormat.colorize(message));
                    }

                    message = config.getMessage("murderer-kill").replace("{player}", victim.getName());
                    attacker.sendMessage(TextFormat.colorize(message));
                }
                
                game.checkWinCondition();
            }
        }
    }
}