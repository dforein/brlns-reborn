package org.brlnsreb.utils;

import java.util.Collection;

import org.cloudburstmc.math.vector.Vector3f;
import org.cloudburstmc.protocol.bedrock.packet.PlaySoundPacket;

import org.powernukkitx.Player;
import org.powernukkitx.math.Vector3;

public class SoundUtil {

    private static final float DEFAULT_PITCH = 1.0f;
    private static final float DEFAULT_VOLUME = 1.0f;

    public static void sendSoundTo(Player player, String soundName) {
        sendSoundTo(player, soundName, DEFAULT_VOLUME, DEFAULT_PITCH);
    }

    public static void sendSoundTo(Player player, String soundName, float volume) {
        sendSoundTo(player, soundName, volume, DEFAULT_PITCH);
    }

    public static void sendSoundTo(Player player, String soundName, float volume, float pitch) {
        PlaySoundPacket pk = buildPlaySoundPacket(player.getVector3(), soundName, volume, pitch);
        player.sendPacket(pk);
    }

    public static void sendSoundTo(Collection<Player> players, String soundName) {
        sendSoundTo(players, soundName, DEFAULT_VOLUME, DEFAULT_PITCH);
    }

    public static void sendSoundTo(Collection<Player> players, String soundName, float volume) {
        sendSoundTo(players, soundName, volume, DEFAULT_PITCH);
    }

    public static void sendSoundTo(Collection<Player> players, String soundName, float volume, float pitch) {
        PlaySoundPacket pk = buildPlaySoundPacket(Vector3.ZERO, soundName, volume, pitch);
        for (Player p : players) {
            pk.setPosition(Vector3f.from(p.x, p.y, p.z));
            p.sendPacket(pk);
        }
    }

    private static PlaySoundPacket buildPlaySoundPacket(Vector3 pos, String soundName, float volume, float pitch) {
        PlaySoundPacket pk = new PlaySoundPacket();

        pk.setName(soundName);
        pk.setVolume(volume);
        pk.setPitch(pitch);
        pk.setPosition(Vector3f.from(pos.x, pos.y, pos.z));

        return pk;
    }

}
