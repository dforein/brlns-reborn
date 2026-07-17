package org.brlnsreb.generallobby.ui;

import java.util.ArrayList;
import java.util.stream.Collectors;

import org.brlnsreb.BrlnsReb;
import org.brlnsreb.core.Configs;
import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.utils.abstraction.BossBarAbstract;

import org.powernukkitx.Player;
import org.powernukkitx.Server;
import org.powernukkitx.level.Level;
import org.powernukkitx.scheduler.TaskHandler;
import org.powernukkitx.utils.Config;

public class MainLobbyBossBar extends BossBarAbstract {

    private final int BOSSBAR_UPDATE_PERIOD = 7 * 20;   //in ticks
    private final String PATH = "lobby.bossbar.";

    private String name;                            //mainMessage1 name (of the game, or server in case of general lobby)
    private Config messages;
    private ArrayList<String> colors;               //mainMessage2 colors
    private ArrayList<String> messagesArray;
    private int messagesIndex = 0;                  //i will periodically change messages in messages.yml order

    public MainLobbyBossBar(String name) {
        onConfigReload(name);
    }

    public void onConfigReload(String name) {
        this.name = name;
        this.messages = Configs.getConfig("global/messages.yml");

        String mainMessage1 = messages.getString(PATH + "message1");
        String mainMessage2 = messages.getString(PATH + "message2");

        this.messagesArray = new ArrayList<>(
            messages.getList(PATH + "other-messages")
                .stream()
                .map(capture -> capture.toString())
                .collect(Collectors.toList())
        );

        this.messagesArray.addFirst(mainMessage2);
        this.messagesArray.addFirst(mainMessage1);

        this.colors = new ArrayList<>(              //for mainMessage2, because it changes color quickly
            messages.getList(PATH + "colors")
                .stream()
                .map(capture -> capture.toString())
                .collect(Collectors.toList())
        );
    }

    public void startBossBarUpdates(Level level) {
        Server.getInstance().getScheduler().scheduleRepeatingTask(BrlnsReb.instance,
            () -> {
                for (Player player : level.getPlayers().values()) {
                    this.updateLobbyBossBar((CustomPlayer) player);
                }

                this.updateDisplayedMessage();
            }, BOSSBAR_UPDATE_PERIOD
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
                taskRef[0] = Server.getInstance().getScheduler().scheduleRepeatingTask(BrlnsReb.instance,
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
                    }, 5
                );
                break;
        
            default:
                updateBossBar(player, this.messagesArray.get(messagesIndex));
                break;
        }
    }

}
