package com.brlnsreb.minigames.mm.systems;

import cn.nukkit.Player;
import cn.nukkit.form.window.SimpleForm;
import cn.nukkit.utils.TextFormat;
import com.brlnsreb.minigames.mm.MurderMysteryGame;
import com.brlnsreb.minigames.mm.roles.GamePlayer;
import com.brlnsreb.minigames.mm.roles.MMRole;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpectatorMenu {
    
    private final MurderMysteryGame game;
    private final Map<String, List<Player>> pendingMenus;
    
    public SpectatorMenu(MurderMysteryGame game) {
        this.game = game;
        this.pendingMenus = new HashMap<>();
    }
    
    public void openTeleportMenu(Player spectator) {
        SimpleForm menu = new SimpleForm(
            TextFormat.colorize(game.getConfig().getSpectatorItemName()),
            TextFormat.colorize("Select a player to teleport")
        );
        
        List<Player> alivePlayers = new ArrayList<>();
        
        for (GamePlayer gp : game.getRoleManager().getAllPlayers()) {
            if (gp.isAlive() && gp.getRole() != MMRole.SPECTATOR) {
                Player player = gp.getPlayer();
                String displayName = player.getName();
                
                switch (gp.getRole()) {
                    case SHERIFF:
                        displayName += TextFormat.colorize(" &7(&l&9SHERIFF&r&7)");
                        break;

                    case MURDERER:
                        displayName += TextFormat.colorize(" &7(&l&cMURDERER&r&7)");
                        break;

                    case INNOCENT:
                        displayName += TextFormat.colorize(" &a(&l&cINNOCENT&r&7)");
                        break;
                    
                    default:
                        break;
                }
                
                menu.addButton(displayName);
                alivePlayers.add(player);
            }
        }
        
        if (alivePlayers.isEmpty()) {
            spectator.sendMessage(TextFormat.RED + "No players alive to teleport to!");
            return;
        }
        
        pendingMenus.put(spectator.getName(), alivePlayers);
        menu.send(spectator);
    }
    
    public void handleResponse(Player spectator, int buttonId) {
        List<Player> alivePlayers = pendingMenus.remove(spectator.getName());
        
        if (alivePlayers == null) return;
        
        if (buttonId >= 0 && buttonId < alivePlayers.size()) {
            Player target = alivePlayers.get(buttonId);
            
            if (target != null && target.isOnline()) {
                spectator.teleport(target.getLocation());

                String message = game.getConfig().getMessage("teleported-to")
                                                    .replace("{player}", target.getName());
                spectator.sendMessage(TextFormat.colorize(message));
            } else {
                spectator.sendMessage(TextFormat.colorize(game.getConfig().getMessage("player-not-available")));
            }
        }
    }
}