package org.brlnsreb.minigames.mm;

import org.powernukkitx.Player;
import org.powernukkitx.entity.Entity;
import org.powernukkitx.entity.effect.Effect;
import org.powernukkitx.entity.effect.EffectType;
import org.powernukkitx.level.Level;
import org.powernukkitx.level.Location;
import org.powernukkitx.level.Position;
import org.powernukkitx.level.Sound;
import org.powernukkitx.math.Vector3;
import org.powernukkitx.scheduler.Task;
import org.powernukkitx.utils.Config;
import org.powernukkitx.utils.TextFormat;
import org.brlnsreb.BrlnsReb;
import org.brlnsreb.core.minigame.match.GameStateType;
import org.brlnsreb.core.minigame.match.game.arena.RandomSpawnsArena;
import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.minigames.mm.config.MMConfig;
import org.brlnsreb.minigames.mm.match.game.entities.DeadBodyEntity;
import org.brlnsreb.minigames.mm.match.game.gamedata.MMRole;
import org.brlnsreb.minigames.mm.match.game.systems.DeathSystem;
import org.brlnsreb.minigames.mm.match.game.systems.GoldSpawnMapper;
import org.brlnsreb.minigames.mm.match.game.systems.GoldSystem;
import org.brlnsreb.minigames.mm.match.game.systems.ProjectileSystem;
import org.brlnsreb.minigames.mm.match.game.systems.RaycastSystem;
import org.brlnsreb.minigames.mm.match.game.ui.MMBossBar;
import org.brlnsreb.minigames.mm.match.game.ui.MMScoreboard;
import org.brlnsreb.minigames.mm.match.game.ui.MMSpectatorMenu;
import org.brlnsreb.minigames.mm.roles.GamePlayer;
import org.brlnsreb.minigames.mm.roles.MMRoleManager;
import org.brlnsreb.minigames.mm.systems.*;
import org.brlnsreb.utils.SoundUtil;
import org.brlnsreb.utils.TimerSystem;
import org.brlnsreb.utils.YamlUtil;
import org.brlnsreb.utils.voting.VotingMapTimeMenu;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class MurderMysteryGame {
    
    private final BrlnsReb plugin;
    private final MMConfig config;
    private final MMRoleManager roleManager;
    
    private GameStateType state;
    private RandomSpawnsArena arena;
    private final HashSet<Player> players;
    private String selectedMap;
    private String selectedTime;
    
    private TimerSystem timer;
    private MMScoreboard scoreboard;
    private RaycastSystem raycast;
    private ProjectileSystem projectile;
    private DeathSystem death;
    private GoldSystem gold;
    private CooldownSystem cooldowns;
    private MMBossBar bossBar;
    private GoldSpawnMapper mapper;
    private QuitTracker quitTracker;
    private MMSpectatorMenu spectatorMenu;
    private TrackerSystem trackerSystem;
    private VotingSystem votingSystem;
    private VotingMapTimeMenu votingMenu;

    private Task updateTask;

    private boolean countdownShortened;
    private int initialCountdown;
    
    private boolean firstKill;
    public boolean checkEnoughPlayers;

    private final Set<Entity> deadBodies = new HashSet<>();
    private final List<Position> redstonePositions = new ArrayList<>();
    
    public MurderMysteryGame(BrlnsReb plugin) {
        this.plugin = plugin;
        this.config = new MMConfig(plugin.getConfig());
        this.roleManager = new MMRoleManager();
        this.state = GameStateType.WAITING_LOBBY;
        this.players = new HashSet<>();
        this.checkEnoughPlayers = true;
        
        this.scoreboard = new MMScoreboard();
        this.raycast = new RaycastSystem(config);
        this.projectile = new ProjectileSystem(config);
        this.death = new DeathSystem(plugin, this);
        this.gold = new GoldSystem(plugin, config);
        this.cooldowns = new CooldownSystem();
        this.bossBar = new MMBossBar();
        this.mapper = new GoldSpawnMapper(plugin);
        this.quitTracker = new QuitTracker();
        this.spectatorMenu = new MMSpectatorMenu(this);
        this.trackerSystem = new TrackerSystem();
        this.votingSystem = new VotingSystem();
        this.votingMenu = new VotingMapTimeMenu(this);
    }
    
    private void onTimeExpired() {
        endGame(false);
    }
    
    public void checkWinCondition() {
        if (state != GameStateType.IN_GAME) return;
        
        cleanupOfflinePlayers();

        if (roleManager.isMurdererDead()) {
            endGame(false);
        } else if (roleManager.allInnocentsDead()) {
            endGame(true);
        }
    }

    private void endGame(boolean murdererWin) {
        state = GameStateType.ENDING;
        
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

            p.getLevel().addSound(p, Sound.RANDOM_CLICK, 1.0f, 1.0f, p);
            SoundUtil.sendSoundTo(p, "entity.generic.extinguish_fire");
        }
        
        String chatMsg = murdererWin ? 
            config.getMessage("murderer-won") : 
            config.getMessage("innocents-won");
        String chatMsg2 = murdererWin ? 
            config.getMessage("murderer-won2") : 
            config.getMessage("innocents-won2");

        broadcast(chatMsg, true);
        broadcast(chatMsg2, false);


        Level lobby = plugin.getServer().getLevelByName(config.getLobbyWorld());
        
        if (lobby == null) {
            plugin.getLogger().warning("Lobby world not found: " + config.getLobbyWorld());
        } else {
            Vector3 spawnPos = config.getLobbySpawn();

            int cx = spawnPos.getFloorX() >> 4;
            int cz = spawnPos.getFloorZ() >> 4;

            for (int x = cx - 1; x <= cx + 1; x++) {
                for (int z = cz - 1; z <= cz + 1; z++) {
                    lobby.loadChunk(x, z);
                }
            }
        }

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
        if (state == GameStateType.IN_GAME 
            || state == GameStateType.LOBBY_COUNTDOWN 
            || state == GameStateType.PREGAME_COUNTDOWN) {

            state = GameStateType.ENDING;

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

    public void setNameTagInvisible(Player player) {
        player.setNameTag("");
    }

    public void setNameTagVisible(Player player) {
        player.setNameTag(player.getName());
    }

    public List<Player> getPlayers() {
        return new ArrayList<>(players);
    }
}