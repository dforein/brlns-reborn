package com.brlnsreb.minigames.mm;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.data.EntityFlag;
import cn.nukkit.entity.effect.Effect;
import cn.nukkit.entity.effect.EffectType;
import cn.nukkit.level.Level;
import cn.nukkit.level.Location;
import cn.nukkit.level.Position;
import cn.nukkit.math.Vector3;
import cn.nukkit.scheduler.Task;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.TextFormat;
import com.brlnsreb.minigames.MinigameCore;
import com.brlnsreb.minigames.core.Arena;
import com.brlnsreb.minigames.core.GameState;
import com.brlnsreb.minigames.mm.config.MMConfig;
import com.brlnsreb.minigames.mm.entities.DeadBodyEntity;
import com.brlnsreb.minigames.mm.items.ItemManager;
import com.brlnsreb.minigames.mm.roles.GamePlayer;
import com.brlnsreb.minigames.mm.roles.MMRole;
import com.brlnsreb.minigames.mm.roles.MMRoleManager;
import com.brlnsreb.minigames.mm.systems.*;

import java.util.*;

public class MurderMysteryGame {
    
    private final MinigameCore plugin;
    private final MMConfig config;
    private final MMRoleManager roleManager;
    
    private GameState state;
    private Arena arena;
    private final List<Player> players;
    private String selectedMap;
    private String selectedTime;
    
    private TimerSystem timer;
    private ScoreboardSystem scoreboard;
    private RaycastSystem raycast;
    private ProjectileSystem projectile;
    private DeathSystem death;
    private GoldSystem gold;
    private CooldownSystem cooldowns;
    private BossBarSystem bossBar;
    private GoldSpawnMapper mapper;
    private QuitTracker quitTracker;
    private SpectatorMenu spectatorMenu;
    private TrackerSystem trackerSystem;
    private VotingSystem votingSystem;
    private VotingMenu votingMenu;

    private Task updateTask;

    private boolean countdownShortened;
    private int initialCountdown;
    
    private boolean firstKill;
    public boolean checkEnoughPlayers;

    private final Set<Entity> deadBodies = new HashSet<>();
    private final List<Position> redstonePositions = new ArrayList<>();
    
    public MurderMysteryGame(MinigameCore plugin) {
        this.plugin = plugin;
        this.config = new MMConfig(plugin.getConfig());
        this.roleManager = new MMRoleManager();
        this.state = GameState.LOBBY;
        this.players = new ArrayList<>();
        this.checkEnoughPlayers = true;
        
        this.scoreboard = new ScoreboardSystem();
        this.raycast = new RaycastSystem(config);
        this.projectile = new ProjectileSystem(config);
        this.death = new DeathSystem(plugin, this);
        this.gold = new GoldSystem(plugin, config);
        this.cooldowns = new CooldownSystem();
        this.bossBar = new BossBarSystem();
        this.mapper = new GoldSpawnMapper(plugin);
        this.quitTracker = new QuitTracker();
        this.spectatorMenu = new SpectatorMenu(this);
        this.trackerSystem = new TrackerSystem();
        this.votingSystem = new VotingSystem();
        this.votingMenu = new VotingMenu(this);
    }
    
    public int joinPlayer(Player player) {
        if (player == null || !player.isOnline()) return -1;

        if (players.contains(player)) {
            return -2;
        }

        if (players.size() >= config.getMaxPlayers()) {
            return -3;
        }

        if (state != GameState.LOBBY && state != GameState.COUNTDOWN) {
            joinAsSpectator(player);
            return 1;
        }
        
        players.add(player);
        roleManager.addPlayer(player);

        cleanupOfflinePlayers();

        refreshPlayerState(player);

        String message = config.getMessageNoPrefix("player-joined")
                        .replace("{name}", player.getName())
                        .replace("{playersNumber}", Integer.toString(players.size()))
                        .replace("{maxPlayers}", Integer.toString(config.getMaxPlayers()));
        
        for (Player p : players) {
            p.sendActionBar(TextFormat.colorize(message), 10, 60, 10);
        }

        int onlineCount = getOnlinePlayers().size();
        
        if (state == GameState.LOBBY && onlineCount >= config.getMinPlayers()) {
            startCountdown();
        } else if (state == GameState.COUNTDOWN 
                    && !countdownShortened 
                    && onlineCount >= config.getMinPlayersStart()) {
            shortenCountdown();
        }

        return 0;
    }

    private void playersRejoin() {
        List<Player> playersRejoining = new ArrayList<>(getOnlinePlayers());
        players.clear();

        for (Player p : playersRejoining) {
            if (p.isOnline()) {
                joinPlayer(p);
            }
        }
    }

    private void joinAsSpectator(Player player) {
        players.add(player);
        
        roleManager.addPlayer(player);
        GamePlayer gp = roleManager.getGamePlayer(player);
        if (gp != null) {
            gp.setAlive(false);
            gp.setRole(MMRole.SPECTATOR);
        }

        player.setGamemode(Player.ADVENTURE);
        player.setFlying(true);
        player.setAllowFlight(true);
        player.setDataFlag(EntityFlag.INVISIBLE, true);
        player.setDataFlag(EntityFlag.COLLIDABLE, false);
        player.fireProof = true;

        ItemManager.giveSpectatorItems(player, config.getSpectatorItemName());

        if (plugin.getConfig().getBoolean("world.arena-regions." + selectedMap + ".night-vision")) {      
            giveNightVision(player);
        }

        player.sendMessage(TextFormat.colorize(config.getMessage("spectator-in-game")));

        if (arena != null) {
            Vector3 spawn = arena.getSpawns().get(0);
            player.teleport(new Location(spawn.x, spawn.y, spawn.z, arena.getLevel()));
        }

        plugin.getServer().getScheduler().scheduleDelayedTask(plugin, () -> {
            for (Entity e : deadBodies) {
                DeadBodyEntity body = (DeadBodyEntity) e;
                body.playAnimation(body.getAnimation(), Collections.singleton(player));
            }
        }, 20);
    }

    public boolean leavePlayer(Player player) {
        if (player == null || !players.contains(player)) return false;

        players.remove(player);
        roleManager.removePlayer(player);
        refreshPlayerState(player, true);

        switch (state) {
            case LOBBY:
            case COUNTDOWN:
                String message = config.getMessageNoPrefix("player-left")
                                .replace("{name}", player.getName())
                                .replace("{playersNumber}", Integer.toString(players.size()))
                                .replace("{maxPlayers}", Integer.toString(config.getMaxPlayers()));
                
                for (Player p : players) {
                    p.sendActionBar(TextFormat.colorize(message), 10, 60, 10);
                }
                break;
            case PREGAME_COUNTDOWN:
                returnToLobby(player);
                break;
            case IN_GAME:
                returnToLobby(player);
                checkWinCondition();
                break;
            case ENDING:
                returnToLobby(player);
                break;
            }
        
        if (players.size() < config.getMinPlayers() && state == GameState.COUNTDOWN) {
            cancelCountdown();
        }

        return true;
    }
    
    public void forceStart() {
        if (timer != null) timer.stop();

        finalizeVoting();

        broadcast("§aGame start was forced by an op!");
        startGame();
    }

    private void startCountdown() {
        state = GameState.COUNTDOWN;

        refreshPlayersState();
        prepareMapVoting();
        
        int duration = config.getMaxCountdown();
        timer = new TimerSystem(plugin, duration);

        initialCountdown = duration;
        
        for (Player p : players) {
            String message = config.getCountdownBossbar().replace("{seconds}", String.valueOf(duration));
            bossBar.showCountdown(p, TextFormat.colorize(message));
        }
        
        timer.startCountdown(duration, this::startGame, () -> {
            int remaining = timer.getSecondsRemaining();

            for (Player p : players) {
                String message = formatCountdownMessage(remaining);
                bossBar.updateCountdown(p, message, remaining, initialCountdown);
            }

            if (remaining == config.getShortenedCountdown()) {
                countdownShortened = true;
                refreshPlayersState();
                finalizeVoting();
            }
        });
    }

    private void shortenCountdown() {
        if (countdownShortened) return;

        countdownShortened = true;
        refreshPlayersState();
        finalizeVoting();

        if (timer != null) timer.stop();

        int shortenedTime = config.getShortenedCountdown();
        initialCountdown = shortenedTime;
        timer = new TimerSystem(plugin, shortenedTime);

        String message = formatCountdownMessage(shortenedTime);
        for (Player p : players) {
            bossBar.updateCountdown(p, message, shortenedTime, initialCountdown);
        }
        
        timer.startCountdown(shortenedTime, this::startGame, () -> {
            int remaining = timer.getSecondsRemaining();

            String message2 = formatCountdownMessage(remaining);
            for (Player p : players) {
                bossBar.updateCountdown(p, message2, remaining, initialCountdown);
            }
        });

        String shortenedMsg = config.getMessage("timer-shortened")
                                    .replace("{seconds}", String.valueOf(shortenedTime));
        broadcast(shortenedMsg);
    }
    
    private void cancelCountdown() {
        if (timer != null) timer.stop();
        
        countdownShortened = false;

        state = GameState.LOBBY;
        refreshPlayersState();
    }
    
    private void startGame() {
        for (Player p : getOnlinePlayers()) {
            bossBar.hide(p);
        }

        cleanupOfflinePlayers();

        if (getOnlinePlayers().size() < config.getMinPlayers()) {
            state = GameState.LOBBY;
            countdownShortened = false;
            refreshPlayersState();

            String message = config.getMessage("not-enough-players").replace("{min}", String.valueOf(config.getMinPlayers()));
            broadcast(message);
            
            return;
        }

        if (selectedMap == null) {
            List<String> enabledMaps = config.getEnabledMaps();
            if (enabledMaps.isEmpty()) {
                plugin.getLogger().error("CRITICAL ERROR: No maps enabled in config!");
                broadcast(TextFormat.RED + "ERROR: no maps available!");
                returnToLobby();
                reset();
                return;
            }
            selectedMap = enabledMaps.get(new Random().nextInt(enabledMaps.size()));
            loadArena(selectedMap);
        }

        gold.cleanupGold(arena.getLevel());
        gold.loadSpawns(mapper, selectedMap);

        startScoreboardUpdates();
        teleportPlayers();

        broadcast(config.getMessage("teleported-to-arena"));

        startPreGameCountdown();
    }

    private void startPreGameCountdown() {
        state = GameState.PREGAME_COUNTDOWN;

        int pregameDuration = config.getPregameCountdown();
        timer = new TimerSystem(plugin, pregameDuration);

        refreshPlayersState();
        
        for (Player p : players) {
            if (plugin.getConfig().getBoolean("world.arena-regions." + selectedMap + ".night-vision")) {
                giveNightVision(p);
            }

            bossBar.showExp(p, 0);
        }

        List<String> builders = config.getMapBuilders(selectedMap);
        if (!builders.isEmpty()) {
            String buildersStr = String.join(" &7/ &d", builders);
            String creditsMsg = config.getMessage("map-credits")
                                        .replace("{builders}", buildersStr);
            broadcast(creditsMsg);
        }
    
        timer.startCountdown(pregameDuration, this::startInGameState, () -> {
            int remaining = timer.getSecondsRemaining() - 1;

            for (Player p : players) {
                switch (remaining) {
                    case 3:
                        p.sendTitle(TextFormat.colorize("&l&a3"), "", 4, 17, 4);
                        break;
                    case 2:
                        p.sendTitle(TextFormat.colorize("&l&62"), "", 4, 17, 4);
                        break;
                    case 1:
                        p.sendTitle(TextFormat.colorize("&l&c1"), "", 4, 17, 4);
                        break;
                    default:
                        break;
                }
            }
        });
    }

    private void startInGameState() {
        state = GameState.IN_GAME;

        for (Player p : getOnlinePlayers()) {
            bossBar.hide(p);
        }

        roleManager.assignRoles(getOnlinePlayers());
        giveItems();

        broadcast(config.getMessage("game-start"));
        broadcast(config.getMessage("game-start2"));

        for (GamePlayer gp : roleManager.getOnlinePlayers()) {
            Player player = gp.getPlayer();
            if (!player.isOnline()) continue;

            switch (gp.getRole()) {
                case INNOCENT:
                    bossBar.showExpAndGold(player, 0, 0);
                    player.sendTitle(
                        TextFormat.colorize(config.getMessageNoPrefix("innocent-title")), 
                        TextFormat.colorize(config.getMessageNoPrefix("innocent-subtitle")),
                        10, 60, 10
                    );
                    break;
                case SHERIFF:
                    bossBar.showExp(player, 0);
                    player.sendTitle(
                        TextFormat.colorize(config.getMessageNoPrefix("sheriff-title")), 
                        TextFormat.colorize(config.getMessageNoPrefix("sheriff-subtitle")),
                        10, 60, 10
                    );
                    player.sendMessage(TextFormat.colorize(config.getMessage("sheriff-advice")));
                    break;
                case MURDERER:
                    bossBar.showExp(player, 0);
                    player.sendTitle(
                        TextFormat.colorize(config.getMessageNoPrefix("murderer-title")), 
                        TextFormat.colorize(config.getMessageNoPrefix("murderer-subtitle")),
                        10, 60, 10
                    );
                    player.sendMessage(TextFormat.colorize(config.getMessage("murderer-advice")));
                    break;
                case SPECTATOR:
                    break;
            }
        }
        
        timer = new TimerSystem(plugin, config.getGameDuration());
        timer.startGame(config.getGameDuration(), this::onTimeExpired);
        
        gold.startSpawning(arena);
        
        if(checkEnoughPlayers) {
            plugin.getServer().getScheduler().scheduleDelayedTask(plugin, () -> {
                checkWinCondition();
            }, 20);
        }
        
    }

    private void prepareMapVoting() {
        if (!votingSystem.getAvailableMaps().isEmpty()) return;
        
        List<String> allMaps = config.getEnabledMaps();
        List<String> votingMaps = new ArrayList<>();
        
        if (allMaps.size() <= 3) {
            votingMaps.addAll(allMaps);
        } else {
            List<String> shuffled = new ArrayList<>(allMaps);
            Collections.shuffle(shuffled);
            votingMaps.addAll(shuffled.subList(0, 3));
        }
        votingSystem.setAvailableMaps(votingMaps);
    }

    private void finalizeVoting() {
        if (selectedMap != null) return;

        selectedMap = votingSystem.getMostVotedMap();
        if (selectedMap == null) {
            List<String> enabledMaps = config.getEnabledMaps();
            if (!enabledMaps.isEmpty()) {
                selectedMap = enabledMaps.get(new Random().nextInt(enabledMaps.size()));
                plugin.getLogger().warning("No vote. Fallback on random map: " + selectedMap);
            } else {
                plugin.getLogger().error("CRITIC ERROR: No map enabled in config!");
                broadcast("§cError: no map available. Match cancelled.");
                forceStop();
                return;
            }
        }

        selectedTime = votingSystem.getMostVotedTime(config.getAvailableTimes());
        if (selectedTime == null) {
            selectedTime = config.getAvailableTimes().isEmpty() ? "day" : config.getAvailableTimes().get(0);
        }

        String selectedMapMsg = config.getMessage("selected-map")
                .replace("{selectedMap}", config.getMapDisplayName(selectedMap))
                .replace("{selectedTime}", selectedTime)
                .replace("{weather}", config.getMapWeather(selectedMap));
        
        broadcast(selectedMapMsg);

        loadArena(selectedMap);
    }

    private String formatCountdownMessage(int seconds) {
        if (seconds <= 10) {
            return TextFormat.colorize(
                config.getCountdownBossbarShort()
                    .replace("{seconds}", String.valueOf(seconds))
            );
        } else if (seconds < 60) {
            return TextFormat.colorize(
                config.getCountdownBossbarMedium()
                    .replace("{seconds}", String.valueOf(seconds))
            );
        } else {
            int minutes = seconds / 60;
            int secs = seconds % 60;
            return TextFormat.colorize(
                config.getCountdownBossbarLong()
                    .replace("{minutes}", String.valueOf(minutes))
                    .replace("{seconds}", String.valueOf(secs))
            );
        }
    }

    private void giveNightVision(Player player) {
        Effect nightVision = Effect.get(EffectType.NIGHT_VISION);
        nightVision.setDuration(9999);
        nightVision.setAmplifier(0);
        nightVision.setVisible(false);
        player.addEffect(nightVision);
    }

    private void loadArena(String selectedMap) {

        int X = 0;
        int Y = 1;
        int Z = 2;
        Config pConfig = plugin.getConfig();
        String path = "world.arena-regions." + selectedMap + ".";

        String worldName;

        if (pConfig.exists(path + "world")) {
            worldName = pConfig.getString(path + "world");
        } else {
            worldName = pConfig.getString("world.default-world");
        }
        
        Level level = plugin.getServer().getLevelByName(worldName);
        
        //min & max coords
        String rawMinCoords = pConfig.getString(path + "min");
        String rawMaxCoords = pConfig.getString(path + "max");

        Vector3 min = new Vector3(
            getCoordinate(rawMinCoords, X),
            getCoordinate(rawMinCoords, Y),
            getCoordinate(rawMinCoords, Z)
        );
        Vector3 max = new Vector3(
            getCoordinate(rawMaxCoords, X),
            getCoordinate(rawMaxCoords, Y),
            getCoordinate(rawMaxCoords, Z)
        );

        //spawns
        List<String> spawnsRawList = plugin.getConfig().getStringList(path + "spawns");
        List<Vector3> spawns = new ArrayList<>();

        for (String coords : spawnsRawList) {
            spawns.add(new Vector3(
                getCoordinate(coords, X),
                getCoordinate(coords, Y),
                getCoordinate(coords, Z)
            ));
        }

        if (level != null) {
            //time
            if (selectedTime != null) {
                int timeValue;
                switch (selectedTime.toLowerCase()) {
                    case "day":
                        timeValue = 6000;
                        break;
                    case "night": 
                        timeValue = 18000; 
                        break;
                    case "sunset": 
                        timeValue = 12000; 
                        break;
                    case "midnight": 
                        timeValue = 20000; 
                        break;
                    default:
                        timeValue = 6000;
                        break;
                }
                level.setTime(timeValue);
            }

            //weather
            switch (config.getMapWeather(selectedMap).toLowerCase()) {
                case "clear":
                    level.setRaining(false);
                    level.setThundering(false);
                    break;
                case "rain":
                    level.setRaining(true);
                    level.setThundering(false);
                    break;
                case "storm":
                    level.setRaining(true);
                    level.setThundering(false);
                    break;
                default:
                    level.setRaining(false);
                    level.setThundering(false);
                    break;
            }
        }

        
        arena = new Arena(pConfig.getString(path + "name"), level, min, max, spawns);
    }

    private double getCoordinate(String rawCoords, int coord) {
        return Double.parseDouble(
            rawCoords.split("\\s+") [coord]
        );
    }

    private void teleportPlayers() {
        List<Vector3> spawns = arena.getSpawns();
        List<Player> onlinePlayers = getOnlinePlayers();

        if (spawns.isEmpty()) {
            forceStop();
            broadcast("&cError: no spawns available!");
            return;
        }
        
        for (int i = 0; i < onlinePlayers.size(); i++) {
            Player player = onlinePlayers.get(i);

            Vector3 spawn = spawns.get(i % spawns.size());
            Location loc = new Location(spawn.x, spawn.y, spawn.z, arena.getLevel());
            
            try {
                player.teleport(loc);
            } catch (Exception e) {
                plugin.getLogger().error("Error teleporting player: " + e.getMessage());
            }
        }
    }

    private void giveItems() {
        for (GamePlayer gp : roleManager.getAllPlayers()) {
            Player p = gp.getPlayer();
            if (!p.isOnline()) continue;
            
            try {
                switch (gp.getRole()) {
                    case MURDERER:
                        ItemManager.giveMurdererItems(p, config.getMurdererSwordName(), config.getMurdererBlazeRodName());
                        break;
                    case SHERIFF:
                        ItemManager.giveSheriffItems(p, config.getSheriffHoeName());
                        break;
                    case INNOCENT:
                        break;
                    case SPECTATOR:
                        break;
                }
            } catch (Exception e) {
                plugin.getLogger().error("Error giving items to player: " + e.getMessage());
            }
        }
    }
    
    private void onTimeExpired() {
        endGame(false);
    }
    
    public void checkWinCondition() {
        if (state != GameState.IN_GAME) return;
        
        cleanupOfflinePlayers();

        if (roleManager.isMurdererDead()) {
            endGame(false);
        } else if (roleManager.allInnocentsDead()) {
            endGame(true);
        }
    }

    private void endGame(boolean murdererWin) {
        state = GameState.ENDING;
        
        if (timer != null) timer.stop();
        
        gold.stop();

        if (!murdererWin) {
            GamePlayer sheriffGp = roleManager.getSheriff();
            if (sheriffGp != null && sheriffGp.isAlive() && sheriffGp.getPlayer().isOnline()) {
                int bonus = config.getExpSheriffWin();
                sheriffGp.addExp(bonus);
            }

            broadcast(config.getMessage("murderer-dead"));
        }
        
        String titleMsg = murdererWin ? 
            config.getMessageNoPrefix("murderer-won-title") : 
            config.getMessageNoPrefix("innocents-won-title");
        
        for (Player p : getOnlinePlayers()) {
            p.sendTitle(TextFormat.colorize(titleMsg), "",
                        10, 60, 10);
        }
        
        String chatMsg = murdererWin ? 
            config.getMessage("murderer-won") : 
            config.getMessage("innocents-won");
        String chatMsg2 = murdererWin ? 
            config.getMessage("murderer-won2") : 
            config.getMessage("innocents-won2");

        broadcast(chatMsg, true);
        broadcast(chatMsg2, false);

        plugin.getServer().getScheduler().scheduleDelayedTask(plugin, () -> {
            gold.cleanupGold(arena.getLevel());
            death.cleanupSheriffHoe(arena.getLevel());
            death.cleanupBodies(deadBodies);
            death.cleanupRedstone(redstonePositions);
            returnToLobby();
            reset();
            playersRejoin();
        }, config.getEndTime() * 20);
    }

    public void forceStop() {
        if (state == GameState.IN_GAME 
            || state == GameState.COUNTDOWN 
            || state == GameState.PREGAME_COUNTDOWN) {

            state = GameState.ENDING;

            if (timer != null) timer.stop();
            gold.stop();

            if (arena != null) {
                gold.cleanupGold(arena.getLevel());
                death.cleanupSheriffHoe(arena.getLevel());
            }
            death.cleanupBodies(deadBodies);
            death.cleanupRedstone(redstonePositions);
            returnToLobby();
            reset();

            players.clear();

            for (Player p : players) {
                ItemManager.clearInventory(p);
            }
        }
    }

    private void returnToLobby() {
        for (Player p : players) {
            returnToLobby(p);
        }
    }

    public void returnToLobby(Player player) {
        if (player == null || !player.isOnline()) return;

        try {
            Level lobby = plugin.getServer().getLevelByName(config.getLobbyWorld());
            if (lobby == null) {
                plugin.getLogger().warning("Lobby world not found: " + config.getLobbyWorld());
                return;
            }
            
            Vector3 spawnPos = config.getLobbySpawn();
            Location lobbySpawn = new Location(spawnPos.x, spawnPos.y, spawnPos.z, lobby);
            
            ItemManager.clearInventory(player);

            player.setMotion(new Vector3(0, 0, 0));
            player.setFlying(false);
            player.setAllowFlight(false);
            player.setMotion(new Vector3(0, 0, 0));

            player.teleport(lobbySpawn);

        } catch (Exception e) {
            plugin.getLogger().error("Error returning player to lobby: " + e.getMessage());
        }
    }
        
    private void reset() {
        stopScoreboardUpdates();

        roleManager.clear();
        cooldowns.clear();
        bossBar.clear(players);
        quitTracker.clear();
        votingSystem.clear();

        state = GameState.LOBBY;
        selectedMap = null;
        selectedTime = null;
        countdownShortened = false;
        firstKill = true;
        redstonePositions.clear();
        deadBodies.clear();

        refreshPlayersState();

        for (Player p : players) {
            ItemManager.clearInventory(p);
        }
    }

    private void cleanupOfflinePlayers() {
        players.removeIf(player -> !player.isOnline());
        
        for (String playerName : new ArrayList<>(roleManager.getAllPlayers().stream()
                .map(gp -> gp.getPlayer().getName())
                .toList())) {
            
            Player p = plugin.getServer().getPlayer(playerName);
            if (p == null || !p.isOnline()) {
                roleManager.removePlayer(plugin.getServer().getOfflinePlayer(playerName));
            }
        }
    }

    private void refreshPlayersState() {
        for (Player p : players) {
            refreshPlayerState(p);
        }
    }

    private void refreshPlayerState(Player player) {
        refreshPlayerState(player, false);
    }

    public void refreshPlayerState(Player p, Boolean leaving) {
        if (p == null) return;
        
        if (!p.isOnline()) {
            scoreboard.remove(p);
            return;
        }

        ItemManager.clearInventory(p);
        
        p.setGamemode(Player.ADVENTURE);

        p.setDataFlag(EntityFlag.INVISIBLE, false);
        p.setDataFlag(EntityFlag.COLLIDABLE, false);
        p.fireProof = false;
        p.removeAllEffects();

        if (leaving) {
            ItemManager.clearInventory(p);
            setNameTagVisible(p);

            switch (state) {
                case LOBBY:
                    break;
                case COUNTDOWN:
                    bossBar.hide(p);
                    break;
                case PREGAME_COUNTDOWN:
                case IN_GAME:
                case ENDING:
                    bossBar.hide(p);
                    scoreboard.remove(p);
                    break;
            }
            
        } else {
            switch (state) {
                case LOBBY:
                    setNameTagVisible(p);

                    scoreboard.remove(p);
                    bossBar.hide(p);
                    ItemManager.giveLobbyItems(
                        p, 
                        config.getRulesItemName(), 
                        plugin
                    );
                    break;
                case COUNTDOWN:
                    setNameTagVisible(p);

                    if (!countdownShortened) {
                        ItemManager.giveLobbyItems(
                            p, 
                            config.getRulesItemName(), 
                            config.getGamePollItemName(), 
                            plugin
                        );
                    } else {
                        ItemManager.giveLobbyItems(
                            p, 
                            config.getRulesItemName(), 
                            plugin
                        );
                    }
                    break;
                case PREGAME_COUNTDOWN:
                case IN_GAME:
                case ENDING:
                    setNameTagsInvisible(p);

                    scoreboard.remove(p);
                    bossBar.hide(p);
                    break;
            }
        }
    }
    
    private void startScoreboardUpdates() {
        if (updateTask != null) return;

        updateTask = new Task() {
            @Override
            public void onRun(int currentTick) {
                updateScoreboards();
            }
        };
        
        plugin.getServer().getScheduler().scheduleRepeatingTask(plugin, updateTask, 10);
    }
    
    private void stopScoreboardUpdates() {
        if (updateTask != null) {
            updateTask.cancel();
            updateTask = null;
        }
    }

    private void updateScoreboards() {
        if (timer == null) return;
        
        String timeStr = timer.getFormattedTime();
        int remainingSeconds = timer.getSecondsRemaining();

        if (state == GameState.PREGAME_COUNTDOWN) {
            for (Player p : getOnlinePlayers()) {
                scoreboard.update(p, timeStr, 0, false, MMRole.INNOCENT, true);
            }
            return;
        } else if (state == GameState.IN_GAME) {
            int innocents = roleManager.getAliveInnocentsCount();
            boolean sheriffAlive = !roleManager.isSheriffDead();
            int trackThreshold = config.getMurdererTrackThreshold();

            GamePlayer murdererGp = roleManager.getMurderer();
            boolean trackingActive = (murdererGp != null
                                    && murdererGp.getPlayer().isOnline()
                                    && murdererGp.isAlive()
                                    && remainingSeconds <= trackThreshold);

            for (GamePlayer gp : roleManager.getAllPlayers()) {
                Player p = gp.getPlayer();
                if (!p.isOnline()) continue;

                scoreboard.update(p, timeStr, innocents, sheriffAlive, gp.getRole(), false);
                updatePlayerBossBar(p, gp, trackingActive);
            }
        }
    }

    private void updatePlayerBossBar(Player p, GamePlayer gp, boolean trackingActive) {
        switch (gp.getRole()) {
            case MURDERER:
                if (trackingActive) {
                    double dist = trackerSystem.getNearestDistance(p, roleManager.getAllPlayers());
                    bossBar.updateExpWithDistance(p, gp.getExpEarned(), dist);
                } else {
                    bossBar.updateExp(p, gp.getExpEarned());
                }
                break;
                
            case INNOCENT:
                bossBar.updateExpAndGold(p, gp.getGoldCollected(), gp.getExpEarned());
                break;
                
            case SHERIFF:
                bossBar.updateExp(p, gp.getExpEarned());
                break;
                
            case SPECTATOR:
                bossBar.updateExp(p, gp.getExpEarned());
                break;
        }
    }

    private void broadcast(String message) {
        for (Player p : getOnlinePlayers()) {
            p.sendMessage(TextFormat.colorize(message));
        }
    }

    private void broadcast(String message, Boolean spectatorsIncluded) {
        for (Player p : getOnlinePlayers()) {
            GamePlayer gp = roleManager.getGamePlayer(p);
            if (gp == null) continue;
            
            if (spectatorsIncluded) {
                p.sendMessage(TextFormat.colorize(message));
            } else if (gp.getRole() != MMRole.SPECTATOR) {
                p.sendMessage(TextFormat.colorize(message));
            }
        }
    }

    public boolean isFirstKill() {
        if (firstKill) {
            firstKill = false;
            return true;
        }
        return false;
    }

    public void setNameTagsInvisible(Player player) {
        player.setNameTag("");
    }

    public void setNameTagVisible(Player player) {
        player.setNameTag(player.getName());
    }

    public void addTrackedRedstone(Position pos) {
        redstonePositions.add(pos);
    }

    public List<Player> getOnlinePlayers() {
        return players.stream()
            .filter(Player::isOnline)
            .toList();
    }
    
    public GameState getState() {
        return state;
    }
    
    public MMRoleManager getRoleManager() {
        return roleManager;
    }
    
    public MMConfig getConfig() {
        return config;
    }
    
    public RaycastSystem getRaycast() {
        return raycast;
    }
    
    public ProjectileSystem getProjectile() {
        return projectile;
    }
    
    public DeathSystem getDeath() {
        return death;
    }
    
    public CooldownSystem getCooldowns() {
        return cooldowns;
    }
    
    public List<Player> getPlayers() {
        return new ArrayList<>(players);
    }

    public BossBarSystem getBossBar() {
        return bossBar;
    }

    public GoldSpawnMapper getMapper() {
        return mapper;
    }

    public QuitTracker getQuitTracker() {
        return quitTracker;
    }

    public SpectatorMenu getSpectatorMenu() {
        return spectatorMenu;
    }

    public Arena getArena() {
        return arena;
    }

    public Set<Entity> getDeadBodies() {
        return deadBodies;
    }

    public MinigameCore getPlugin() {
        return plugin;
    }

    public VotingSystem getVotingSystem() {
        return votingSystem;
    }

    public VotingMenu getVotingMenu() {
        return votingMenu;
    }
}