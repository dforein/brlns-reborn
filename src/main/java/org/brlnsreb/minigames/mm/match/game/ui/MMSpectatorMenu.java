package org.brlnsreb.minigames.mm.match.game.ui;

import org.powernukkitx.Server;
import org.powernukkitx.form.window.SimpleForm;
import org.powernukkitx.scheduler.TaskHandler;

import org.brlnsreb.core.minigame.match.GameStateType;
import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.core.player.PlayerUtils;
import org.brlnsreb.mainhub.MainHub;
import org.brlnsreb.minigames.mm.match.game.MMGame;
import org.brlnsreb.minigames.mm.match.game.gamedata.MMPlayerGameData;
import org.brlnsreb.utils.ChatMsgs;
import org.brlnsreb.utils.abstraction.MenuAbstract;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class MMSpectatorMenu extends MenuAbstract {

    private final MMGame game;

    private final Map<UUID, List<UUID>> spectatePendingMenus;
    private final Map<UUID, TaskHandler> spectateHandlers;
    
    public MMSpectatorMenu(MMGame game) {
        this.game = game;

        this.spectatePendingMenus = new ConcurrentHashMap<>();
        this.spectateHandlers = new ConcurrentHashMap<>();
    }
    
    public void openSpectateMenu(CustomPlayer spectator) {
        checkCooldown(spectator);

        SimpleForm menu = new SimpleForm("Spectate player");

        List<UUID> playerIds = new ArrayList<>();

        menu.addButton("Random Player");

        MMPlayerGameData gameData;
        boolean playersEmpty = true;

        for (CustomPlayer p : game.getPlayers()) {
            gameData = game.getGameData(p);
            if (gameData == null) continue;
            playersEmpty = false;

            String displayName = p.data.name 
                + switch (gameData.role) {
                    case SHERIFF -> " §8(§l§9SHERIFF§r§8)";
                    case MURDERER -> " §8(§l§cMURDERER§r§8)";
                    case INNOCENT -> " §8(§l§aINNOCENT§r§8)";
                };
            
            menu.addButton(displayName);
            playerIds.add(p.getUniqueId());
        }
        
        if (playersEmpty) {
            spectator.sendMessage(ChatMsgs.ERROR_PFX + "No players alive to teleport to!");
            return;
        }
        
        int formId = sendForm(spectator, menu);
        spectatePendingMenus.put(spectator.getUniqueId(), playerIds);
        menu.onSubmit((p, response) -> handleSpectateResponse(spectator, response.buttonId(), formId));
    }

    public void openActionsMenu(CustomPlayer spectator) {
        checkCooldown(spectator);

        SimpleForm menu = new SimpleForm("Spectator Actions");

        menu.addButton("Return to Lobby");
        menu.addButton("Play Again");
        menu.addButton("Play Another Game");
        
        int formId = sendForm(spectator, menu);
        menu.onSubmit((p, response) -> handleActionsResponse(spectator, response.buttonId(), formId));
    }
    
    private void handleSpectateResponse(CustomPlayer spectator, int buttonId, int formId) {
        removeForm(formId);
        
        List<UUID> playerIds = spectatePendingMenus.remove(spectator.getUniqueId());
        CustomPlayer target = PlayerUtils.getPlayer(playerIds.get(
            buttonId == 0 
                ? ThreadLocalRandom.current().nextInt(playerIds.size()) 
                : (buttonId - 1)
        ));
        
        if (target == null) {
            game.getMsgUtil().sendPresetMessagePrefix(spectator, "player-not-available");
            return;
        }

        spectator.teleport(target.getLocation());

        game.getMsgUtil().sendPresetMessagePrefix(
            spectator,
            "teleported-to", 
            new String[] { target.data.name }
        );

        TaskHandler oldHandler = spectateHandlers.remove(spectator.getUniqueId());
        if (oldHandler != null) oldHandler.cancel();

        final TaskHandler[] currHandler = new TaskHandler[1];
        currHandler[0] = Server.getInstance().getScheduler().scheduleRepeatingTask(() -> {
            if (!game.getPlayers().contains(target) || !game.getSpectators().contains(spectator)
                    || !(game.getCurrentState() == GameStateType.IN_GAME || game.getCurrentState() == GameStateType.ENDING)
            ) {
                if (currHandler[0] != null) currHandler[0].cancel();
                spectateHandlers.remove(spectator.getUniqueId());
                return;
            }

            spectator.sendActionBar(
                "§l§aTarget: §e"+ target.data.name +" §aDistance: §d%.2fm".formatted(spectator.distance(target))
            );
        }, 20);

        spectateHandlers.put(spectator.getUniqueId(), currHandler[0]);
    }

    private void handleActionsResponse(CustomPlayer spectator, int buttonId, int formId) {
        removeForm(formId);

        spectator.matchCurrent.onLeave(spectator);

        switch (buttonId) {
            case 0 -> spectator.minigameCurrent.onLobbyJoin(spectator);     //return to lobby
            case 1 -> spectator.minigameCurrent.onMatchJoin(spectator);     //play again
            case 2 -> MainHub.instance.onJoin(spectator);              //play another game
        }
    }
}