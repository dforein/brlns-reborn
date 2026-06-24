package org.brlnsreb.mm.listeners;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockButton;
import cn.nukkit.block.BlockCake;
import cn.nukkit.block.BlockCandleCake;
import cn.nukkit.block.BlockDoor;
import cn.nukkit.block.BlockFarmland;
import cn.nukkit.block.BlockFenceGate;
import cn.nukkit.block.BlockLever;
import cn.nukkit.block.BlockPressurePlateBase;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.ItemFrameUseEvent;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.event.player.PlayerInteractEvent.Action;
import cn.nukkit.event.player.PlayerInteractEntityEvent;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemBlazeRod;
import cn.nukkit.item.ItemCompass;
import cn.nukkit.item.ItemGoldenHoe;
import cn.nukkit.item.ItemIronSword;
import cn.nukkit.item.ItemNetherStar;
import cn.nukkit.item.ItemYellowDye;
import cn.nukkit.level.Sound;
import cn.nukkit.entity.item.EntityArmorStand;
import cn.nukkit.entity.effect.Effect;
import cn.nukkit.entity.effect.EffectType;
import cn.nukkit.utils.TextFormat;

import org.brlnsreb.BrlnsReb;
import org.brlnsreb.core.minigame.match.GameStateType;
import org.brlnsreb.mm.MurderMysteryGame;
import org.brlnsreb.mm.config.MMConfig;
import org.brlnsreb.mm.roles.GamePlayer;
import org.brlnsreb.mm.roles.MMRole;
import org.brlnsreb.mm.systems.ItemManager;
import org.brlnsreb.mm.ui.BossBarSystem;

import java.util.*;

public class MMPlayerInteractListener implements Listener {
    
    private final MurderMysteryGame game;
    private static final HashSet<String> INTERACT_BLOCKS = new HashSet<>(Arrays.asList(
            Block.CHEST, Block.TRAPPED_CHEST, Block.ENDER_CHEST, Block.COPPER_CHEST,
            Block.FURNACE, Block.BLAST_FURNACE, Block.SMOKER,
            Block.HOPPER, 
            Block.BARREL, 
            Block.BEACON, 
            Block.BREWING_STAND,
            Block.ANVIL, Block.CHIPPED_ANVIL, Block.DAMAGED_ANVIL,
            Block.CARTOGRAPHY_TABLE, 
            Block.CRAFTING_TABLE, 
            Block.CRAFTER,
            Block.DISPENSER, Block.DROPPER, 
            Block.ENCHANTING_TABLE,
            Block.GRINDSTONE, 
            Block.LECTERN,
            Block.LOOM,
            Block.WALL_SIGN, Block.STANDING_SIGN,
            Block.SMITHING_TABLE,
            Block.STONECUTTER,
            Block.DRAGON_EGG
        )
    );
    
    public MMPlayerInteractListener(MurderMysteryGame game) {
        this.game = game;
    }
    
    @EventHandler
    public void onItemFrameInteract(ItemFrameUseEvent event) {

        Player player = event.getPlayer();

        if (player != null && (player.getGamemode() == Player.ADVENTURE || !player.isOp())) {
            event.setCancelled(true);
        }

    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();

        if (event.getEntity() instanceof EntityArmorStand) {
            if (player != null && (player.getGamemode() == Player.ADVENTURE || !player.isOp())) {
                event.setCancelled(true);
            }
        }

        GamePlayer gp = game.getRoleManager().getGamePlayer(player);
        if (gp == null) return;

        Item item = event.getItem();
        if (item == null) return;
        
        if (handleItemInteraction(player, item, gp)) event.setCancelled();

    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {

        Block block = event.getBlock();
    
        if (block != null) {
            if (INTERACT_BLOCKS.contains(block.getId()) 
                || block instanceof BlockCake               //TODO: remove later (cake)
                || block instanceof BlockCandleCake) {      //TODO: remove later (cake)

                event.setCancelled(true);
                return;
            }

            if (event.getAction() == Action.PHYSICAL) {
                if (block instanceof BlockFarmland || block instanceof BlockPressurePlateBase) {

                    event.setCancelled(true);
                    return;
                }
            }
        }

        Player player = event.getPlayer();
        GamePlayer gp = game.getRoleManager().getGamePlayer(player);
        if (gp == null) return;

        if (block != null) {
            if (gp.getRole() == MMRole.SPECTATOR) {
                if (block instanceof BlockDoor
                    || block instanceof BlockFenceGate
                    || block instanceof BlockButton
                    || block instanceof BlockLever) {
                    
                    event.setCancelled(true);
                    return;
                }
            }
        }

        Item item = event.getItem();
        if (item == null) return;

        if ((item instanceof ItemIronSword || item instanceof ItemGoldenHoe) 
            && event.getAction() != Action.RIGHT_CLICK_AIR) return;
        
        if (handleItemInteraction(player, item, gp)) event.setCancelled();

    }

    private boolean handleItemInteraction(Player player, Item item, GamePlayer gp) {
        if (game.getState() == GameStateType.WAITING_LOBBY || game.getState() == GameStateType.LOBBY_COUNTDOWN) {
            if (game.getPlayers().contains(player)) {
                //game poll
                if (item instanceof ItemNetherStar) {
                    String customName = item.getCustomName();
                    if (customName != null && customName.contains("Game Poll")) {
                        game.getVotingMenu().openVotingMenu(player);
                        return true;
                    }
                }
            }

            return false;
        }
        
        if (item instanceof ItemCompass && gp.getRole() == MMRole.SPECTATOR) {
            game.getSpectatorMenu().openTeleportMenu(player);
            return true;
        }

        if (!gp.isAlive()) return false;
        MMConfig config = game.getConfig();

        if (item instanceof ItemGoldenHoe) {
            handleSheriffShoot(player, gp, config);
            return true;
        }

        else if (item instanceof ItemIronSword) {
            handleMurdererThrow(player, gp, config);
            return true;
        }
        
        else if (item instanceof ItemBlazeRod) {
            handleFlash(player, gp, config);
            return true;
        }
        
        else if (item instanceof ItemYellowDye) {
            handleBecomeSheriff(player, gp, config);
            return true;
        }

        return false;
    }
    
    private void handleSheriffShoot(Player shooter, GamePlayer gp, MMConfig config) {
        if (gp.getRole() != MMRole.SHERIFF) return;
        
        String cooldownKey = "mmsht:" + shooter.getName();
        if (!game.getCooldowns().canUse(cooldownKey, config.getShootCooldown())) {
            return;
        }
        
        Player target = game.getRaycast().shoot(shooter);
        shooter.getLevel().addSound(shooter, Sound.RANDOM_FIZZ, 0.8f, 0.9f);
        
        if (game.getState() == GameStateType.ENDING) return;


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
        double ticks = cooldown * 20;
        
        for (int i = 0; i <= ticks; i++) {
            final int tick = i;
            
            player.getServer().getScheduler().scheduleDelayedTask(() -> {
                int progress = (int) (tick / ticks * 100.0);
                player.setExperience(progress, 0);
            }, i);
        }
    }
    
    private void handleMurdererThrow(Player murderer, GamePlayer gp, MMConfig config) {
        if (gp.getRole() != MMRole.MURDERER) return;
        
        String cooldownKey = "mmthrw:" + murderer.getName();
        if (!game.getCooldowns().canUse(cooldownKey, config.getSwordThrowCooldown())) {
            murderer.sendMessage(TextFormat.colorize(config.getMessage("sword-cooldown")));
            return;
        }
        
        game.getProjectile().throwSword(murderer);
        murderer.getLevel().addSound(murderer, Sound.RANDOM_BOW, 0.8f, 0.5f);
        
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
                    blindness.setVisible(false);
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

        if (game.getState() == GameStateType.ENDING) return;


        BrlnsReb plugin = game.getPlugin();
        plugin.getServer().getScheduler().scheduleDelayedTask(plugin, () -> {
            for (Player p : game.getPlayers()) {
                p.sendMessage(TextFormat.colorize(config.getMessage("lights-out-over")));
            }
        }, config.getBlindnessDuration() * 20);
    }
    
    private void handleBecomeSheriff(Player player, GamePlayer gp, MMConfig config) {
        if (gp == null || !gp.isAlive() || gp.getRole() != MMRole.INNOCENT) return;
        if (!game.getRoleManager().isSheriffDead()) return;
        
        if (!gp.canBecomeSheriff(config.getGoldForGun())) {
            String message = config.getMessage("not-enough-gold");
            player.sendMessage(TextFormat.colorize(
                message.replace(
                    "{gold}", 
                    String.valueOf(gp.getGoldCollected())
                )
            ));
            return;
        }
        
        game.getRoleManager().setSheriff(gp);
        
        for (Player p : game.getPlayers()) {
            clearItem(p, Item.YELLOW_DYE);
        }

        game.getDeath().cleanupSheriffHoe(game.getArena().getLevel());

        ItemManager.giveSheriffItems(player, config.getSheriffHoeName());

        BossBarSystem bossBar = game.getBossBar();
        bossBar.updateExp(player, game.getRoleManager().getGamePlayer(player).getExpEarned());

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

            if (itemPointed.getId().equals(itemId)) {
                player.getInventory().clear(i);
                break;
            }
        }
    }

}