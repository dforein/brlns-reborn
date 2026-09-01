package org.brlnsreb.utils.abstraction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.brlnsreb.BrlnsReb;
import org.brlnsreb.core.minigame.match.GameStateType;
import org.brlnsreb.core.minigame.match.game.Game;
import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.core.player.PlayerUtils;
import org.brlnsreb.mainhub.MainHub;
import org.brlnsreb.utils.messages.ChatMsgs;
import org.powernukkitx.form.window.SimpleForm;
import org.powernukkitx.scheduler.TaskHandler;

public abstract class SpectatorMenuAbstract extends MenuAbstract {

    private final Map<UUID, List<UUID>> spectatePendingMenus = new HashMap<>();
    private final Map<UUID, TaskHandler> spectateHandlers = new HashMap<>();


    public void openActionsMenu(CustomPlayer spectator) {
        checkCooldown(spectator);

        SimpleForm menu = new SimpleForm("Spectator Actions");

        menu.addButton("Return to Lobby");
        menu.addButton("Play Again");
        menu.addButton("Play Another Game");
        
        int formId = sendForm(spectator, menu);
        menu.onSubmit((p, response) -> handleActionsResponse(spectator, response.buttonId(), formId));
    }

    private void handleActionsResponse(CustomPlayer spectator, int buttonId, int formId) {
        removeForm(formId);

        spectator.matchCurrent.onLeave(spectator);

        switch (buttonId) {
            case 0 -> spectator.minigameCurrent.onLobbyJoin(spectator);     //return to lobby
            case 1 -> spectator.minigameCurrent.onMatchJoin(spectator);     //play again
            case 2 -> MainHub.instance.onJoin(spectator);                   //play another game
        }
    }


    public void openSpectateMenu(CustomPlayer spectator) {
        if (!checkCooldown(spectator)) return;

        SimpleForm menu = new SimpleForm("Spectate player");

        List<UUID> playerIds = new ArrayList<>();

        menu.addButton("Random Player");

        boolean playersEmpty = true;
        for (CustomPlayer p : getGame().getPlayers()) {
            String displayName = getDisplayNameForSpectateMenu(p);
            if (displayName == null) continue;
            playersEmpty = false;
            
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

    protected abstract String getDisplayNameForSpectateMenu(CustomPlayer player);

    private void handleSpectateResponse(CustomPlayer spectator, int buttonId, int formId) {
        removeForm(formId);

        Game game = getGame();
        
        List<UUID> playerIds = spectatePendingMenus.remove(spectator.getUniqueId());
        CustomPlayer target = PlayerUtils.getPlayer(playerIds.get(
            buttonId == 0 
                ? ThreadLocalRandom.current().nextInt(playerIds.size()) 
                : (buttonId - 1)
        ));
        
        if (target == null || !game.getPlayers().contains(target)) {
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
        currHandler[0] = BrlnsReb.getScheduler().scheduleRepeatingTask(() -> {
            if (!game.getPlayers().contains(target) || !game.getSpectators().contains(spectator)
                    || !(game.state() == GameStateType.IN_GAME || game.state() == GameStateType.ENDING)
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

    protected abstract Game getGame();

}
