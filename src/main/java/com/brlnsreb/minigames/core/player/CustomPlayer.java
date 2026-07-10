package com.brlnsreb.minigames.core.player;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

import org.cloudburstmc.protocol.bedrock.BedrockServerSession;
import org.cloudburstmc.protocol.bedrock.data.skin.AnimationData;
import org.cloudburstmc.protocol.bedrock.data.skin.PersonaPieceData;
import org.cloudburstmc.protocol.bedrock.data.skin.PersonaPieceTintData;
import org.jetbrains.annotations.NotNull;

import org.powernukkitx.Player;
import org.powernukkitx.Server;
import org.powernukkitx.event.entity.EntityDamageEvent;
import org.powernukkitx.nbt.tag.CompoundTag;
import org.powernukkitx.nbt.tag.ListTag;
import org.powernukkitx.nbt.tag.StringTag;

public class CustomPlayer extends Player {

    public boolean isGameSpectator = false;
    public boolean takeDamage = false;

    public CustomPlayer(@NotNull BedrockServerSession session, @NotNull PlayerInfo info) {
        super(session, info);

        //search player data in db through DatabaseManager and save in a PlayerData instance
    }

    public boolean isGameSpectator() {
        return isGameSpectator;
    }

    public void setGameSpectator(boolean value) {
        this.setGameSpectator(value, false);
    }
    
    public void setGameSpectator(boolean value, boolean spawnToAll) {
        this.isGameSpectator = value;

        if (value) {
            this.despawnFromAll();
        } else if (spawnToAll) {
            this.spawnToAll();
        }
    }

    @Override
    public void spawnTo(Player player) {
        if (this.isGameSpectator) return;
        super.spawnTo(player);
    }

    @Override
    public boolean attack(EntityDamageEvent source) {
        //when working on this later, consider sources like cactus damage, player attacks, lava, etc. for different games

        if (takeDamage) {
            return super.attack(source);
        } else {
            Server.getInstance().getPluginManager().callEvent(source);  //to trigger the attack listener
        }
        
        //source.setCancelled();
        return true;
    }

    @Override
    public void saveNBT() {
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

}
