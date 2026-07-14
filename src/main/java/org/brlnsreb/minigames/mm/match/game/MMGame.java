package org.brlnsreb.minigames.mm.match.game;

import java.util.*;

import org.brlnsreb.BrlnsReb;
import org.brlnsreb.core.ConfigManager;
import org.brlnsreb.core.minigame.match.GameStateType;
import org.brlnsreb.core.minigame.match.MatchExpand;
import org.brlnsreb.core.minigame.match.game.GameExpand;
import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.core.player.PlayerUtils;
import org.brlnsreb.core.player.CustomPlayer.DamageMode;
import org.brlnsreb.core.player.CustomPlayer.InteractMode;
import org.brlnsreb.core.player.data.database.AccountsManager;
import org.brlnsreb.minigames.mm.entities.DeadBodyEntity;
import org.brlnsreb.minigames.mm.match.game.items.MMItemManager;
import org.brlnsreb.minigames.mm.match.game.ui.MMBossBar;
import org.brlnsreb.minigames.mm.match.game.ui.MMScoreboard;
import org.brlnsreb.minigames.mm.roles.MMRole;
import org.brlnsreb.utils.TimerSystem;
import org.brlnsreb.utils.YamlUtil;
import org.brlnsreb.utils.voting.TimeOfDay;
import org.brlnsreb.utils.voting.Weather;
import org.powernukkitx.Player;
import org.powernukkitx.entity.Entity;
import org.powernukkitx.entity.effect.EffectType;
import org.powernukkitx.event.player.PlayerChatEvent;
import org.powernukkitx.event.player.PlayerCommandPreprocessEvent;
import org.powernukkitx.item.Item;
import org.powernukkitx.level.Position;
import org.powernukkitx.scheduler.Task;

public class MMGame extends GameExpand {

    private final Map<CustomPlayer, MMPlayerGameData> gameDataMap = new HashMap<>();

    private final MMBossBar bossBar;
    private final MMScoreboard scoreboard;
    private final MMItemManager items;

    private TimerSystem timer;
    private Task updateUiTask;

    private boolean isSheriffAlive = true;

    private final Set<Entity> deadBodies = new HashSet<>();

    private static final String[] blockedChatCommands = {
        "grm",
        "frm",
        "say",
        "whisper",
        "tell",
        "msg",
        "me"
    };

    public MMGame(MatchExpand match, String map, TimeOfDay time, Weather weather) {
        super(match, map, time, weather);

        this.bossBar = new MMBossBar(players, config.getInt("game.time-start-tracking"));
        this.scoreboard = new MMScoreboard();
        this.items = new MMItemManager(config);
    }


    //join-leave logic

    protected Position onJoinPosition(CustomPlayer player) {
        return arena.getRandomSpawn();
    }

    protected void prepareGameData(CustomPlayer player) {
        gameDataMap.put(player, new MMPlayerGameData(player));
    }

    protected void onJoinPreparePlayer(CustomPlayer player) {
        player.interactMode = InteractMode.LIMITED;

        if (config.getBoolean(arena.getConfigPath() + "night-vision")) {
            for (CustomPlayer p : players) {
                PlayerUtils.giveEffect(p, EffectType.NIGHT_VISION, 99999999, 2, false);
            }
        }

        bossBar.updateExp(player, gameDataMap.get(player));
    }

    protected void setPregameNameTag(CustomPlayer player) {
        player.ingameChatNameTag = player.lobbyNameTag;
    }

    public void onJoinPrepareSpectator(CustomPlayer player) {
        player.setAllowFlight(true);
        player.setFlying(true);
        PlayerUtils.giveEffect(player, EffectType.NIGHT_VISION, 99999999, 2, false);

        scheduler.scheduleDelayedTask(BrlnsReb.instance, () -> playDeadBodyAnimation(player), 20);
        scheduler.scheduleDelayedTask(BrlnsReb.instance, () -> playDeadBodyAnimation(player), 60);
    }

    private void playDeadBodyAnimation(Player player) {
        for (Entity e : deadBodies) {
            DeadBodyEntity body = (DeadBodyEntity) e;
            body.playAnimation(body.getStaticAnimation(), Collections.singleton(player));
        }
    }


    public void onLeave(CustomPlayer player) {
        prepareAndSaveData(player);
        gameDataMap.remove(player);

        checkWinConditions();
    }

    public void prepareAndSaveData(CustomPlayer player) {
        if (state.current == GameStateType.ENDING) gameDataMap.get(player).addStatOnEnding();
        AccountsManager.savePlayerData(player);
    }


    //<GAME LIFECYCLE>

    //pregame

    protected void prepareGame() {
        for (CustomPlayer p : players) {
            bossBar.updateExp(p, gameDataMap.get(p));
        }

        //TODO: prepareGame message with bars

        //builders message
        List<String> builders = config.getStringList(arena.getConfigPath() + "builders");
        if (!builders.isEmpty()) {
            String buildersStr = String.join("&7, &d", builders);
            
            String buildersTeam = YamlUtil.getStr(arena.getConfigPath() + "build-team", config);
            if (buildersTeam != null && buildersTeam.length() > 0) buildersStr = buildersStr + " &7/ &d" + buildersTeam;

            String creditsMsg = YamlUtil.getStr("match.game.map-credits", ConfigManager.getGlobalMessages()).formatted(buildersStr);
            msgUtil.broadcastPrefix(creditsMsg);
        }

        //load gold spawns
        //TODO: load gold spawn
    }

    protected void updatePregameScoreboards(String formattedTime) {
        scoreboard.updatePregame(players, formattedTime);
        scoreboard.updatePregame(spectators, formattedTime);
    }


    //GAME

    //start

    protected void setGameNameTag(CustomPlayer player) {
        player.setNameTag("");
    }

    protected void startGame() {
        //role assignment, items
        List<CustomPlayer> shuffledPlayers = new ArrayList<>(players);
        Collections.shuffle(shuffledPlayers);
        MMPlayerGameData currGameData;

        for (int i = 0; i < shuffledPlayers.size(); i++) {
            currGameData = gameDataMap.get(shuffledPlayers.get(i));
            if (currGameData == null) {     //in case of disconnection
                shuffledPlayers.remove(i--);
                continue;
            }

            switch (i) {
                case 0 -> setRole(MMRole.MURDERER, shuffledPlayers.get(i), currGameData);
                case 1 -> setRole(MMRole.SHERIFF, shuffledPlayers.get(i), currGameData);
                default -> setRole(MMRole.INNOCENT, shuffledPlayers.get(i), currGameData);
            }
        }

        //timer start
        timer = new TimerSystem();
        timer.start(config.getInt("game.duration"), this::onGameEnding);

        //ui
        updateUiTask = new Task() {
            @Override
            public void onRun(int currentTick) {
                updateGameScoreboards();
                updateGameBossBar();
            }
        };
        scheduler.scheduleRepeatingTask(BrlnsReb.instance, updateUiTask, config.getInt("game.ui-update-interval"));

        //spawn gold
        //TODO: spawn gold

        //player check at game start
        if (config.getBoolean("settings.check-players-at-game-start")) {
            scheduler.scheduleDelayedTask(BrlnsReb.instance, this::checkWinConditions, 20);      //end the game directly if the players aren't enough
        }
    }

    private void setRole(MMRole role, CustomPlayer player, MMPlayerGameData gameData) {
        gameData.role = role;
        items.giveItemsAtStart(player, gameData);

        player.setAttackVars(
            DamageMode.ONLY_PLAYERS,
            role == MMRole.MURDERER || role == MMRole.SHERIFF,
            false
        );

        //title message
        String titlePath = switch (role) {
            case MURDERER -> "title.murderer";
            case SHERIFF -> "title.sheriff";
            case INNOCENT -> "title.innocent";
        };
        String subtitlePath = switch (role) {
            case MURDERER -> "title.murderer-sub";
            case SHERIFF -> "title.sheriff-sub";
            case INNOCENT -> "title.innocent-sub";
        };
        player.sendTitle(
            YamlUtil.getStr(titlePath, config),
            YamlUtil.getStr(subtitlePath, config),
            10, 60, 10
        );
    }


    //ui update

    public void updateGameScoreboards() {
        int innocents = players.size() - 2;     //total - murderer - sheriff
        String formattedTime = timer.getFormattedTime();

        for (CustomPlayer p : players) {
            scoreboard.updateIngame(p, innocents, isSheriffAlive, formattedTime, gameDataMap.get(p).role);
        }

        for (CustomPlayer s : spectators) {
            scoreboard.updateSpectator(s, innocents, isSheriffAlive, formattedTime, spectators.size());
        }
    }

    public void updateGameBossBar() {
        int secondsRemaining = timer.getSecondsRemaining();

        for (CustomPlayer p : players) {
            bossBar.updateGameBossBar(p, gameDataMap.get(p), secondsRemaining);
        }
    }

    
    //death

    public void kill(CustomPlayer player) {
        //TODO
    }


    //features

    public void useFlash(CustomPlayer player) {
        MMPlayerGameData gameData = gameDataMap.get(player);
        if (gameData.flashUsed) return;

        gameData.flashUsed = true;
        items.useFlash(player);

        //TODO: use flash
    }


    //check win conditions

    public boolean checkWinConditions() {

    }


    //ending

    public void endGame() {
        stopGame();
        //TODO
    }

    public void forceStop() {
        timer.stop();
        stopGame();
        //TODO
    }

    private void stopGame() {
        if (updateUiTask != null) updateUiTask.cancel();
        //gold stop
    }


    //listener access

    public void onItemUse(CustomPlayer player, Item item) {
        //TODO
    }

    public boolean onChat(CustomPlayer player, PlayerChatEvent event) {
        if (players.contains(player)) return chatRoleCheck(player);

        if (spectators.contains(player)) {
            event.getRecipients().removeIf(recipient -> players.contains(recipient));
        }

        return false;
    }

    public boolean onCommandPreprocess(CustomPlayer player, PlayerCommandPreprocessEvent event) {
        String message = event.getMessage().substring(1).trim().split(" ")[0];

        for (String command : blockedChatCommands) {
            if (!message.equals(command)) continue;

            if (players.contains(player)) return chatRoleCheck(player);
            if (spectators.contains(player)) {
                msgUtil.sendPresetMessagePrefix("no-chat", player, new String[] { "spectators" });
                return false;
            }
        }
        
        return true;
    }

    private boolean chatRoleCheck(CustomPlayer player) {
        switch (gameDataMap.get(player).role) {
            case MURDERER:
                msgUtil.sendPresetMessagePrefix("no-chat", player, new String[] { "the murderer" });
                return false;

            case SHERIFF:
                msgUtil.sendPresetMessagePrefix("no-chat", player, new String[] { "the sheriff" });
                return false;

            case INNOCENT: return true;
            default: return true;
        }
    }

}