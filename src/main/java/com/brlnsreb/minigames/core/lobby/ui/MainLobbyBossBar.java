package com.brlnsreb.minigames.core.lobby.ui;

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

public class MainLobbyBossBar extends BossBarAbstract {

    private final MinigameCore plugin;
    private final String path = "lobby-bossbar.";
    private String name;                        //mainMessage1 name (of the game, or server in case of general lobby)
    private Config messages;
    private ArrayList<String> colors;           //mainMessage2 colors
    private ArrayList<String> messagesArray;
    private int messagesIndex = 0;              //i will periodically change messages in messages.yml order

    public MainLobbyBossBar(String name) {
        plugin = MinigameCore.getInstance();
        reloadConfig(name);
    }

    public void reloadConfig(String name) {
        this.name = name;
        this.messages = new Config(plugin.getDataFolder() + "global/messages.yml", Config.YAML);

        String mainMessage1 = messages.getString(path + "message1");
        String mainMessage2 = messages.getString(path + "message2");

        this.messagesArray = new ArrayList<>(
            messages.getList(path + "other-messages")
                .stream()
                .map(capture -> capture.toString())
                .collect(Collectors.toList())
        );

        this.messagesArray.addFirst(mainMessage2);
        this.messagesArray.addFirst(mainMessage1);

        this.colors = new ArrayList<>(              //for mainMessage2, because it changes color quickly
            messages.getList(path + "colors")
                .stream()
                .map(capture -> capture.toString())
                .collect(Collectors.toList())
        );
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
        
        if (this.messagesIndex >= this.messagesArray.size()) {
            this.messagesIndex = 0;
        }
    }

    public void updateLobbyBossBar(CustomPlayer player) {
        switch (this.messagesIndex) {
            case 0:
                updateBossBar(player, messagesArray.get(0).formatted(name));
                break;
            
            case 1:
                int[] colorIndex = {0};

                TaskHandler[] taskRef = new TaskHandler[1];
                taskRef[0] = Server.getInstance().getScheduler().scheduleRepeatingTask(plugin,
                    () -> {
                        int index = colorIndex[0];
                        if (index >= colors.size()) {
                            index -= colors.size();
                        }

                        updateBossBar(player, messagesArray.get(1).formatted(colors.get(index)));
                        colorIndex[0]++;

                        if (colorIndex[0] >= colors.size()) {
                            taskRef[0].cancel();
                        }
                    }, 0
                );
                break;
        
            default:
                updateBossBar(player, this.messagesArray.get(messagesIndex));
                break;
        }
    }

}
