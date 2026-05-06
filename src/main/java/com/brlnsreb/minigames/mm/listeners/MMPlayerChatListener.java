package com.brlnsreb.minigames.mm.listeners;

import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerChatEvent;
import cn.nukkit.event.player.PlayerCommandPreprocessEvent;
import cn.nukkit.utils.TextFormat;

import com.brlnsreb.minigames.core.minigame.GameStateType;
import com.brlnsreb.minigames.mm.MurderMysteryGame;
import com.brlnsreb.minigames.mm.roles.GamePlayer;
import com.brlnsreb.minigames.mm.roles.MMRole;

public class MMPlayerChatListener implements Listener {
    
    private final MurderMysteryGame game;
    private String[] blockedCommands = {
        "say",
        "whisper",
        "tell",
        "msg",
        "me"
    };
    
    public MMPlayerChatListener(MurderMysteryGame game) {
        this.game = game;
    }
    
    @EventHandler
    public void onChat(PlayerChatEvent event) {

        if (game.getState() != GameStateType.IN_GAME) return;
        
        Player player = event.getPlayer();
        GamePlayer gp = game.getRoleManager().getGamePlayer(player);
        
        if (gp == null) return;
        if (gp.getRole() == MMRole.INNOCENT) return;

        String message = game.getConfig().getMessage("no-chat");
        switch (gp.getRole()) {
            case MURDERER:
                message = message.replace("{role}", "the murderer");
                break;
            case SHERIFF:
                message = message.replace("{role}", "the sheriff");
                break;
            case SPECTATOR:
                message = message.replace("{role}", "spectators");
                break;
            default:
                return;
        }

        player.sendMessage(TextFormat.colorize(message));
        event.setCancelled(true);

    }

    @EventHandler
    public void onCommandPreprocess(PlayerCommandPreprocessEvent event) {

        if (game.getState() != GameStateType.IN_GAME) return;

        Player player = event.getPlayer();
        GamePlayer gp = game.getRoleManager().getGamePlayer(player);
        
        if (gp == null) return;

        String message = event.getMessage();

        if (message == null || message.isEmpty() || !message.startsWith("/")) return;

        message = message.substring(1)
                         .trim()
                         .split(" ")[0];

        for (String command : blockedCommands) {
            if (message.equals(command)) {
                if (gp.getRole() == MMRole.MURDERER || 
                    gp.getRole() == MMRole.SHERIFF || 
                    gp.getRole() == MMRole.SPECTATOR) {
                    
                    event.setCancelled(true);
                    player.sendMessage(TextFormat.colorize(
                        game.getConfig().getMessage("no-chat")
                    ));

                    return;
                }
            }
        }

    }

}