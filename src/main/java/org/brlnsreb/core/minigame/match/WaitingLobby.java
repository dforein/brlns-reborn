package org.brlnsreb.core.minigame.match;

import java.util.Set;

import org.brlnsreb.core.ConfigManager;
import org.brlnsreb.core.lobby.Lobby;
import org.brlnsreb.core.lobby.entities.NPCEntity;
import org.brlnsreb.core.minigame.match.ui.WaitingLobbyBossBar;
import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.core.player.PlayerStateType;
import org.brlnsreb.utils.ItemManager;
import org.brlnsreb.utils.YamlUtil;

import cn.nukkit.Player;
import cn.nukkit.item.Item;
import cn.nukkit.utils.Config;

public abstract class WaitingLobby extends Lobby {

    protected final Set<Player> players;

    protected final int minPlayers;
    protected final int minPlayersShortenedCountdown;
    protected final int maxPlayers;

    protected final int maxCountdownSeconds;
    protected final int shortenedCountdownSeconds;

    protected final NPCEntity leaveNpc;

    protected final WaitingLobbyBossBar bossBar;

    public WaitingLobby(MinigameMatch match) {
        super(match);

        this.players = match.getPlayers();

        this.minPlayers = match.getMinPlayers();
        this.minPlayersShortenedCountdown = config.getInt("settings.min-players-shortened-countdown");
        this.maxPlayers = match.getMaxPlayers();

        Config globalConfig = ConfigManager.getConfig("global/config.yml");
        this.maxCountdownSeconds = globalConfig.getInt(configPath() + "max-countdown");
        this.shortenedCountdownSeconds = globalConfig.getInt(configPath() + "shortened-countdown");

        this.leaveNpc = spawnNpc(
            configPath() + "npc.", 
            (Player player) -> { this.onLeave(player); }
        );

        this.bossBar = new WaitingLobbyBossBar(globalConfig);
        
    }

    @Override
    public boolean onJoin(Player player) {
        if (players.size() >= maxPlayers) {
            minigame.onMatchCreation();
            return false;
        }

        if (players.contains(player)) return false;

        players.add(player);
        super.onJoin(player);

        checkPlayerNumber();

        return true;
    }

    protected PlayerStateType onJoinState() { 
        return PlayerStateType.WAITING_LOBBY; 
    }

    protected void onJoinBossBar(CustomPlayer player) {
        bossBar.updateWaitingLobbyBossBar(maxCountdownSeconds, false);
    }

    protected void onJoinItems(CustomPlayer player) {
        //give game poll
        Config config = ConfigManager.getConfig("global/config.yml");
        ItemManager.giveItem(
            player, 
            7, 
            Item.NETHER_STAR, 
            YamlUtil.getStr("items.game-poll", config)
        );
    }

    protected void onLeave(Player player) {
        match.onLeave(player);
        checkPlayerNumber();
    }

    protected void checkPlayerNumber() {
        if (players.size() > minPlayersShortenedCountdown) {
            
        } else if (players.size() > minPlayers) {
            match.state.current = GameStateType.LOBBY_COUNTDOWN;

        } else if (match.state.current == GameStateType.LOBBY_COUNTDOWN) {
            match.state.current = GameStateType.WAITING_LOBBY;

        }
    }



    //match.initGame(selectedMap);

    public String requireConfigPath() { return "waiting-lobby."; }
    
}