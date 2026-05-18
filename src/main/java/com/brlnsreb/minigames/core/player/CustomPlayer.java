package com.brlnsreb.minigames.core.player;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.entity.data.Skin;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.level.Position;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.DoubleTag;
import cn.nukkit.nbt.tag.FloatTag;
import cn.nukkit.nbt.tag.ListTag;
import cn.nukkit.nbt.tag.StringTag;
import cn.nukkit.network.connection.BedrockSession;
import cn.nukkit.network.protocol.types.PlayerInfo;
import cn.nukkit.utils.PersonaPiece;
import cn.nukkit.utils.PersonaPieceTint;
import cn.nukkit.utils.SkinAnimation;
import cn.nukkit.utils.TextFormat;

public class CustomPlayer extends Player {

    public PlayerStateType state = PlayerStateType.LOBBY;
    public boolean takeDamage = false;
    public AtomicBoolean asyncFlag = new AtomicBoolean(false);
    public String playerNameTag;
    private PlayerData data;

    public CustomPlayer(@NotNull BedrockSession session, @NotNull PlayerInfo info) {
        super(session, info);

        PlayerDataManager.initPlayer(this);
        updatePlayerNameTag();
    }

    public boolean canRunAsync() {
        return asyncFlag.compareAndSet(false, true);
    }

    public void resetAsync() {
        asyncFlag.set(false);
    }

    public boolean isGameSpectator() {
        return state == PlayerStateType.SPECTATOR;
    }

    public void setGameSpectator(boolean value) {
        setGameSpectator(value, false);
    }
    
    public void setGameSpectator(boolean value, boolean spawnToAll) {
        if (value) {
            state = PlayerStateType.SPECTATOR;
            despawnFromAll();
        } else {
            if (spawnToAll) spawnToAll();
            state = null;
        }
    }

    public PlayerData getPlayerData() {
        return data;
    }

    public void setPlayerData(PlayerData data) {
        this.data = data;
    }

    public void updatePlayerNameTag() {
        playerNameTag = "&7" + (data.getFloorLevel() < 0 ? "?" : data.getFloorLevel()) 
                        + " &a" + (data.name != null ? data.name : this.getName());
        setNameTag(TextFormat.colorize(playerNameTag));
    }

    @Override
    public void spawnTo(Player player) {
        if (state == PlayerStateType.SPECTATOR) return;
        super.spawnTo(player);
    }

    @Override
    public boolean attack(EntityDamageEvent source) {
        //when working on this later, consider sources like cactus damage, player attacks, lava, etc. for different games

        if (takeDamage) {
            return super.attack(source);
        }
        
        //source.setCancelled();
        return false;
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
