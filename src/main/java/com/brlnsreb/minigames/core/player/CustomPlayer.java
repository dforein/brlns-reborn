package com.brlnsreb.minigames.core.player;

import java.lang.ref.WeakReference;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;

import com.brlnsreb.minigames.core.minigame.MinigameType;
import com.brlnsreb.minigames.core.minigame.match.MinigameMatch;
import com.brlnsreb.minigames.generallobby.GeneralLobby;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.data.Skin;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.event.entity.EntityDamageEvent.DamageCause;
import cn.nukkit.level.Position;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.DoubleTag;
import cn.nukkit.nbt.tag.FloatTag;
import cn.nukkit.nbt.tag.ListTag;
import cn.nukkit.nbt.tag.StringTag;
import cn.nukkit.network.connection.BedrockSession;
import cn.nukkit.network.protocol.types.PlayerInfo;
import cn.nukkit.scoreboard.Scoreboard;
import cn.nukkit.utils.PersonaPiece;
import cn.nukkit.utils.PersonaPieceTint;
import cn.nukkit.utils.SkinAnimation;
import cn.nukkit.utils.TextFormat;

public class CustomPlayer extends Player {

    public enum DamageState {
        INVULNERABLE,
        ONLY_INANIMATE,
        ONLY_PLAYERS,
        ONLY_MOBS,
        MOBS_AND_PLAYERS,
        FULL_NO_FALL_DAMAGE,
        FULL
    }

    public DamageState damageState = DamageState.INVULNERABLE;
    public boolean canAttackPlayers = false;
    public boolean attackEvent = false;

    public PlayerStateType state = PlayerStateType.LOBBY;
    public MinigameType currentMinigame = null;
    private WeakReference<MinigameMatch> currentMatch = null;
    
    public String playerNameTag;
    private PlayerData data;

    public Scoreboard scoreboard = null;
    public Long bossBarId = null;

    public AtomicBoolean asyncFlag = new AtomicBoolean(false);


    public CustomPlayer(@NotNull BedrockSession session, @NotNull PlayerInfo info) {
        super(session, info);

        PlayerDataManager.initPlayer(this);
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

    public void setGameSpectator(boolean value) {
        this.setGameSpectator(value, false);
    }
    
    public void setGameSpectator(boolean value, boolean spawnToAll) {
        if (value) {
            this.state = PlayerStateType.SPECTATOR;
            this.despawnFromAll();
        } else {
            if (spawnToAll) this.spawnToAll();
            this.state = null;
        }
    }

    public PlayerData getPlayerData() {
        return this.data;
    }

    public void setPlayerData(PlayerData data) {
        this.data = data;
    }

    public void updatePlayerNameTag() {
        this.playerNameTag = "&7" + (data.getFloorLevel() < 0 ? "?" : data.getFloorLevel()) 
                        + " &a" + (data.name != null ? data.name : this.getName());
        this.resetNameTag();
    }

    public void resetNameTag() {
        this.setNameTag(TextFormat.colorize(this.playerNameTag));
    }

    @Override
    public void spawnTo(Player player) {
        if (state == PlayerStateType.SPECTATOR) return;
        super.spawnTo(player);
    }

    @Override
    public boolean attack(EntityDamageEvent source) {
        //TODO: attack player
        // - consider sources like cactus damage, lava, player attacks etc.: 
        //      need new variables or (maybe better) internal enum to isolate different classes of sources
        // - work on setCancelled use (for listeners)
        // - avoid death? compare damage and health

        switch (this.damageState) {
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

        if (currentMinigame != null) {
            this.getMatch().getGame().onDeath(this);
        }

        return true;
    }

    public MinigameMatch getMatch() {
        return this.currentMatch.get();
    }

    public void setMatch(MinigameMatch match) {
        this.currentMatch = new WeakReference<>(match);
    }

    @Override
    public void saveNBT() {

        Position spawn = Server.getInstance().getDefaultLevel().getSpawnLocation();

        this.namedTag.putString("Level", spawn.getLevel().getFolderName());
        this.namedTag.putList("Pos", new ListTag<DoubleTag>()
            .add(new DoubleTag(spawn.x))
            .add(new DoubleTag(spawn.y))
            .add(new DoubleTag(spawn.z))
        );
        this.namedTag.putList("Motion", new ListTag<DoubleTag>()
            .add(new DoubleTag(0))
            .add(new DoubleTag(0))
            .add(new DoubleTag(0))
        );
        this.namedTag.putList("Rotation", new ListTag<FloatTag>()
            .add(new FloatTag(0.0f))
            .add(new FloatTag(0.0f))
        );
        this.namedTag.putFloat("FallDistance", 0.0f);
        this.namedTag.putShort("Fire", 0);
        this.namedTag.putBoolean("Invulnerable", this.invulnerable);
        this.namedTag.putFloat("Scale", this.scale);
        this.namedTag.putFloat("Health", 20.0f);
        this.namedTag.putInt("playerGameType", Player.ADVENTURE);

        this.namedTag.remove("ActiveEffects");
        this.namedTag.remove("Attributes");
        this.namedTag.remove("Inventory");
        this.namedTag.remove("SelectedInventorySlot");
        this.namedTag.remove("OffInventory");
        this.namedTag.remove("EnderItems");

        this.savePlayerSkin();

        this.adventureSettings.saveNBT();

    }

    private void savePlayerSkin() {
        Skin skin = this.getSkin();
        if (skin != null) {
            CompoundTag skinTag = (new CompoundTag()).putByteArray("Data", skin.getSkinData().data).putInt("SkinImageWidth", skin.getSkinData().width).putInt("SkinImageHeight", skin.getSkinData().height).putString("ModelId", skin.getSkinId()).putString("CapeId", skin.getCapeId()).putByteArray("CapeData", skin.getCapeData().data).putInt("CapeImageWidth", skin.getCapeData().width).putInt("CapeImageHeight", skin.getCapeData().height).putByteArray("SkinResourcePatch", skin.getSkinResourcePatch().getBytes(StandardCharsets.UTF_8)).putByteArray("GeometryData", skin.getGeometryData().getBytes(StandardCharsets.UTF_8)).putByteArray("SkinAnimationData", skin.getAnimationData().getBytes(StandardCharsets.UTF_8)).putBoolean("PremiumSkin", skin.isPremium()).putBoolean("PersonaSkin", skin.isPersona()).putBoolean("CapeOnClassicSkin", skin.isCapeOnClassic()).putString("ArmSize", skin.getArmSize()).putString("SkinColor", skin.getSkinColor()).putBoolean("IsTrustedSkin", skin.isTrusted());
            List<SkinAnimation> animations = skin.getAnimations();
            if (!animations.isEmpty()) {
                ListTag<CompoundTag> animationsTag = new ListTag<>();

                for(SkinAnimation animation : animations) {
                animationsTag.add((new CompoundTag()).putFloat("Frames", animation.frames).putInt("Type", animation.type).putInt("ImageWidth", animation.image.width).putInt("ImageHeight", animation.image.height).putInt("AnimationExpression", animation.expression).putByteArray("Image", animation.image.data));
                }

                skinTag.putList("AnimatedImageData", animationsTag);
            }

            List<PersonaPiece> personaPieces = skin.getPersonaPieces();
            if (!personaPieces.isEmpty()) {
                ListTag<CompoundTag> piecesTag = new ListTag<>();

                for(PersonaPiece piece : personaPieces) {
                piecesTag.add((new CompoundTag()).putString("PieceId", piece.id).putString("PieceType", piece.type).putString("PackId", piece.packId).putBoolean("IsDefault", piece.isDefault).putString("ProductId", piece.productId));
                }

                skinTag.putList("PersonaPieces", piecesTag);
            }

            List<PersonaPieceTint> tints = skin.getTintColors();
            if (!tints.isEmpty()) {
                ListTag<CompoundTag> tintsTag = new ListTag<>();

                for(PersonaPieceTint tint : tints) {
                ListTag<StringTag> colors = new ListTag<>();
                colors.setAll((List<StringTag>)tint.colors.stream().map(StringTag::new).collect(Collectors.toList()));
                tintsTag.add((new CompoundTag()).putString("PieceType", tint.pieceType).putList("Colors", colors));
                }

                skinTag.putList("PieceTintColors", tintsTag);
            }

            if (!skin.getPlayFabId().isEmpty()) {
                skinTag.putString("PlayFabId", skin.getPlayFabId());
            }

            this.namedTag.putCompound("Skin", skinTag);
        }
    }

}
