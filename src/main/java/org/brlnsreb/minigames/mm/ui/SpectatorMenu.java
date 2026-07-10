package org.brlnsreb.minigames.mm.ui;

import org.powernukkitx.Player;
import org.powernukkitx.Server;
import org.powernukkitx.form.window.SimpleForm;
import org.powernukkitx.scheduler.TaskHandler;
import org.powernukkitx.utils.TextFormat;

import org.brlnsreb.core.minigame.match.GameStateType;
import org.brlnsreb.minigames.mm.MurderMysteryGame;
import org.brlnsreb.minigames.mm.roles.GamePlayer;
import org.brlnsreb.minigames.mm.roles.MMRole;
import org.brlnsreb.utils.abstraction.MenuAbstract;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SpectatorMenu extends MenuAbstract {
    
    private final MurderMysteryGame game;
    private final Map<UUID, List<Player>> pendingMenus;
    private final Map<UUID, TaskHandler> handlers;
    
    public SpectatorMenu(MurderMysteryGame game) {
        this.game = game;
        this.pendingMenus = new ConcurrentHashMap<>();
        this.handlers = new ConcurrentHashMap<>();
    }
    
    public void openTeleportMenu(Player spectator) {
        checkCooldown(spectator);

        SimpleForm menu = new SimpleForm("Spectate player");

        //TODO: add "Random Player" first option
        
        List<Player> alivePlayers = new ArrayList<>();
        
        for (GamePlayer gp : game.getRoleManager().getAllPlayers()) {
            if (gp.isAlive() && gp.getRole() != MMRole.SPECTATOR) {
                Player player = gp.getPlayer();
                String displayName = player.getName();
                
                switch (gp.getRole()) {
                    case SHERIFF:
                        displayName += TextFormat.colorize(" &8(&l&9SHERIFF&r&8)");
                        break;

                    case MURDERER:
                        displayName += TextFormat.colorize(" &8(&l&cMURDERER&r&8)");
                        break;

                    case INNOCENT:
                        displayName += TextFormat.colorize(" &8(&l&aINNOCENT&r&8)");
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
        
        pendingMenus.put(spectator.getUniqueId(), alivePlayers);
        menu.send(spectator);
    }
    
    public void handleResponse(Player spectator, int buttonId) {
        List<Player> alivePlayers = pendingMenus.remove(spectator.getUniqueId());
        
        if (alivePlayers == null) return;
        
        if (buttonId >= 0 && buttonId < alivePlayers.size()) {
            Player target = alivePlayers.get(buttonId);
            
            if (target != null && target.isOnline()) {
                spectator.teleport(target.getLocation());

                String message = game.getConfig().getMessage("teleported-to").replace("{player}", target.getName());
                spectator.sendMessage(TextFormat.colorize(message));

                TaskHandler oldHandler = handlers.remove(spectator.getUniqueId());
                if (oldHandler != null) oldHandler.cancel();

                final TaskHandler[] currHandler = new TaskHandler[1];
                currHandler[0] = Server.getInstance().getScheduler().scheduleRepeatingTask(() -> {
                    if ((target == null || !target.isOnline() || !game.getRoleManager().getGamePlayer(target).isAlive() || !game.getPlayers().contains(target)
                        || spectator == null || !spectator.isOnline() || !game.getPlayers().contains(spectator)) 
                        || !(game.getState() == GameStateType.IN_GAME || game.getState() == GameStateType.ENDING)) {

                        if (currHandler[0] != null) currHandler[0].cancel();
                        handlers.remove(spectator.getUniqueId());
                        return;
                    }

                    String actionBarMsg = "&l&aTarget: &e"+ target.getName() +" &aDistance: &d%.2fm";
                    spectator.sendActionBar(TextFormat.colorize(
                        actionBarMsg.formatted(spectator.distance(target))
                    ));
                }, 20);

                handlers.put(spectator.getUniqueId(), currHandler[0]);

            } else {
                spectator.sendMessage(TextFormat.colorize(game.getConfig().getMessage("player-not-available")));
            }
        }
    }
}