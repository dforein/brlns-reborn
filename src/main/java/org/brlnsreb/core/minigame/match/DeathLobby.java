package org.brlnsreb.core.minigame.match;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.brlnsreb.BrlnsReb;
import org.brlnsreb.core.Configs;
import org.brlnsreb.core.lobby.Lobby;
import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.core.player.PlayerStateType;
import org.brlnsreb.mainhub.MainHub;
import org.brlnsreb.utils.ChatMsgs;
import org.brlnsreb.utils.YamlUtil;
import org.powernukkitx.Player;
import org.powernukkitx.level.Location;
import org.powernukkitx.scheduler.Task;
import org.powernukkitx.utils.Config;

public class DeathLobby extends Lobby {

    private final Map<UUID, Location> playerLocs = new HashMap<>();
    private Task afkCheckTask;

    public DeathLobby(MatchExpand match) {
        super(match);

        //play again NPC
        spawnNpc(
            configPath() + "npc.play-again.",
            player -> {
                onLeave(player);
                minigame.onMatchJoin(player);
            }
        );

        //spectate NPC
        spawnNpc(
            configPath() + "npc.spectate.",
            player -> {
                if (match.closed) {
                    player.sendMessage(ChatMsgs.ERROR_PFX + "The match has already ended! You can return to lobby or join a new match.");
                    return;
                }
                onLeave(player);
                match.onJoinAsSpectator(player);
            }
        );

        //return to lobby NPC
        spawnNpc(
            configPath() + "npc.return-to-lobby.",
            player -> {
                onLeave(player);
                minigame.onLobbyJoin(player);
            }
        );


        afkCheckTask = new Task() {
            @Override
            public void onRun(int currentTick) {
                for (Player p : map.getPlayers().values()) {
                    Location prev = playerLocs.putIfAbsent(p.getUniqueId(), p.getLocation());
                    if (prev != null && playerLocs.get(p.getUniqueId()).equals(prev)) {
                        onLeave(p);
                        MainHub.instance.onJoin((CustomPlayer) p);
                    }
                }

                if (match.closed && map.getPlayers().isEmpty()) {
                    close();
                    this.cancel();
                }
            }
        };
        BrlnsReb.getScheduler().scheduleDelayedRepeatingTask(BrlnsReb.instance, afkCheckTask, 30 * 20, 30 * 20);
    }


    //join logic

    protected PlayerStateType onJoinState() {
        return PlayerStateType.DEATH_LOBBY;
    }

    protected void onJoinMessages(CustomPlayer player) {
        player.sendTitle(
            YamlUtil.getStr(requireConfigPath() + "title", config), 
            YamlUtil.getStr(requireConfigPath() + "subtitle", config),
            10, 60, 10
        );
    }

    protected void onJoinUi(CustomPlayer player) {}
    protected void onJoinItems(CustomPlayer player) {}


    public void onLeave(Player player) {
        playerLocs.remove(player.getUniqueId());
    }

    
    
    public Config getConfig() { return Configs.getGlobalConfig(); }
    public Config getMessages() { return null; }
    public String requireConfigPath() { return "match.death-lobby."; }
}
