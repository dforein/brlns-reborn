package org.brlnsreb.mainhub.messages;

import java.util.List;
import java.util.stream.Collectors;

import org.brlnsreb.BrlnsReb;
import org.brlnsreb.core.minigame.Minigame;
import org.brlnsreb.core.minigame.MinigameManager;
import org.brlnsreb.mainhub.MainHub;
import org.brlnsreb.utils.messages.ChatMsgs;
import org.powernukkitx.Player;
import org.powernukkitx.utils.Config;
import org.powernukkitx.utils.TextFormat;

public class MainLobbyMessages {

    private Config messages;
    private List<String> messageList;
    private int index;

    public MainLobbyMessages(Config messages) {
        this.messages = messages;
        onConfigReload();
    }

    public void startMessagesRotation(int seconds) {
        BrlnsReb.getScheduler().scheduleDelayedRepeatingTask(BrlnsReb.instance, 
            () -> sendMessageAndRotate(),
            seconds*20, seconds*20
        );
    }

    public void sendMessageAndRotate() {
        if (messageList.isEmpty()) return;

        String msg = ChatMsgs.BROKENLENS_PFX + messageList.get(index);

        for (Player p : MainHub.instance.getMap().level.getPlayers().values()) {
            p.sendMessage(msg);
        }

        for (Minigame mg : MinigameManager.getMinigames()) {
            for (Player p : mg.getLobby().getMap().getPlayers().values()) {
                p.sendMessage(msg);
            }
        }

        index++;
        if (index + 1 >= messageList.size()) index = 0;
    }

    public void onConfigReload() {
        messageList = messages.getStringList("main-lobby-messages").stream()
            .map(msg -> TextFormat.colorize(msg))
            .collect(Collectors.toUnmodifiableList());
        index = 0;
    }
    
}
