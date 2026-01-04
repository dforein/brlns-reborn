package com.brlnsreb.minigames.mm.listeners;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.item.Item;
import cn.nukkit.entity.effect.Effect;
import cn.nukkit.entity.effect.EffectType;
import cn.nukkit.utils.TextFormat;
import com.brlnsreb.minigames.MinigameCore;
import com.brlnsreb.minigames.core.GameState;
import com.brlnsreb.minigames.mm.MurderMysteryGame;
import com.brlnsreb.minigames.mm.config.MMConfig;
import com.brlnsreb.minigames.mm.items.ItemManager;
import com.brlnsreb.minigames.mm.roles.GamePlayer;
import com.brlnsreb.minigames.mm.roles.MMRole;
import com.brlnsreb.minigames.mm.systems.BossBarSystem;
import com.brlnsreb.minigames.mm.systems.BookManager;

public class MMPlayerInteractListener implements Listener {
    
    private final MurderMysteryGame game;
    
    public MMPlayerInteractListener(MurderMysteryGame game) {
        this.game = game;
    }
    
    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        GamePlayer gp = game.getRoleManager().getGamePlayer(player);
        if (gp == null) return;

        if (game.getState() == GameState.LOBBY || game.getState() == GameState.COUNTDOWN) {
            if (game.getPlayers().contains(player)) {
                Item item = event.getItem();
                
                //rules book
                if (item.getId() == Item.BOOK) {
                    String customName = item.getCustomName();
                    if (customName != null && customName.contains("Rules")) {
                        event.setCancelled(true);
                        BookManager.openRulesBook(player, game.getPlugin());
                        return;
                    }
                }
                
                //game poll
                else if (item.getId() == Item.NETHER_STAR) {
                    String customName = item.getCustomName();
                    if (customName != null && customName.contains("Game Poll")) {
                        event.setCancelled(true);
                        game.getVotingMenu().openVotingMenu(player);
                        return;
                    }
                }
            }
        }
        
        //spectator interaction with things
        if (gp.getRole() == MMRole.SPECTATOR) {
            Block block = event.getBlock();
            if (block != null &&
                (block.getId() == Block.FENCE_GATE || 
                block.getId() == Block.TRAPDOOR || 
                block.getId() == Block.WOODEN_DOOR || 
                block.getId() == Block.IRON_DOOR ||
                block.getId() == Block.LEVER ||
                block.getId() == Block.STONE_BUTTON ||
                block.getId() == Block.WOODEN_BUTTON)) {
                
                event.setCancelled(true);
                return;
            }

            /*
            if (event.getItem().getId() == Item.NETHER_STAR && gp.getRole() == MMRole.SPECTATOR) {
                game.getSpectatorMenu().openTeleportMenu(player);
                event.setCancelled(true);
                return;
            }*/
        }
        
        if (game.getState() != GameState.IN_GAME && game.getState() != GameState.ENDING) return;
        if (!gp.isAlive()) return;

        Item item = event.getItem();
        MMConfig config = game.getConfig();
        
        if (item.getId() == Item.GOLDEN_HOE) {
            handleSheriffShoot(player, gp, config);
            event.setCancelled(true);
        }
        
        else if (item.getId() == Item.IRON_SWORD && event.getAction() == PlayerInteractEvent.Action.RIGHT_CLICK_AIR) {
            handleMurdererThrow(player, gp, config);
            event.setCancelled(true);
        }
        
        else if (item.getId() == Item.BLAZE_ROD) {
            handleFlash(player, gp, config);
            event.setCancelled(true);
        }
        
        else if (item.getId() == Item.DYE && item.getDamage() == 11) {
            handleBecomeSheriff(player, gp, config);
            event.setCancelled(true);
        }
    }
    
    private void handleSheriffShoot(Player shooter, GamePlayer gp, MMConfig config) {
        if (gp.getRole() != MMRole.SHERIFF) return;
        
        String cooldownKey = "sheriff_shoot_" + shooter.getName();
        if (!game.getCooldowns().canUse(cooldownKey, config.getShootCooldown())) {
            return;
        }
        
        Player target = game.getRaycast().shoot(shooter);
        
        if (game.getState() == GameState.ENDING) return;


        if (target != null) {
            GamePlayer targetGp = game.getRoleManager().getGamePlayer(target);
            
            if (targetGp != null && targetGp.isAlive()) {
                targetGp.setAlive(false);
                game.getDeath().kill(target, false);
                
                if (targetGp.getRole() == MMRole.INNOCENT && config.isFriendlyFireDeath()) {
                    gp.setAlive(false);
                    game.getDeath().kill(shooter, true);
                    
                    for (Player p : game.getPlayers()) {
                        p.sendMessage(TextFormat.colorize(config.getMessage("sheriff-friendly-fire")));
                        p.sendMessage(TextFormat.colorize(config.getMessage("sheriff-gun-dropped")));
                        p.sendMessage(TextFormat.colorize(config.getMessage("sheriff-dead-instructions")));
                    }
                } else if (targetGp.getRole() == MMRole.MURDERER) {
                    String message = config.getMessage("killed")
                                        .replace("{killer}", config.getMessageNoPrefix("sheriff"))
                                        .replace("{killed}", config.getMessageNoPrefix("murderer"));
                    
                    for (Player p : game.getPlayers()) {
                        p.sendMessage(TextFormat.colorize(message));
                    }

                    shooter.sendMessage(TextFormat.colorize(config.getMessage("sheriff-kill")));
                }
                
                game.checkWinCondition();
            }
        }
        
        game.getCooldowns().recordUse(cooldownKey);
        
        shooter.setExperience(0, 0);
        startXpRecharge(shooter, config.getShootCooldown());
    }
    
    private void startXpRecharge(Player player, double cooldown) {
        int ticks = (int)(cooldown * 20);
        
        for (int i = 0; i <= ticks; i++) {
            final int tick = i;
            
            player.getServer().getScheduler().scheduleDelayedTask(() -> {
                int progress = tick / ticks;
                player.setExperience(0, progress);
            }, i);
        }
    }
    
    private void handleMurdererThrow(Player murderer, GamePlayer gp, MMConfig config) {
        if (gp.getRole() != MMRole.MURDERER) return;
        
        String cooldownKey = "sword_throw_" + murderer.getName();
        if (!game.getCooldowns().canUse(cooldownKey, config.getSwordThrowCooldown())) {
            murderer.sendMessage(TextFormat.colorize(config.getMessage("sword-cooldown")));
            return;
        }
        
        game.getProjectile().throwSword(murderer);
        game.getCooldowns().recordUse(cooldownKey);
    }
    
    private void handleFlash(Player murderer, GamePlayer gp, MMConfig config) {
        if (gp.getRole() != MMRole.MURDERER) return;
        if (gp.hasUsedFlash()) return;
        
        int duration = config.getBlindnessDuration();
        
        for (Player p : game.getPlayers()) {
            if (!p.equals(murderer) && p.isAlive()) {
                GamePlayer targetGp = game.getRoleManager().getGamePlayer(p);
                if (targetGp != null && targetGp.isAlive()) {
                    Effect blindness = Effect.get(EffectType.BLINDNESS);
                    blindness.setDuration(duration * 20);
                    blindness.setAmplifier(0);
                    p.addEffect(blindness);
                }

                p.sendMessage(TextFormat.colorize(
                    config.getMessage("lights-out-others").replace("{seconds}", Integer.toString(config.getBlindnessDuration()))
                ));
            } else {
                p.sendMessage(TextFormat.colorize(
                    config.getMessage("lights-out-murderer").replace("{seconds}", Integer.toString(config.getBlindnessDuration()))
                ));
            }
        }
        
        clearItem(murderer, Item.BLAZE_ROD);
        gp.setUsedFlash(true);

        if (game.getState() == GameState.ENDING) return;


        MinigameCore plugin = game.getPlugin();
        plugin.getServer().getScheduler().scheduleDelayedTask(plugin, () -> {
            for (Player p : game.getPlayers()) {
                p.sendMessage(TextFormat.colorize(config.getMessage("lights-out-over")));
            }
        }, config.getBlindnessDuration() * 20);
    }
    
    private void handleBecomeSheriff(Player player, GamePlayer gp, MMConfig config) {
        if (gp == null || !gp.isAlive() || gp.getRole() != MMRole.INNOCENT) return;
        if (!game.getRoleManager().isSheriffDead()) return;
        if (!gp.canBecomeSheriff(config.getGoldForGun())) return;
        
        game.getRoleManager().setSheriff(gp);
        
        for (Player p : game.getPlayers()) {
            clearItem(p, Item.DYE, 11);
        }

        ItemManager.giveSheriffItems(player, config.getSheriffHoeName());

        BossBarSystem bossBar = game.getBossBar();
        bossBar.hide(player);
        bossBar.showExp(player, game.getRoleManager().getGamePlayer(player).getExpEarned());

        player.sendTitle(
            TextFormat.colorize(config.getMessageNoPrefix("sheriff-title")),
            TextFormat.colorize(config.getMessageNoPrefix("sheriff-subtitle")),
            10, 60, 10
        );
        for (Player p : game.getPlayers()) {
            p.sendMessage(TextFormat.colorize(config.getMessage("new-sheriff-chosen")));
        }
    }

    private void clearItem(Player player, String itemId) {
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            Item itemPointed = player.getInventory().getItem(i);

            if (itemPointed.getId() == itemId) {
                player.getInventory().clear(i);
                break;
            }
        }
    }

    private void clearItem(Player player, String itemId, int meta) {
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            Item itemPointed = player.getInventory().getItem(i);

            if (itemPointed.getId() == itemId && itemPointed.getDamage() == meta) {
                player.getInventory().clear(i);
                break;
            }
        }
    }
}