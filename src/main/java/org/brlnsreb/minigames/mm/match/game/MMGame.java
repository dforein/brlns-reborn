package org.brlnsreb.minigames.mm.match.game;

import java.util.*;

import org.brlnsreb.BrlnsReb;
import org.brlnsreb.core.maps.MapLevel;
import org.brlnsreb.core.maps.RandomSpawnsMap;
import org.brlnsreb.core.minigame.match.GameStateType;
import org.brlnsreb.core.minigame.match.MatchExpand;
import org.brlnsreb.core.minigame.match.game.GameExpand;
import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.core.player.PlayerUtils;
import org.brlnsreb.core.player.CustomPlayer.DamageMode;
import org.brlnsreb.core.player.CustomPlayer.InteractMode;
import org.brlnsreb.core.player.data.StatType;
import org.brlnsreb.minigames.mm.match.game.entities.DeadBodyEntity;
import org.brlnsreb.minigames.mm.match.game.entities.ThrownSwordEntity;
import org.brlnsreb.minigames.mm.match.game.gamedata.MMPlayerGameData;
import org.brlnsreb.minigames.mm.match.game.gamedata.MMRole;
import org.brlnsreb.minigames.mm.match.game.gamedata.MMPlayerGameData.MMEvent;
import org.brlnsreb.minigames.mm.match.game.items.MMItemManager;
import org.brlnsreb.minigames.mm.match.game.systems.DeathSystem;
import org.brlnsreb.minigames.mm.match.game.systems.GoldSystem;
import org.brlnsreb.minigames.mm.match.game.systems.ProjectileSystem;
import org.brlnsreb.minigames.mm.match.game.systems.RaycastSystem;
import org.brlnsreb.minigames.mm.match.game.ui.MMBossBar;
import org.brlnsreb.minigames.mm.match.game.ui.MMScoreboard;
import org.brlnsreb.minigames.mm.match.game.ui.MMSpectatorMenu;
import org.brlnsreb.utils.SoundUtil;
import org.brlnsreb.utils.TimerSystem;
import org.brlnsreb.utils.YamlUtil;
import org.brlnsreb.utils.voting.TimeOfDay;
import org.brlnsreb.utils.voting.Weather;
import org.powernukkitx.Player;
import org.powernukkitx.entity.Entity;
import org.powernukkitx.entity.effect.EffectType;
import org.powernukkitx.entity.item.EntityItem;
import org.powernukkitx.event.entity.EntityDamageByEntityEvent;
import org.powernukkitx.event.entity.EntityDamageEvent;
import org.powernukkitx.event.entity.ProjectileHitEvent;
import org.powernukkitx.event.entity.EntityDamageEvent.DamageCause;
import org.powernukkitx.event.player.PlayerChatEvent;
import org.powernukkitx.event.player.PlayerCommandPreprocessEvent;
import org.powernukkitx.event.player.PlayerDropItemEvent;
import org.powernukkitx.event.player.PlayerItemHeldEvent;
import org.powernukkitx.item.Item;
import org.powernukkitx.item.ItemGoldIngot;
import org.powernukkitx.item.ItemGoldenHoe;
import org.powernukkitx.level.Position;
import org.powernukkitx.level.Sound;
import org.powernukkitx.scheduler.Task;

public class MMGame extends GameExpand {

    private final Map<CustomPlayer, MMPlayerGameData> gameDataMap = new HashMap<>();

    private final MMBossBar bossBar;
    private final MMScoreboard scoreboard;
    private final MMItemManager items;
    private final MMSpectatorMenu spectatorMenu;

    private TimerSystem timer;
    private Task updateUiTask;
    private Task checkPosTask;
    private final GoldSystem gold;
    private final DeathSystem death;
    private final ProjectileSystem projectile;
    private final RaycastSystem raycast;

    private CustomPlayer murderer;
    private boolean firstKill;
    private static float damageMultiplier;

    private CustomPlayer sheriff;
    private static boolean friendlyFireDeath;

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

    public MMGame(MatchExpand match, String mapId, TimeOfDay time, Weather weather) {
        super(match, mapId, time, weather);

        this.bossBar = new MMBossBar(this);
        this.scoreboard = new MMScoreboard(this);
        this.items = new MMItemManager(this);
        this.spectatorMenu = new MMSpectatorMenu(this);

        this.gold = new GoldSystem(config, map);
        this.death = new DeathSystem(this, scheduler);
        this.projectile = new ProjectileSystem(this);
        this.raycast = new RaycastSystem(this, scheduler);

        firstKill = true;
        damageMultiplier = (float) config.getDouble("game.murderer-damage-multiplier");
        friendlyFireDeath = config.getBoolean("game.items.hoe.friendly-fire-death");

        MMPlayerGameData.setExpPrizes(config);
        MMPlayerGameData.setCoinsPrizes(config);
    }

    protected MapLevel prepareMap(String mapId, TimeOfDay time, Weather weather) {
        return new RandomSpawnsMap(
            config, "map-settings.maps." + mapId,
            mapId, time, weather
        );
    }


    //join-leave logic

    protected Position onJoinPosition(CustomPlayer player) {
        return map.getRandomSpawn();
    }

    protected void prepareGameData(CustomPlayer player) {
        gameDataMap.put(player, new MMPlayerGameData(player));
    }

    protected void onJoinPreparePlayer(CustomPlayer player) {
        player.interactMode = InteractMode.LIMITED;

        if (config.getBoolean(map.getConfigPath() + "night-vision")) {
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

        roleCheckOnLeave(player, player.getPosition());
        checkWinConditions();
    }


    //<GAME LIFECYCLE>

    //pregame

    protected void prepareGame() {
        for (CustomPlayer p : players) {
            bossBar.updateExp(p, gameDataMap.get(p));
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
        timer.start(config.getInt("game.duration"), this::onGameEnding);

        //ui
        updateUiTask = new Task() {
            @Override
            public void onRun(int currentTick) {
                scoreboard.updateGameScoreboards();
                bossBar.updateGameBossBars();
            }
        };
        scheduler.scheduleRepeatingTask(BrlnsReb.instance, updateUiTask, config.getInt("game.ui-update-ticks"));

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
                if (outOfBounds != null) {
                    for (CustomPlayer p : outOfBounds) match.onDeath(p, null);
                }
            }
        };
        scheduler.scheduleRepeatingTask(BrlnsReb.instance, checkPosTask, 10);

        //spawn gold
        gold.startSpawning();

        //player check at game start
        if (config.getBoolean("settings.check-players-at-game-start")) {
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
            YamlUtil.getStr(titlePath, config),
            YamlUtil.getStr(subtitlePath, config),
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

    public void afterDeath(DamageCause cause, Position deathPos, CustomPlayer victim, CustomPlayer killer) {
        MMPlayerGameData gameData = gameDataMap.get(killer);

        if (killer == murderer) {
            if (isFirstKill()) {
                msgUtil.sendPresetMessagePrefix(murderer, "murderer-warning");
            }

            if (victim == sheriff) {
                murdererKillsSheriff(gameData);
            } else {
                murdererKillsInnocent(gameData, victim.data.name);
            }

        } else if (killer == sheriff) {
            if (victim == murderer) {
                sheriffKillsMurderer(gameData);
            } else {
                sheriffKillsInnocent();
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
            new Integer[] { 
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
            getMatch().onDeath(sheriff, null);
        } else {
            msgUtil.broadcastPresetPrefix("sheriff-friendly-fire");
        }
    }

    private void roleCheckOnLeave(CustomPlayer player, Position lastPos) {
        if (gameDataMap.get(player).role == MMRole.MURDERER) {
            murderer = null;
        } else if (gameDataMap.get(player).role == MMRole.SHERIFF) {
            sheriff = null;

            death.dropSheriffHoe(lastPos);
            items.giveYellowDye();

            msgUtil.broadcastPresetPrefix("sheriff-gun-dropped");
            msgUtil.broadcastPresetPrefix("sheriff-dead-instructions");
        }

        gameDataMap.remove(player);
    }


    //features

    private boolean collectGold(CustomPlayer player) {
        MMPlayerGameData gameData = getGameData(player);
        if (gameData.role != MMRole.INNOCENT) return false;

        gameData.gold++;
        gameData.addExp(MMEvent.GOLD);

        msgUtil.sendPresetMessagePrefix(player, "gold-collected");
        return true;
    }

    private void useFlash(CustomPlayer player) {
        MMPlayerGameData gameData = gameDataMap.get(player);
        if (gameData.flashUsed) return;

        gameData.flashUsed = true;
        items.useFlash(player, scheduler);
    }

    private void throwSword(CustomPlayer player) {
        if (!projectile.throwSword(player)) {
            msgUtil.sendPresetMessagePrefix(player, "sword-cooldown");
        }
    }

    private boolean newSheriff(CustomPlayer player) {
        MMPlayerGameData gameData = gameDataMap.get(player);
        if (!gameData.canBecomeSheriff()) {
            msgUtil.sendPresetMessagePrefix(player, "not-enough-gold", new Integer[] { gameData.gold });
            return false;
        }

        if (isSheriffAlive()) return true;

        sheriff = player;
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

        if (!isMurdererAlive()) msgUtil.broadcastPresetPrefix("murderer-dead");

        msgUtil.broadcastPresetPrefix(isMurdererAlive() ? "murderer-won" : "innocents-won");
        msgUtil.broadcastPresetPrefix(players, "congratulations");
        
        msgUtil.sendTitle(isMurdererAlive() ? "title.murderer-won" : "title.innocents-won", null);

        SoundUtil.sendSoundTo(players, Sound.RANDOM_CLICK.getSound());
        SoundUtil.sendSoundTo(players, "entity.generic.extinguish_fire");
        SoundUtil.sendSoundTo(spectators, Sound.RANDOM_CLICK.getSound());
        SoundUtil.sendSoundTo(spectators, "entity.generic.extinguish_fire");
    }

    public void forceStop() {
        timer.stop();
        stopGame();
    }

    private void stopGame() {
        if (updateUiTask != null) updateUiTask.cancel();
        if (checkPosTask != null) checkPosTask.cancel();
        gold.stop();
    }


    //LISTENER ACCESS

    //items

    public void onItemUse(CustomPlayer player, Item item) {
        switch (item.getId()) {
            //sheriff
            case Item.GOLDEN_HOE -> {
                CustomPlayer victim = raycast.shoot(player);
                if (victim != null) getMatch().onDeath(victim, player);
            }

            //murderer
            case Item.IRON_SWORD -> throwSword(player);
            case Item.BLAZE_ROD -> useFlash(player);

            //innocent
            case Item.YELLOW_DYE -> newSheriff(player);
            
            //spectator
            case Item.NETHER_STAR -> spectatorMenu.openSpectateMenu(player);
            case Item.COMPASS -> spectatorMenu.openActionsMenu(player);
        }
    }

    public boolean onItemPickup(CustomPlayer player, EntityItem itemEntity) {
        if (state.current != GameStateType.IN_GAME) return false;

        if (itemEntity.getItem() instanceof ItemGoldIngot) {
            if (collectGold(player)) itemEntity.close();
        }

        if (itemEntity.getItem() instanceof ItemGoldenHoe) {
            if (newSheriff(player)) itemEntity.close();
        }

        return false;
    }

    public boolean onItemHeld(CustomPlayer player, PlayerItemHeldEvent event) { return true; }
    public boolean onItemDrop(CustomPlayer player, PlayerDropItemEvent event) { return false; }


    //damage

    public void onPlayerDamage(CustomPlayer player, EntityDamageEvent event) {
        if (event instanceof EntityDamageByEntityEvent) {
            MMPlayerGameData damagerGameData = gameDataMap.get(event.getEntity());
            if (damagerGameData == null || damagerGameData.role != MMRole.MURDERER) return;
            event.setDamage(event.getDamage() * damageMultiplier);
        }
    }


    //projectile

    public void onProjectileHit(CustomPlayer player, ProjectileHitEvent event) {
        if (state.current != GameStateType.IN_GAME) return;
        if (!(event.getEntity() instanceof ThrownSwordEntity)) return;
        if (player == murderer) return;

        getMatch().onDeath(player, (CustomPlayer) ((ThrownSwordEntity) event.getEntity()).shootingEntity);
    }


    //chat

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
                msgUtil.sendPresetMessagePrefix(player, "no-chat-spectators", new String[] { "spectators" });
                return false;
            }
        }
        
        return true;
    }

    private boolean roleCheckOnChat(CustomPlayer player) {
        switch (gameDataMap.get(player).role) {
            case MURDERER:
                msgUtil.sendPresetMessagePrefix(player, "no-chat", new String[] { "the murderer" });
                return false;

            case SHERIFF:
                msgUtil.sendPresetMessagePrefix(player, "no-chat", new String[] { "the sheriff" });
                return false;

            case INNOCENT: return true;
            default: return true;
        }
    }


    public boolean isMurdererAlive() { return murderer != null; }
    public boolean isSheriffAlive() { return sheriff != null; }
    public MMPlayerGameData getGameData(CustomPlayer player) { return gameDataMap.get(player); }
    public RandomSpawnsMap getMap() { return (RandomSpawnsMap) map; }

}