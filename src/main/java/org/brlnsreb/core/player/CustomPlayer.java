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
import org.brlnsreb.core.minigame.Minigame;
import org.brlnsreb.core.minigame.match.game.MinigameGameExpand;
import org.brlnsreb.core.minigame.match.MinigameMatch;
import org.brlnsreb.core.minigame.match.MinigameMatchExpand;
import org.brlnsreb.generallobby.GeneralLobby;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.entity.Entity;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.event.entity.EntityDamageEvent.DamageCause;
import cn.nukkit.level.Position;
import cn.nukkit.math.BlockFace;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.DoubleTag;
import cn.nukkit.nbt.tag.ListTag;
import cn.nukkit.nbt.tag.StringTag;
import cn.nukkit.scoreboard.Scoreboard;
import cn.nukkit.utils.TextFormat;

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
    private static HashMap<Integer, HashSet<Vector3>> playerBlocks = new HashMap<>();   //Integer -> levelId

    public PlayerStateType state = null;
    public Minigame currentMinigame = null;
    private WeakReference<MinigameMatch> currentMatch = null;
    
    public String lobbyPlayerNameTag;
    public String gamePlayerNameTag;
    private PlayerData data;

    public Scoreboard scoreboard = null;
    public Long bossBarId = null;

    public AtomicBoolean asyncFlag = new AtomicBoolean(false);


    public CustomPlayer(@NotNull BedrockServerSession session, @NotNull PlayerInfo info) {
        super(session, info);

        PlayerDataManager.onServerJoin(this);
        this.updatePlayerNameTag();
        GeneralLobby.getInstance().onJoin(this);
    }


    public boolean canRunAsync() {
        return this.asyncFlag.compareAndSet(false, true);
    }

    public void resetAsync() {
        this.asyncFlag.set(false);
    }

    public boolean isGameSpectator() {
        return this.state == PlayerStateType.SPECTATOR;
    }
    
    public void setGameSpectator() {
        this.state = PlayerStateType.SPECTATOR;
        this.setAttackVars(DamageMode.INVULNERABLE, false, false);
        this.interactMode = InteractMode.NOTHING;
        this.despawnFromAll();
    }

    public boolean isTeleporting() {
        return this.state == PlayerStateType.TELEPORTING;
    }

    public void setTeleporting() {
        this.state = PlayerStateType.TELEPORTING;
    }

    public PlayerData getPlayerData() {
        return this.data;
    }

    public void setPlayerData(PlayerData data) {
        this.data = data;
    }

    public void updatePlayerNameTag() {
        this.lobbyPlayerNameTag = TextFormat.colorize(
            "&8" + (data.getFloorLevel() < 0 ? "?" : data.getFloorLevel()) +
            " &7" + (data.isLogged() ? data.name : this.getName())
        );
        
        this.gamePlayerNameTag = TextFormat.colorize(
            "&8" + (data.getFloorLevel() < 0 ? "?" : data.getFloorLevel()) +
            " &a" + (data.isLogged() ? data.name : this.getName())
        );

        this.resetNameTag();
    }

    public void resetNameTag() {
        switch (this.state) {
            case LOBBY:
                this.setNameTag(this.lobbyPlayerNameTag);
                break;
            
            case WAITING_LOBBY:
                this.setNameTag(this.gamePlayerNameTag);
                break;
            
            default:
                BrlnsReb.getInstance().getLogger().alert("CustomPlayer::resetNameTag, unrecognized state: " + state.toString());
                break;
        } 
    }

    @Override
    public void spawnTo(Player player) {
        if (this.isGameSpectator()) return;
        super.spawnTo(player);
    }

    public MinigameMatch getMatch() {
        return this.currentMatch.get();
    }

    public void setMatch(MinigameMatch match) {
        this.currentMatch = new WeakReference<>(match);
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
        //TODO: attack player
        // - consider sources like cactus damage, lava, player attacks etc.: 
        //      need new variables or (maybe better) internal enum to isolate different classes of sources
        // - work on setCancelled use (for listeners)
        // - avoid death? compare damage and health

        switch (this.damageMode) {
            case INVULNERABLE:
                break;

            case ONLY_PLAYERS:
                if (source instanceof EntityDamageByEntityEvent) {
                    Entity entity = ((EntityDamageByEntityEvent) source).getDamager();
                    if (entity instanceof Player && ((CustomPlayer) entity).canAttackPlayers) {
                        return checkAndAttack(source);
                    }
                }
                break;

            case FULL:
                return checkAndAttack(source);

            case FULL_NO_FALL_DAMAGE:
                if (!(source.getCause() == DamageCause.FALL)) break;
                return checkAndAttack(source);

            case ONLY_INANIMATE:
                if (source instanceof EntityDamageByEntityEvent) break;
                return checkAndAttack(source);
                
            case ONLY_MOBS:
                if (source instanceof EntityDamageByEntityEvent) {
                    EntityDamageByEntityEvent event = (EntityDamageByEntityEvent) source;
                    if (event.getDamager() instanceof Player) break;

                    return checkAndAttack(source);
                }
                break;
            
            case MOBS_AND_PLAYERS:
                if (!(source instanceof EntityDamageByEntityEvent)) break;
                Entity entity = ((EntityDamageByEntityEvent) source).getDamager();
                if (entity instanceof Player && !((CustomPlayer) entity).canAttackPlayers) break;
                return checkAndAttack(source);
        }
        
        if (!this.attackEvent) source.setCancelled();
        return false;
    }

    private boolean checkAndAttack(EntityDamageEvent source) {
        if (source.getDamage() < getHealthCurrent()) {
            //OK!
            return super.attack(source);
        }

        //not ok, player death
        this.setHealthCurrent(this.getHealthMax());
        super.attack(source);               //to show the damage animation
        this.setHealthCurrent(this.getHealthMax());

        MinigameMatch match = getMatch();
        if (match != null && match instanceof MinigameMatchExpand) {
            ((MinigameGameExpand) match.getGame()).onDeath(this);
        }

        return true;
    }


    //save logic

    @Override
    public void saveNBT() {
        Position spawn = Server.getInstance().getDefaultLevel().getSpawnLocation();

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

        this.nbt.remove("Rotation");

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
