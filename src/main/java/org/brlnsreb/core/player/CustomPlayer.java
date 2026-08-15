package org.brlnsreb.core.player;

import java.lang.ref.WeakReference;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.cloudburstmc.protocol.bedrock.BedrockServerSession;
import org.cloudburstmc.protocol.bedrock.data.skin.AnimationData;
import org.cloudburstmc.protocol.bedrock.data.skin.PersonaPieceData;
import org.cloudburstmc.protocol.bedrock.data.skin.PersonaPieceTintData;
import org.cloudburstmc.protocol.common.util.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.brlnsreb.BrlnsReb;
import org.brlnsreb.core.lobby.Lobby;
import org.brlnsreb.core.minigame.Minigame;
import org.brlnsreb.core.player.data.PlayerData;
import org.brlnsreb.core.player.data.database.PlayerDataManager;
import org.brlnsreb.mainhub.MainHub;
import org.brlnsreb.core.minigame.match.Match;
import org.brlnsreb.core.minigame.match.MatchExpand;
import org.powernukkitx.Player;
import org.powernukkitx.Server;
import org.powernukkitx.block.Block;
import org.powernukkitx.entity.Entity;
import org.powernukkitx.event.entity.EntityDamageByEntityEvent;
import org.powernukkitx.event.entity.EntityDamageEvent;
import org.powernukkitx.event.entity.EntityDamageEvent.DamageCause;
import org.powernukkitx.level.Location;
import org.powernukkitx.math.BlockFace;
import org.powernukkitx.math.Vector3;
import org.powernukkitx.nbt.tag.CompoundTag;
import org.powernukkitx.nbt.tag.DoubleTag;
import org.powernukkitx.nbt.tag.FloatTag;
import org.powernukkitx.nbt.tag.ListTag;
import org.powernukkitx.nbt.tag.StringTag;
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

    public PlayerData data = null;

    private Scoreboard scoreboard = null;
    private Long bossBarId = null;

    private AtomicBoolean asyncFlag = new AtomicBoolean(false);


    public CustomPlayer(@NotNull BedrockServerSession session, @NotNull PlayerInfo info) {
        super(session, info);

        PlayerDataManager.onServerJoin(this);
    }

    @Override
    public void onPlayerLocallyInitialized() {
        super.onPlayerLocallyInitialized();
        
        PlayerUtils.updateOnlinePlayer(this, true);     //remove the name for players who aren't in the same level (main hub)
        PlayerUtils.cleanPlayerList(this);

        MainHub.instance.onServerJoin(this);
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
            default -> BrlnsReb.instance.getLogger().alert("CustomPlayer::resetNameTag, unrecognized state: " + state.toString());
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
            int barExp = calculateRequireExperience(data.getFloorLevel())                   //deltaExp between minecraft xp levels
                        - calculateRequireExperience(data.getFloorLevel() + 1);
            barExp *= (data.getLevel() - data.getFloorLevel());                             //level - floorLevel = % exp needed for next exp level

            this.setExperience(barExp, data.getFloorLevel());
        } else {
            this.setExperience(0);
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
                if (source instanceof EntityDamageByEntityEvent event) {
                    Entity entity = event.getDamager();
                    if (entity instanceof Player) {
                        return checkAndAttack(source);
                    }
                }
                break;

            case FULL:
                return checkAndAttack(source);

            case FULL_NO_FALL_DAMAGE:
                if (source.getCause() == DamageCause.FALL) break;
                return checkAndAttack(source);

            case ONLY_INANIMATE:
                if (source instanceof EntityDamageByEntityEvent) break;
                return checkAndAttack(source);
                
            case ONLY_MOBS:
                if (source instanceof EntityDamageByEntityEvent event) {
                    if (event.getDamager() instanceof Player) break;
                    return checkAndAttack(source);
                }
                break;
            
            case MOBS_AND_PLAYERS:
                if (!(source instanceof EntityDamageByEntityEvent event)) break;
                Entity entity = event.getDamager();
                if (entity instanceof CustomPlayer player && !player.canAttackPlayers) break;
                return checkAndAttack(source);
        }
        
        if (!this.attackEvent) source.setCancelled();
        return false;
    }

    private boolean checkAndAttack(EntityDamageEvent source) {
        //canAttackPlayer check
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
                //not ok, player death
                this.setHealthCurrent(this.getHealthMax());
                super.attack(source);               //to show the damage animation  //TODO: need to test whether i have to wait one tick to show the anim
                this.setHealthCurrent(this.getHealthMax());

                if (matchCurrent instanceof MatchExpand matchExpand) {
                    matchExpand.onDeath(source.getCause(), this, damager);
                }
            }

            case LOBBY, WAITING_LOBBY, DEATH_LOBBY -> {
                //go back to lobby spawn (in case of void)
                this.lobbyCurrent.get().teleportToSpawn(this);
            }

            default -> {}
        }

        return true;
    }


    //save logic

    @SuppressWarnings("deprecation")
    @Override
    public void saveNBT() {
        Location spawn = MainHub.instance.getSpawnLoc();
        if (spawn == null) {
            BrlnsReb.instance.getLogger().error("MainHub spawn is null! Proceeding to save player's NBT according to PNX...");
            super.saveNBT();
            return;
        }

        this.nbt.putList("Pos", new ListTag<DoubleTag>()
                .add(new DoubleTag(spawn.x))
                .add(new DoubleTag(spawn.y))
                .add(new DoubleTag(spawn.z))
        );

        this.nbt.putList("Motion", new ListTag<DoubleTag>()
                .add(new DoubleTag(0.0))
                .add(new DoubleTag(0.0))
                .add(new DoubleTag(0.0))
        );

        this.nbt.putList("Rotation", new ListTag<FloatTag>()
            .add(new FloatTag(spawn.yaw))
            .add(new FloatTag(0.0F))
        );

        this.nbt.remove("FallDistance");
        this.nbt.remove("Fire");
        this.nbt.remove("Air");
        this.nbt.putBoolean("OnGround", true);
        this.nbt.remove("Invulnerable");
        this.nbt.putFloat("Health", this.getHealthDefaultMax());
        this.nbt.putInt("playerGameType", Player.ADVENTURE);

        this.nbt.remove("SpawnX");
        this.nbt.remove("SpawnY");
        this.nbt.remove("SpawnZ");
        this.nbt.remove("SpawnLevel");
        this.nbt.remove("SpawnDimension");

        this.nbt.remove("CursorItem");
        this.nbt.remove("ActiveEffects");
        this.nbt.remove("Attributes");
        
        this.nbt.remove("Inventory");
        this.nbt.remove("SelectedInventorySlot");
        this.nbt.remove("OffInventory");
        this.nbt.remove("EnderItems");

        this.adventureSettings.saveNBT();

        var skin = getSkin();
        var serializedSkin = skin.getSkin();
        if (serializedSkin != null) {
            CompoundTag skinTag = new CompoundTag()
                    .putByteArray("Data", serializedSkin.getSkinData().getImage())
                    .putInt("SkinImageWidth", serializedSkin.getSkinData().getWidth())
                    .putInt("SkinImageHeight", serializedSkin.getSkinData().getHeight())
                    .putString("ModelId", serializedSkin.getSkinId())
                    .putString("CapeId", serializedSkin.getCapeId())
                    .putByteArray("CapeData", serializedSkin.getCapeData().getImage())
                    .putInt("CapeImageWidth", serializedSkin.getCapeData().getWidth())
                    .putInt("CapeImageHeight", serializedSkin.getCapeData().getHeight())
                    .putByteArray("SkinResourcePatch", serializedSkin.getSkinResourcePatch().getBytes(StandardCharsets.UTF_8))
                    .putByteArray("GeometryData", serializedSkin.getGeometryData().getBytes(StandardCharsets.UTF_8))
                    .putByteArray("SkinAnimationData", serializedSkin.getAnimationData().getBytes(StandardCharsets.UTF_8))
                    .putBoolean("PremiumSkin", serializedSkin.isPremium())
                    .putBoolean("PersonaSkin", serializedSkin.isPersona())
                    .putBoolean("CapeOnClassicSkin", serializedSkin.isCapeOnClassic())
                    .putString("ArmSize", serializedSkin.getArmSize())
                    .putString("SkinColor", serializedSkin.getSkinColor())
                    .putBoolean("IsTrustedSkin", skin.isTrusted());

            List<AnimationData> animations = serializedSkin.getAnimations();
            if (!animations.isEmpty()) {
                ListTag<CompoundTag> animationsTag = new ListTag<>();
                for (AnimationData animation : animations) {
                    animationsTag.add(new CompoundTag()
                            .putFloat("Frames", animation.getFrames())
                            .putInt("Type", animation.getTextureType().ordinal())
                            .putInt("ImageWidth", animation.getImage().getWidth())
                            .putInt("ImageHeight", animation.getImage().getHeight())
                            .putInt("AnimationExpression", animation.getExpressionType().ordinal())
                            .putByteArray("Image", animation.getImage().getImage()));
                }
                skinTag.putList("AnimatedImageData", animationsTag);
            }

            List<PersonaPieceData> personaPieces = serializedSkin.getPersonaPieces();
            if (!personaPieces.isEmpty()) {
                ListTag<CompoundTag> piecesTag = new ListTag<>();
                for (PersonaPieceData piece : personaPieces) {
                    piecesTag.add(new CompoundTag().putString("PieceId", piece.getId())
                            .putString("PieceType", piece.getType())
                            .putString("PackId", piece.getPackId())
                            .putBoolean("IsDefault", piece.isDefault())
                            .putString("ProductId", piece.getProductId()));
                }
                skinTag.putList("PersonaPieces", piecesTag);
            }
            List<PersonaPieceTintData> tints = serializedSkin.getTintColors();
            if (!tints.isEmpty()) {
                ListTag<CompoundTag> tintsTag = new ListTag<>();
                for (PersonaPieceTintData tint : tints) {
                    ListTag<StringTag> colors = new ListTag<>();
                    colors.setAll(tint.getColors().stream().map(StringTag::new).collect(Collectors.toList()));
                    tintsTag.add(new CompoundTag()
                            .putString("PieceType", tint.getType())
                            .putList("Colors", colors));
                }
                skinTag.putList("PieceTintColors", tintsTag);
            }

            if (!serializedSkin.getPlayFabId().isEmpty()) {
                skinTag.putString("PlayFabId", serializedSkin.getPlayFabId());
            }

            this.getNbt().putCompound("Skin", skinTag);
        }
    }

    @Override
    public void save(boolean async) {
        Preconditions.checkState(!this.closed, "Tried to save closed player");

        saveNBT();

        if (this.level != null && this.level.getProvider() != null) {
            this.nbt.putString("Level", Server.getInstance().getDefaultLevel().getName());

            this.nbt.putInt("playerGameType", ADVENTURE);
            this.nbt.putLong("lastPlayed", System.currentTimeMillis() / 1000);
            this.nbt.putString("lastIP", this.getAddress());
            this.nbt.putInt("EXP", this.getExperience());
            this.nbt.putInt("expLevel", this.getExperienceLevel());
            this.nbt.putInt("foodLevel", 18);
            this.nbt.putFloat("foodSaturationLevel", this.getFoodData().getSaturation());
            this.nbt.putInt("enchSeed", this.getEnchantmentSeed());

            var fogIdentifiers = new ListTag<StringTag>();
            var userProvidedFogIds = new ListTag<StringTag>();
            this.fogStack.forEach(fog -> {
                fogIdentifiers.add(new StringTag(fog));
                userProvidedFogIds.add(new StringTag(fog));
            });
            this.nbt.putList("fogIdentifiers", fogIdentifiers);
            this.nbt.putList("userProvidedFogIds", userProvidedFogIds);

            this.nbt.putInt("TimeSinceRest", this.timeSinceRest);

            if (!this.getName().isBlank() && this.nbt != null) {
                this.server.saveOfflinePlayerData(this.uuid, this.getNbt(), async);
            }
        }
    }

}
