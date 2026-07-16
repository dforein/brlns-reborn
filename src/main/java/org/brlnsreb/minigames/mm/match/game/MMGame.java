package org.brlnsreb.minigames.mm.match.game;

import java.util.*;

import org.brlnsreb.BrlnsReb;
import org.brlnsreb.core.ConfigManager;
import org.brlnsreb.core.minigame.match.GameStateType;
import org.brlnsreb.core.minigame.match.MatchExpand;
import org.brlnsreb.core.minigame.match.game.GameExpand;
import org.brlnsreb.core.minigame.match.game.arena.Arena;
import org.brlnsreb.core.minigame.match.game.arena.RandomSpawnsArena;
import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.core.player.PlayerUtils;
import org.brlnsreb.core.player.CustomPlayer.DamageMode;
import org.brlnsreb.core.player.CustomPlayer.InteractMode;
import org.brlnsreb.core.player.data.StatType;
import org.brlnsreb.core.player.data.database.AccountsManager;
import org.brlnsreb.minigames.mm.match.game.entities.DeadBodyEntity;
import org.brlnsreb.minigames.mm.match.game.entities.ThrownSwordEntity;
import org.brlnsreb.minigames.mm.match.game.gamedata.MMPlayerGameData;
import org.brlnsreb.minigames.mm.match.game.gamedata.MMRole;
import org.brlnsreb.minigames.mm.match.game.items.MMItemManager;
import org.brlnsreb.minigames.mm.match.game.systems.DeathSystem;
import org.brlnsreb.minigames.mm.match.game.systems.GoldSystem;
import org.brlnsreb.minigames.mm.match.game.systems.ProjectileSystem;
import org.brlnsreb.minigames.mm.match.game.systems.RaycastSystem;
import org.brlnsreb.minigames.mm.match.game.ui.MMBossBar;
import org.brlnsreb.minigames.mm.match.game.ui.MMScoreboard;
import org.brlnsreb.minigames.mm.match.game.ui.MMSpectatorMenu;
import org.brlnsreb.utils.TimerSystem;
import org.brlnsreb.utils.YamlUtil;
import org.brlnsreb.utils.voting.TimeOfDay;
import org.brlnsreb.utils.voting.Weather;
import org.cloudburstmc.protocol.bedrock.data.actor.EntityDamageCause;
import org.powernukkitx.Player;
import org.powernukkitx.entity.Entity;
import org.powernukkitx.entity.effect.EffectType;
import org.powernukkitx.event.entity.ProjectileHitEvent;
import org.powernukkitx.event.inventory.InventoryPickupItemEvent;
import org.powernukkitx.event.player.PlayerChatEvent;
import org.powernukkitx.event.player.PlayerCommandPreprocessEvent;
import org.powernukkitx.item.Item;
import org.powernukkitx.level.Position;
import org.powernukkitx.scheduler.Task;
import org.powernukkitx.utils.TextFormat;

public class MMGame extends GameExpand {

    private final Map<CustomPlayer, MMPlayerGameData> gameDataMap = new HashMap<>();

    private final MMBossBar bossBar;
    private final MMScoreboard scoreboard;
    private final MMItemManager items;
    private final MMSpectatorMenu spectatorMenu;

    private TimerSystem timer;
    private Task updateUiTask;
    private final GoldSystem gold;
    private final DeathSystem death;
    private final ProjectileSystem projectile;
    private final RaycastSystem raycast;

    private CustomPlayer murderer;
    private boolean firstKill;

    private CustomPlayer sheriff;

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
        this.items = new MMItemManager(this);
        this.spectatorMenu = new MMSpectatorMenu(this);

        this.gold = new GoldSystem(config, arena);
        this.death = new DeathSystem(this, scheduler);
        this.projectile = new ProjectileSystem(this);
        this.raycast = new RaycastSystem(this);

        firstKill = true;
    }

    protected Arena prepareArena(String mapId, TimeOfDay time, Weather weather) {
        return new RandomSpawnsArena(
            config,
            mapId,
            "map-settings.maps." + mapId,
            time, weather
        );
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
        player.ingameChatNameTag = player.grayNameTag;
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
        if (player.isGameSpectator()) return;

        prepareAndSaveData(player);

        roleCheckOnLeave(player, player.getPosition());
        checkWinConditions();
    }

    public void prepareAndSaveData(CustomPlayer player) {
        AccountsManager.savePlayerData(player);
    }

    private void roleCheckOnLeave(CustomPlayer player, Position lastPos) {
        if (gameDataMap.get(player).role == MMRole.MURDERER) {
            murderer = null;
        } else if (gameDataMap.get(player).role == MMRole.SHERIFF) {
            sheriff = null;
            death.dropSheriffHoe(lastPos);
        }

        gameDataMap.remove(player);
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
        gold.loadSpawns();
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
        scheduler.scheduleRepeatingTask(BrlnsReb.instance, updateUiTask, config.getInt("game.ui-update-ticks"));

        //spawn gold
        gold.startSpawning();

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

        //save key player
        if (role == MMRole.MURDERER) murderer = player;
        if (role == MMRole.SHERIFF) sheriff = player;

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
        int innocents = players.size();
        if (isMurdererAlive()) innocents--;
        if (isSheriffAlive()) innocents--;

        String formattedTime = timer.getFormattedTime();

        for (CustomPlayer p : players) {
            scoreboard.updateIngame(p, innocents, isSheriffAlive(), formattedTime, gameDataMap.get(p).role);
        }

        for (CustomPlayer s : spectators) {
            scoreboard.updateSpectator(s, innocents, isSheriffAlive(), formattedTime, spectators.size());
        }
    }

    public void updateGameBossBar() {
        int secondsRemaining = timer.getSecondsRemaining();

        for (CustomPlayer p : players) {
            bossBar.updateGameBossBar(p, gameDataMap.get(p), secondsRemaining);
        }
    }

    
    //death

    public void onDeath(EntityDamageCause cause, CustomPlayer victim, CustomPlayer killer) {
        onDeath(victim, killer);
    }

    public void onDeath(CustomPlayer victim, CustomPlayer killer) {
        Position deathPos = victim.getPosition();

        getMatch().onEndLobbyJoin(victim);

        gameDataMap.get(victim).incrementStat(StatType.DEATHS);
        gameDataMap.get(victim).incrementStat(StatType.LOSSES);
        prepareAndSaveData(victim);

        if (killer == murderer) {
            if (isFirstKill()) {
                msgUtil.sendPresetMessagePrefix("murderer-warning", murderer);
            }
            gameDataMap.get(murderer).incrementStat(StatType.KILLS);
            
        } else if (killer == sheriff) {
            if (victim == murderer) {
                gameDataMap.get(sheriff).incrementStat(StatType.KILLS);
            } else {
                onDeath(null, sheriff, null);
            }
        }

        death.onDeath(victim, deathPos);
        roleCheckOnLeave(victim, deathPos);
        checkWinConditions();
    }

    private boolean isFirstKill() {
        if (firstKill) {
            firstKill = false;
            return true;
        }
        return false;
    }


    //features

    public void useFlash(CustomPlayer player) {
        MMPlayerGameData gameData = gameDataMap.get(player);
        if (gameData.flashUsed) return;

        gameData.flashUsed = true;
        items.useFlash(player, scheduler);
    }


    //check win conditions

    public boolean checkWinConditions() {
        if (state.current != GameStateType.IN_GAME) return false;

        if (!isMurdererAlive()) {
            onGameEnding();
            return true;

        } else if (players.size() <= 1 && isMurdererAlive()) {
            onGameEnding();
            return true;

        } else if (players.size() <= 2 && isMurdererAlive() && isSheriffAlive()) {
            onGameEnding();
            return true;
        }

        return false;
    }

    public boolean isMurdererAlive() { return murderer != null; }
    public boolean isSheriffAlive() { return sheriff != null; }


    //ending

    public void endGame() {
        stopGame();
        for (CustomPlayer p : players) {
            if (isMurdererAlive() && isSheriffAlive()) {
                if (p == murderer) gameDataMap.get(p).incrementStat(StatType.WINS);
                if (p == sheriff) gameDataMap.get(p).incrementStat(StatType.LOSSES);
            } else {
                gameDataMap.get(p).incrementStat(StatType.WINS);
            }
        }

        scheduler.scheduleDelayedTask(BrlnsReb.instance, match::onEnding, config.getInt("game.ending-duration") * 20);
    }

    public void forceStop() {
        timer.stop();
        stopGame();
        //TODO
    }

    private void stopGame() {
        if (updateUiTask != null) updateUiTask.cancel();
        gold.stop();
    }


    //listener access

    public void onItemUse(CustomPlayer player, Item item) {
        switch (item.getId()) {
            //sheriff
            case Item.GOLDEN_HOE -> {
                CustomPlayer victim = raycast.shoot(player);
                if (victim != null) onDeath(victim, player);
            }
            //murderer
            case Item.IRON_SWORD -> projectile.throwSword(player);
            case Item.BLAZE_ROD -> useFlash(player);
            //innocent
            case Item.YELLOW_DYE -> newSheriff(player);
            //spectator
            case Item.NETHER_STAR -> spectatorMenu.openSpectateMenu(player);
            case Item.COMPASS -> spectatorMenu.openActionsMenu(player);
        }
    }

    public void onProjectileHit(CustomPlayer player, ProjectileHitEvent event) {
        //TODO
        if (state.current != GameStateType.IN_GAME) return;
        if (!(event.getEntity() instanceof ThrownSwordEntity)) return;

        

        
    }

    public void onItemPickup(CustomPlayer player, InventoryPickupItemEvent event) {
        //TODO
    }

    public boolean onChat(CustomPlayer player, PlayerChatEvent event) {
        if (players.contains(player)) return roleCheckOnChat(player);

        if (spectators.contains(player)) {
            event.getRecipients().removeIf(recipient -> players.contains(recipient));
        }

        return false;
    }

    public boolean onCommandPreprocess(CustomPlayer player, PlayerCommandPreprocessEvent event) {
        String message = event.getMessage().substring(1).trim().split(" ")[0];

        for (String command : blockedChatCommands) {
            if (!message.equals(command)) continue;

            if (players.contains(player)) return roleCheckOnChat(player);
            if (spectators.contains(player)) {
                msgUtil.sendPresetMessagePrefix("no-chat", player, new String[] { "spectators" });
                return false;
            }
        }
        
        return true;
    }

    private boolean roleCheckOnChat(CustomPlayer player) {
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


    public MMPlayerGameData getGameData(CustomPlayer player) { return gameDataMap.get(player); }
    public RandomSpawnsArena getArena() { return (RandomSpawnsArena) arena; }

}