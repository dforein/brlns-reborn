package org.brlnsreb.core.player;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicBoolean;

import org.cloudburstmc.protocol.bedrock.BedrockServerSession;
import org.jetbrains.annotations.NotNull;
import org.brlnsreb.BrlnsReb;
import org.brlnsreb.core.auth.AuthSystem;
import org.brlnsreb.core.lobby.Lobby;
import org.brlnsreb.core.minigame.Minigame;
import org.brlnsreb.core.player.data.PlayerData;
import org.brlnsreb.core.player.data.database.Outcome;
import org.brlnsreb.core.player.data.database.PlayerDataManager;
import org.brlnsreb.mainhub.MainHub;
import org.brlnsreb.utils.config.Configs;
import org.brlnsreb.utils.config.YamlUtil;
import org.brlnsreb.utils.messages.ChatMsgs;
import org.brlnsreb.utils.messages.Messages;
import org.brlnsreb.utils.messages.ChatMsgs.Alignment;
import org.brlnsreb.core.minigame.match.Match;
import org.brlnsreb.core.minigame.match.MatchExpand;
import org.powernukkitx.Player;
import org.powernukkitx.Server;
import org.powernukkitx.block.Block;
import org.powernukkitx.entity.EntityLiving;
import org.powernukkitx.event.entity.EntityDamageByEntityEvent;
import org.powernukkitx.event.entity.EntityDamageEvent;
import org.powernukkitx.event.entity.EntityDamageEvent.DamageCause;
import org.powernukkitx.math.BlockFace;
import org.powernukkitx.math.Vector3;
import org.powernukkitx.scoreboard.Scoreboard;

public class CustomPlayer extends Player {

    public enum DamageMode {
        INVULNERABLE,
        ONLY_INANIMATE,
        ONLY_PLAYERS,
        ONLY_MOBS,
        MOBS_AND_PLAYERS,
        FULL_NO_FALL_DAMAGE,
        FULL
    }

    public enum InteractMode {
        NOTHING,
        LIMITED,                    //doors, fences, trapdoors, buttons, levers, cakes (except lobbies, here cakes not allowed)
        ONLY_PLAYER_BLOCKS,         //limited + blocks placed by players
        FULL
    }

    public DamageMode damageMode = DamageMode.INVULNERABLE;
    public boolean canAttackPlayers = false;
    public boolean attackEvent = false;

    public InteractMode interactMode = InteractMode.LIMITED;
    private static final HashMap<Integer, HashSet<Vector3>> playerBlocks = new HashMap<>();   //Integer = levelId

    public PlayerStateType state = PlayerStateType.LOBBY;
    private WeakReference<Lobby> lobbyCurrent = new WeakReference<>(null);
    public Minigame minigameCurrent = null;
    public Match matchCurrent = null;
    
    public String grayNameTag;              //used especially in lobby
    public String greenNameTag;             //used especially in waiting lobby/ingame
    public String ingameChatNameTag;        //name tag to display in chat when texting during game

    public PlayerData data = new PlayerData();

    private Scoreboard scoreboard = null;
    private Long bossBarId = null;

    private AtomicBoolean asyncFlag = new AtomicBoolean(false);


    public CustomPlayer(@NotNull BedrockServerSession session, @NotNull PlayerInfo info) {
        super(session, info);

        MainHub.instance.teleportToSpawn(this);
    }

    @Override
    public void onPlayerLocallyInitialized() {
        super.onPlayerLocallyInitialized();

        this.sendMessage(ChatMsgs.INFO_PFX + "Logging in...");

        PlayerDataManager.onServerJoin(this).thenAccept(outcome -> {
            if (outcome != Outcome.OK) return;

            if (data.isLogged()) {
                AuthSystem.sendLoginMessageBlock(this, data.name);
            } else {
                Messages.sendMessageBlock(this, Alignment.LEFT, false,
                    "§aWelcome to §eBroken§6Lens §d" + this.getDisplayName() + "§a!",
                    "§aYou can login to save stats and coins!",
                    "§aLogin by typing §e/login"
                );
            }

            this.sendMessage(
                ChatMsgs.INFO_PFX + YamlUtil.getStr("auth.after-server-join", Configs.getGlobalMessages())
            );

            //send disclaimer
            this.sendMessage(
                ChatMsgs.BROKENLENS_PFX 
                + YamlUtil.getStr(
                    MainHub.instance.configPath() + "disclaimer", 
                    MainHub.instance.getMessages()
                )
            );
        });
        
        PlayerUtils.cleanPlayerList(this);
        MainHub.instance.onServerJoin(this);

        BrlnsReb.logger.info("§aJOIN§r " + this.getName());
    }


    //state logic

    public boolean canRunAsync() {
        return this.asyncFlag.compareAndSet(false, true);
    }

    public void resetAsync() {
        this.asyncFlag.set(false);
    }

    public boolean isPlaying() { return this.state == PlayerStateType.PLAYING; }
    public boolean isGameSpectator() { return this.state == PlayerStateType.SPECTATOR; }
    public void setGameSpectator() {
        this.state = PlayerStateType.SPECTATOR;
        this.setAttackVars(DamageMode.INVULNERABLE, false, false);
        this.interactMode = InteractMode.NOTHING;
        this.despawnFromAll();
    }

    @Override
    public void spawnTo(Player player) {
        if (this.isGameSpectator()) return;
        super.spawnTo(player);
    }

    public boolean isTeleporting() { return this.state == PlayerStateType.TELEPORTING; }
    public void setTeleporting() { this.state = PlayerStateType.TELEPORTING; }

    public void setLobby(Lobby lobby) { this.lobbyCurrent = new WeakReference<>(lobby); }
    public Lobby getLobby() { return this.lobbyCurrent.get(); }

    //data logic

    public void updatePresetNameTags() {
        this.grayNameTag = "§8" + (data.isLogged() ? data.getFloorLevel() : "?") +
                                  " §7" + (data.isLogged() ? data.name : this.getDisplayName());
        
        this.greenNameTag = "§8" + (data.isLogged() ? data.getFloorLevel() : "?") +
                                   " §a" + (data.isLogged() ? data.name : this.getDisplayName());

        this.setPresetNameTag();
    }

    public void setPresetNameTag() {
        switch (this.state) {
            case LOBBY -> this.setNameTag(this.grayNameTag);
            case WAITING_LOBBY -> this.setNameTag(this.greenNameTag);
            case DEATH_LOBBY -> this.setNameTag("§l§fGHOST§r " + data.name);
            default -> BrlnsReb.logger.alert("CustomPlayer::resetNameTag, unrecognized state: " + state.toString());
        } 
    }


    //ui logic

    public void setScoreboard(Scoreboard scoreboard) { this.scoreboard = scoreboard; }
    public void resetScoreboard() { this.scoreboard = null; }
    public void removeScoreboard() { this.removeScoreboard(this.scoreboard); }
    public Scoreboard getScoreboard() { return this.scoreboard; }
    public boolean hasScoreboard() { return this.scoreboard != null; }

    public void setBossBarId(long bossBarId) { this.bossBarId = bossBarId; }
    public void resetBossBarId() { this.bossBarId = null; }
    public Long getBossBarId() { return this.bossBarId; }
    public boolean hasBossBar() { return this.bossBarId != null; }

    public void updateExp() {
        if (data.isLogged()) {
            int fLevel = data.getFloorLevel();
            double deltaExp = calculateRequireExperience(fLevel) * (data.getLevel() - fLevel);
            this.setExperience((int) deltaExp, fLevel);
        } else {
            this.setExperience(0, 0);
        }
    }


    //interact logic

    public static void putPlacedBlock(Block block) {
        int levelId = block.getLevel().getId();

        if (!playerBlocks.containsKey(levelId)) {
            playerBlocks.put(levelId, new HashSet<Vector3>());
        }

        playerBlocks.get(levelId).add(block.getVector3());
    }

    public static void removeLevel(int levelId) {
        playerBlocks.remove(levelId);
    }

    @Override
    public void onBlockBreakStart(Vector3 pos, BlockFace face) {
        switch (interactMode) {
            case FULL:
                super.onBlockBreakStart(pos, face);
                break;

            case ONLY_PLAYER_BLOCKS:
                if (playerBlocks.get(level.getId()).contains(pos)) {
                    super.onBlockBreakStart(pos, face);
                }
                break;
        
            default:
                break;
        }
    }

    @Override
    public boolean sleepOn(Vector3 pos) {
        if (interactMode != InteractMode.FULL) return false;
        return super.sleepOn(pos);
    }


    //attack logic

    public void setAttackVars(DamageMode damageMode, boolean canAttackPlayers, boolean attackEvent) {
        this.damageMode = damageMode;
        this.canAttackPlayers = canAttackPlayers;
        this.attackEvent = attackEvent;
    }

    @Override
    public boolean attack(EntityDamageEvent source) {
        if (source.getCause() == DamageCause.VOID) {
            source.setDamage(10000.0f);
            return checkAndAttack(source);
        }

        switch (this.damageMode) {
            case INVULNERABLE:
                if (this.attackEvent) Server.getInstance().getPluginManager().callEvent(source);
                //in the other cases, it's always called when super.attack is called
                break;

            case ONLY_PLAYERS:
                if (!(source instanceof EntityDamageByEntityEvent event)) break;
                if (!(event.getDamager() instanceof CustomPlayer player)) break;
                if (!player.canAttackPlayers) break;
                return checkAndAttack(source);

            case FULL:
                return checkAndAttack(source);

            case FULL_NO_FALL_DAMAGE:
                if (source.getCause() == DamageCause.FALL) break;
                return checkAndAttack(source);

            case ONLY_INANIMATE:
                if (source instanceof EntityDamageByEntityEvent event
                    && event.getDamager() instanceof EntityLiving) break;
                return checkAndAttack(source);
                
            case ONLY_MOBS:
                if (!(source instanceof EntityDamageByEntityEvent event)) break;
                if (!(event.getDamager() instanceof EntityLiving)) break;
                if (event.getDamager() instanceof CustomPlayer) break;
                return checkAndAttack(source);
            
            case MOBS_AND_PLAYERS:
                if (!(source instanceof EntityDamageByEntityEvent event)) break;
                if (!(event.getDamager() instanceof EntityLiving damager)) break;
                if (damager instanceof CustomPlayer player && !player.canAttackPlayers) break;
                return checkAndAttack(source);
        }
        
        if (!this.attackEvent) source.setCancelled();
        return false;
    }

    private boolean checkAndAttack(EntityDamageEvent source) {
        //game preprocessing
        if (this.isPlaying()) {
            matchCurrent.getGame().getListenerAccess().onPlayerDamage(this, source);
        }
        if (source.getFinalDamage() <= 0.0F) return false;

        //getting damager (if damager exists)
        CustomPlayer damager = null;
        if (source instanceof EntityDamageByEntityEvent event) {
            if (event.getDamager() instanceof CustomPlayer) {
                damager = (CustomPlayer) event.getDamager();
                if (!damager.canAttackPlayers) return false;
            }
        }

        //health check
        if (source.getFinalDamage() < getHealthCurrent()) {
            //OK!
            return super.attack(source);
        }

        switch (state) {
            case PLAYING -> {
                if (matchCurrent instanceof MatchExpand matchExpand) {
                    //not ok, player death
                    this.setHealthCurrent(this.getHealthMax());
                    source.setDamage(1);
                    super.attack(source);               //to show the damage animation
                    this.setHealthCurrent(this.getHealthMax());
                    matchExpand.onDeath(source.getCause(), this, damager);
                }
            }

            case LOBBY, WAITING_LOBBY, DEATH_LOBBY -> {
                //go back to lobby spawn (in case of void)
                Lobby lobby = this.lobbyCurrent.get();
                if (lobby != null) {
                    lobby.teleportToSpawn(this);
                }
            }

            default -> {}
        }

        return true;
    }

}