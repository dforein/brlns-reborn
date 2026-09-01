package org.brlnsreb.minigames.mm.match.game;

import java.util.*;

import org.brlnsreb.BrlnsReb;
import org.brlnsreb.core.maps.GameMapLevel;
import org.brlnsreb.core.maps.RandomSpawnsMap;
import org.brlnsreb.core.minigame.match.MatchExpand;
import org.brlnsreb.core.minigame.match.game.GameExpand;
import org.brlnsreb.core.minigame.match.game.GameTeam;
import org.brlnsreb.core.minigame.match.game.listeners.ListenerAccess;
import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.core.player.PlayerUtils;
import org.brlnsreb.core.player.CustomPlayer.DamageMode;
import org.brlnsreb.core.player.CustomPlayer.InteractMode;
import org.brlnsreb.core.player.data.StatType;
import org.brlnsreb.minigames.mm.match.game.entities.DeadBodyEntity;
import org.brlnsreb.minigames.mm.match.game.gamedata.MMPlayerGameData;
import org.brlnsreb.minigames.mm.match.game.gamedata.MMRole;
import org.brlnsreb.minigames.mm.match.game.gamedata.MMPlayerGameData.MMEvent;
import org.brlnsreb.minigames.mm.match.game.items.MMItemManager;
import org.brlnsreb.minigames.mm.match.game.listeners.MMListenerAccess;
import org.brlnsreb.minigames.mm.match.game.systems.DeathSystem;
import org.brlnsreb.minigames.mm.match.game.systems.GoldSystem;
import org.brlnsreb.minigames.mm.match.game.systems.ProjectileSystem;
import org.brlnsreb.minigames.mm.match.game.systems.RaycastSystem;
import org.brlnsreb.minigames.mm.match.game.teams.MMTeamManager;
import org.brlnsreb.minigames.mm.match.game.ui.MMBossBar;
import org.brlnsreb.minigames.mm.match.game.ui.MMScoreboard;
import org.brlnsreb.minigames.mm.match.game.ui.MMSpectatorMenu;
import org.brlnsreb.utils.SoundUtil;
import org.brlnsreb.utils.TimerSystem;
import org.brlnsreb.utils.config.YamlUtil;
import org.brlnsreb.utils.level.TimeOfDay;
import org.brlnsreb.utils.level.Weather;
import org.powernukkitx.Player;
import org.powernukkitx.entity.Entity;
import org.powernukkitx.event.entity.EntityDamageEvent.DamageCause;
import org.powernukkitx.level.Location;
import org.powernukkitx.level.Position;
import org.powernukkitx.level.Sound;
import org.powernukkitx.scheduler.Task;

public class MMGame extends GameExpand implements GameTeam {

    private final MMTeamManager teams;
    private final Map<CustomPlayer, MMPlayerGameData> gameDataMap;

    private final MMBossBar bossBar;
    private final MMScoreboard scoreboard;
    private final MMItemManager items;
    private final MMSpectatorMenu spectatorMenu;

    private Task updateUiTask;
    private Task checkPosTask;
    private final GoldSystem gold;
    private final DeathSystem death;
    private final ProjectileSystem projectile;
    private final RaycastSystem raycast;

    private CustomPlayer murderer;
    private boolean firstKill;

    private CustomPlayer sheriff;
    private static boolean friendlyFireDeath;
    private final Object lock = new Object();

    private boolean murdererWin;

    private final Set<Entity> deadBodies = new HashSet<>();

    public MMGame(MatchExpand match, String mapId, TimeOfDay time, Weather weather) {
        super(match, mapId, time, weather);

        this.teams = new MMTeamManager(this);
        this.gameDataMap = new HashMap<>();

        this.bossBar = new MMBossBar(this);
        this.scoreboard = new MMScoreboard(this);
        this.items = new MMItemManager(this);
        this.spectatorMenu = new MMSpectatorMenu(this);

        this.gold = new GoldSystem(config, map);
        this.death = new DeathSystem(this, scheduler);
        this.projectile = new ProjectileSystem(this);
        this.raycast = new RaycastSystem(this, scheduler);

        this.firstKill = true;
        friendlyFireDeath = config.getBoolean("game.items.hoe.friendly-fire-death");

        MMPlayerGameData.setExpPrizes(config);
        MMPlayerGameData.setCoinsPrizes(config);
    }

    protected GameMapLevel prepareMap(String mapId, TimeOfDay time, Weather weather) {
        return new RandomSpawnsMap(minigame, mapId, time, weather);
    }

    protected ListenerAccess createListenerAccess() {
        return new MMListenerAccess(this);
    }


    //join-leave logic

    protected Location onJoinLocation(CustomPlayer player) {
        return map.getSpawnFor(player);
    }

    protected void prepareGameData(CustomPlayer player) {
        gameDataMap.put(player, new MMPlayerGameData(player));
    }

    protected void onJoinPreparePlayer(CustomPlayer player) {
        player.interactMode = InteractMode.LIMITED;

        if (map.isNightVisionEnabled()) PlayerUtils.giveNightVision(player);

        bossBar.updateExp(player, gameDataMap.get(player));
    }

    protected void setPregameNameTag(CustomPlayer player) {
        player.ingameChatNameTag = player.grayNameTag;
    }

    public void onJoinPrepareSpectator(CustomPlayer player) {
        player.setAllowFlight(true);
        player.setFlying(true);
        if (map.isNightVisionEnabled()) PlayerUtils.giveNightVision(player);

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
        if (!player.isPlaying() || !this.isInGame()) return;

        roleCheckOnLeave(player, player.getPosition());
        checkWinConditions();
    }

    private void roleCheckOnLeave(CustomPlayer player, Position lastPos) {
        MMPlayerGameData gameData = gameDataMap.remove(player);
        if (gameData == null) return;
        if (gameData.role == MMRole.MURDERER) {
            murderer = null;
        } else if (gameData.role == MMRole.SHERIFF) {
            sheriff = null;

            death.dropSheriffHoe(lastPos);
            items.giveYellowDye();

            msgUtil.broadcastPresetPrefix("sheriff-gun-dropped");
            msgUtil.broadcastPresetPrefix("sheriff-dead-instructions");
        }
    }


    //<GAME LIFECYCLE>

    //pregame

    protected void prepareGame() {
        for (CustomPlayer p : players) {
            bossBar.updateExp(p, gameDataMap.get(p));
        }

        //load gold spawns
        gold.loadSpawns();

        //check player position (the player must be inside the map!)
        checkPosTask = new Task() {
            @Override
            public void onRun(int currentTick) {
                List<CustomPlayer> outOfBounds = null;
                for (CustomPlayer p : players) {
                    if (!map.isInMap(p)) {
                        if (outOfBounds == null) outOfBounds = new ArrayList<>();
                        outOfBounds.add(p);
                    }
                }
                if (outOfBounds == null) return;

                for (CustomPlayer p : outOfBounds) {
                    if (isInGame()) match.onDeath(p, null);
                    else p.teleport(onJoinLocation(p));
                }
            }
        };
        scheduler.scheduleRepeatingTask(BrlnsReb.instance, checkPosTask, 10);
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
        //message
        msgUtil.broadcastPresetPrefix(players, "game-start");
        msgUtil.broadcastPresetPrefix(players, "game-start2");

        //role assignment, items
        List<CustomPlayer> shuffledPlayers = new ArrayList<>(players);
        Collections.shuffle(shuffledPlayers);

        for (int i = 0; i < shuffledPlayers.size(); i++) {
            if (!players.contains(shuffledPlayers.get(i))) {     //in case of disconnection
                shuffledPlayers.remove(i--);
                continue;
            }

            MMRole role = switch (i) {
                case 0 -> MMRole.MURDERER;
                case 1 -> MMRole.SHERIFF;
                default -> MMRole.INNOCENT;
            };
            setRole(shuffledPlayers.get(i), role);
        }

        //timer start
        timer = new TimerSystem();
        timer.start(config.getInt("game.duration"), this::onTimeOut);

        //ui
        updateUiTask = new Task() {
            @Override
            public void onRun(int currentTick) {
                scoreboard.updateGameScoreboards();
                bossBar.updateGameBossBars();
            }
        };
        scheduler.scheduleRepeatingTask(BrlnsReb.instance, updateUiTask, config.getInt("game.ui-update-ticks"));

        //spawn gold
        gold.startSpawning();

        //player check at game start
        if (config.getBoolean("settings.check-players-at-game-start") && players.size() != 2) {     //2 players (sheriff and murderer) are an exception
            scheduler.scheduleDelayedTask(BrlnsReb.instance, this::checkWinConditions, 20);      //end the game directly if the players aren't enough
        }
    }

    private void setRole(CustomPlayer player, MMRole role) {
        MMPlayerGameData gameData = gameDataMap.get(player);

        gameData.role = role;
        items.giveItemsByRole(player, gameData);

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
            YamlUtil.getStr(titlePath, messages),
            YamlUtil.getStr(subtitlePath, messages),
            10, 60, 10
        );

        //save key player and send advice
        switch (role) {
            case MURDERER -> {
                murderer = player;
                msgUtil.sendPresetMessagePrefix(player, "murderer-advice");
            }

            case SHERIFF -> {
                sheriff = player;
                msgUtil.sendPresetMessagePrefix(player, "sheriff-advice");
            }
        
            default -> {}
        }
    }

    
    //death

    public boolean onDeath(DamageCause cause, CustomPlayer victim, CustomPlayer killer) {
        gameDataMap.get(victim).incrementStat(StatType.DEATHS);
        gameDataMap.get(victim).incrementStat(StatType.LOSSES);
        return true;
    }

    public void afterDeath(DamageCause cause, Location deathLoc, CustomPlayer victim, CustomPlayer killer) {
        death.onDeath(victim, deathLoc);

        if (!this.isInGame()) return;

        MMPlayerGameData gameData = gameDataMap.get(killer);

        if (isMurdererAlive() && killer == murderer) {
            if (isFirstKill()) {
                msgUtil.sendPresetMessagePrefix(murderer, "murderer-warning");
            }

            if (victim == sheriff) {
                murdererKillsSheriff(gameData);
            } else {
                murdererKillsInnocent(gameData, victim.data.name);
            }

        } else if (isSheriffAlive() && killer == sheriff) {
            if (victim == murderer) {
                sheriffKillsMurderer(gameData);
            } else {
                sheriffKillsInnocent();
            }
        }

        roleCheckOnLeave(victim, deathLoc);
        checkWinConditions();
    }

    private boolean isFirstKill() {
        if (firstKill) {
            firstKill = false;
            return true;
        }
        return false;
    }

    private void murdererKillsSheriff(MMPlayerGameData murdererGameData) {
        murdererGameData.incrementStat(StatType.KILLS);
        murdererGameData.addExp(MMEvent.MURDERER_KILLS_SHERIFF);
        murdererGameData.addCoins(MMEvent.MURDERER_KILLS_SHERIFF);

        msgUtil.sendPresetMessagePrefix(
            murderer, 
            "murderer-kills-sheriff", 
            new Integer[] { 
                MMPlayerGameData.getExpPrize(MMEvent.MURDERER_KILLS_SHERIFF), 
                MMPlayerGameData.getCoinsPrize(MMEvent.MURDERER_KILLS_SHERIFF), 
            }
        );
        msgUtil.broadcastPresetPrefix(
            "kill", 
            new String[] { 
                "§eThe Murderer", 
                "§cthe §9Sheriff" 
            }
        );
    }

    private void murdererKillsInnocent(MMPlayerGameData murdererGameData, String victimName) {
        murdererGameData.incrementStat(StatType.KILLS);
        murdererGameData.addExp(MMEvent.KILL);
        murdererGameData.addCoins(MMEvent.KILL);

        msgUtil.sendPresetMessagePrefix(
            murderer, 
            "murderer-kills-player",
            new Object[] {
                victimName,
                MMPlayerGameData.getExpPrize(MMEvent.KILL), 
                MMPlayerGameData.getCoinsPrize(MMEvent.KILL), 
            }
        );
        msgUtil.broadcastPresetPrefix(
            "kill", 
            new String[] { 
                "§eThe Murderer", 
                victimName
            }
        );
    }

    private void sheriffKillsMurderer(MMPlayerGameData sheriffGameData) {
        sheriffGameData.incrementStat(StatType.KILLS);
        sheriffGameData.addExp(MMEvent.SHERIFF_KILLS_MURDERER);
        sheriffGameData.addCoins(MMEvent.SHERIFF_KILLS_MURDERER);

        msgUtil.broadcastPresetPrefix(
            "kill", 
            new String[] { 
                "§cThe §9Sheriff", 
                "§eThe Murderer" 
            }
        );
        msgUtil.sendPresetMessagePrefix(
            sheriff, 
            "sheriff-kills-murderer",
            new Integer[] { 
                MMPlayerGameData.getExpPrize(MMEvent.SHERIFF_KILLS_MURDERER), 
                MMPlayerGameData.getCoinsPrize(MMEvent.SHERIFF_KILLS_MURDERER), 
            }
        );
    }

    private void sheriffKillsInnocent() {
        if (friendlyFireDeath) {
            msgUtil.broadcastPresetPrefix("sheriff-friendly-fire-death");
            scheduler.scheduleTask(BrlnsReb.instance, 
                () -> getMatch().onDeath(sheriff, null)
            );
        } else {
            msgUtil.broadcastPresetPrefix("sheriff-friendly-fire");
        }
    }


    //features

    public boolean collectGold(CustomPlayer player) {
        MMPlayerGameData gameData = getGameData(player);
        if (gameData.role != MMRole.INNOCENT) return false;

        gameData.gold++;
        gameData.addExp(MMEvent.GOLD);

        msgUtil.sendPresetMessagePrefix(player, "gold-collected");
        return true;
    }

    public CustomPlayer shoot(CustomPlayer shooter) {
        return raycast.shoot(shooter);
    }

    public void useFlash(CustomPlayer player) {
        MMPlayerGameData gameData = gameDataMap.get(player);
        if (gameData.flashUsed) return;

        gameData.flashUsed = true;
        items.useFlash(player, scheduler);
    }

    public void throwSword(CustomPlayer player) {
        if (!projectile.throwSword(player)) {
            int cooldown = projectile.getCooldownSeconds(player);
            if (cooldown < 0) cooldown = 0; 
            msgUtil.sendPresetMessagePrefix(player, "sword-cooldown", new Integer[] { cooldown });
        }
    }

    public boolean newSheriff(CustomPlayer player, boolean checkGold) {
        if (!this.isInGame()) return false;
        if (player == murderer || player == sheriff) return false;
        MMPlayerGameData gameData = gameDataMap.get(player);

        if (checkGold && !gameData.hasEnoughGoldForSheriff()) {
            msgUtil.sendPresetMessagePrefix(player, "not-enough-gold", new Integer[] { gameData.gold });
            return false;
        }

        synchronized (lock) {
            if (isSheriffAlive()) return true;
            sheriff = player;
        }
        
        setRole(sheriff, MMRole.SHERIFF);
        
        death.cleanupSheriffHoe();
        items.removeYellowDye();

        bossBar.updateGameBossBars();
        scoreboard.updateGameScoreboards();

        msgUtil.broadcastPresetPrefix("new-sheriff-chosen");
        return true;
    }


    //check win conditions

    public boolean checkWinConditions() {
        if (!this.isInGame()) return false;
        if (players.size() == 0) forceStop();

        if (!isMurdererAlive()) {
            murdererWin = false;
            onGameEnding();
            return true;

        } else if (players.size() <= 1 && isMurdererAlive()) {
            murdererWin = true;
            onGameEnding();
            return true;

        } else if (players.size() <= 2 && isMurdererAlive() && isSheriffAlive()) {
            murdererWin = true;
            onGameEnding();
            return true;
        }

        return false;
    }

    private void onTimeOut() {
        murdererWin = false;
        onGameEnding();
    }


    //ending

    public void endGame() {
        stopGame();

        if (isMurdererAlive())  murderer.canAttackPlayers = false;
        if (isSheriffAlive())   sheriff.canAttackPlayers = false;
        
        for (CustomPlayer p : players) {
            if (murdererWin && isSheriffAlive()) {
                if (p == murderer) gameDataMap.get(p).incrementStat(StatType.WINS);
                if (p == sheriff) gameDataMap.get(p).incrementStat(StatType.LOSSES);
            } else {
                gameDataMap.get(p).incrementStat(StatType.WINS);
            }
        }

        if (!murdererWin) msgUtil.broadcastPresetPrefix("murderer-dead");

        msgUtil.broadcastPresetPrefix(murdererWin ? "murderer-won" : "innocents-won");
        msgUtil.broadcastPresetPrefix(players, "congratulations");
        
        msgUtil.sendTitle(murdererWin ? "title.murderer-won" : "title.innocents-won", null);

        SoundUtil.sendSoundTo(players, Sound.RANDOM_CLICK.getSound(), 1.0f, 0.95f);
        SoundUtil.sendSoundTo(players, SoundUtil.RANDOM_FIZZ_3DFALSE, 0.9f, 0.9f);
        SoundUtil.sendSoundTo(spectators, Sound.RANDOM_CLICK.getSound(), 1.0f, 0.95f);
        SoundUtil.sendSoundTo(spectators, SoundUtil.RANDOM_FIZZ_3DFALSE, 0.9f, 0.9f);
    }

    public void forceStop() {
        stopGame();
    }

    private void stopGame() {
        items.removeYellowDye();
        timer.stop();
        if (updateUiTask != null) updateUiTask.cancel();
        if (checkPosTask != null) checkPosTask.cancel();
        gold.stop();
    }


    //getters

    public CustomPlayer getMurderer() { return murderer; }
    public CustomPlayer getSheriff() { return sheriff; }
    public boolean isMurdererAlive() { return murderer != null; }
    public boolean isSheriffAlive() { return sheriff != null; }

    public MMPlayerGameData getGameData(CustomPlayer player) { return gameDataMap.get(player); }
    public MMPlayerGameData getGameData(Entity player) { return gameDataMap.get(player); }
    public RandomSpawnsMap getMap() { return (RandomSpawnsMap) map; }
    public MMSpectatorMenu getSpectatorMenu() { return spectatorMenu; }
    public MMTeamManager getTeamManager() { return teams; }

}