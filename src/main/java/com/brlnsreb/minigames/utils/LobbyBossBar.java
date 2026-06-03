package com.brlnsreb.minigames.utils;

import java.util.ArrayList;
import java.util.stream.Collectors;

import com.brlnsreb.minigames.MinigameCore;
import com.brlnsreb.minigames.core.player.CustomPlayer;
import com.brlnsreb.minigames.utils.abstraction.BossBarAbstract;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.level.Level;
import cn.nukkit.scheduler.TaskHandler;
import cn.nukkit.utils.Config;

public class LobbyBossBar extends BossBarAbstract {

    private final MinigameCore plugin;
    private final String path = "lobby-bossbar.";
    private String mainMsg1Name;
    private String mainMessage1;
    private String mainMessage2;
    private ArrayList<String> mainMsg2Colors;
    private int numberOfColors;
    private ArrayList<String> otherMessages;
    private int numberOfMessages;
    private int messagesIndex = 0;      //i will periodically change messages in messages.yml order

    public LobbyBossBar(MinigameCore plugin, String mainMsg1Name, Config messages) {
        this.plugin = plugin;
        reloadConfig(mainMsg1Name, messages);
    }

    public void reloadConfig(String mainMsg1Name, Config messages) {
        this.mainMsg1Name = mainMsg1Name;
        this.mainMessage1 = messages.getString(path + "message1");
        this.mainMessage2 = messages.getString(path + "message2");

        this.mainMsg2Colors = new ArrayList<>(
            messages.getList(path + "colors")
                .stream()
                .map(capture -> capture.toString())
                .collect(Collectors.toList())
        );

        this.otherMessages = new ArrayList<>(
            messages.getList(path + "other-messages")
                .stream()
                .map(capture -> capture.toString())
                .collect(Collectors.toList())
        );

        this.numberOfColors = this.mainMsg2Colors.size();
        this.numberOfMessages = 2 + this.otherMessages.size();
    }

    public void startBossBarUpdates(Level level) {
        Server.getInstance().getScheduler().scheduleRepeatingTask(plugin,
            () -> {
                for (Player player : level.getPlayers().values()) {
                    this.updateLobbyBossBar((CustomPlayer) player);
                }

                this.updateDisplayedMessage();
            }, 200      //update every 10s
        );
    }

    public void updateDisplayedMessage() {
        this.messagesIndex++;
        
        if (this.messagesIndex >= this.numberOfMessages) {
            this.messagesIndex = 0;
        }
    }

    public void updateLobbyBossBar(CustomPlayer player) {
        switch (this.messagesIndex) {
            case 0:
                updateBossBar(player, mainMessage1.formatted(mainMsg1Name));
                break;
            
            case 1:
                int[] colorIndex = {0};

                TaskHandler[] taskRef = new TaskHandler[1];
                taskRef[0] = Server.getInstance().getScheduler().scheduleRepeatingTask(plugin,
                    () -> {
                        int index = colorIndex[0];
                        if (index >= numberOfColors) {
                            index -= numberOfColors;
                        }

                        updateBossBar(player, mainMessage2.formatted(mainMsg2Colors.get(index)));
                        colorIndex[0]++;

                        if (colorIndex[0] >= numberOfColors) {
                            taskRef[0].cancel();
                        }
                    }, 0
                );
                break;
        
            default:
                updateBossBar(player, this.otherMessages.get(this.messagesIndex - 2));
                break;
        }
    }

}
