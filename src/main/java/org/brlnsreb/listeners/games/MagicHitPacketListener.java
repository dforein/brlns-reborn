package org.brlnsreb.listeners.games;

import org.cloudburstmc.protocol.bedrock.packet.AnimatePacket;
import org.powernukkitx.event.EventHandler;
import org.powernukkitx.event.Listener;
import org.powernukkitx.event.server.PacketSendEvent;
import org.powernukkitx.plugin.annotation.EventListener;

@EventListener
public class MagicHitPacketListener implements Listener {

    @EventHandler
    public void onPacket(PacketSendEvent event) {
        if (event.getPacket() instanceof AnimatePacket pk) {
            if (pk.getAction() == AnimatePacket.Action.MAGIC_CRITICAL_HIT) {
                event.setCancelled();
            }
        }
    }
    
}
