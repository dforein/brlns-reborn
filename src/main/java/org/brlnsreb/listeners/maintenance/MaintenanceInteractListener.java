package org.brlnsreb.listeners.maintenance;

import java.util.ArrayList;
import java.util.UUID;

import org.brlnsreb.commands.maintenance.minigames.abstraction.MapsSystem;
import org.brlnsreb.utils.Cooldown;
import org.brlnsreb.utils.messages.ChatMsgs;
import org.powernukkitx.Player;
import org.powernukkitx.event.EventHandler;
import org.powernukkitx.event.Listener;
import org.powernukkitx.event.player.PlayerInteractEvent;
import org.powernukkitx.item.Item;
import org.powernukkitx.math.Vector3;

public class MaintenanceInteractListener implements Listener {

    private static final Cooldown cooldown = Cooldown.milliseconds(500);
    
    @EventHandler
    public void onItemInteract(PlayerInteractEvent event) {
        Item item = event.getItem();
        if (item == null) return;

        Player p = event.getPlayer();
        UUID uuid = p.getUniqueId();
        if (!cooldown.check(uuid)) return;

        String itemName = item.getDisplayName();

        switch (item.getId()) {
            case Item.LIGHT_BLUE_DYE -> {
                if (!itemName.contains("Set")) return;
                Vector3 min = MapsSystem.minMaxMap.get(uuid).first();
                if (itemName.contains(" X ")) {
                    min.x = p.getFloorX();
                } else if (itemName.contains(" Y ")) {
                    min.y = p.getFloorY();
                } else {
                    min.z = p.getFloorZ();
                }
                p.sendMessage("Min: ("+min.getFloorX()+", "+min.getFloorY()+", "+min.getFloorZ()+")");
            }
            case Item.CYAN_DYE -> {
                if (!itemName.contains("Set")) return;
                Vector3 max = MapsSystem.minMaxMap.get(uuid).second();
                if (itemName.contains(" X ")) {
                    max.x = p.getFloorX();
                } else if (itemName.contains(" Y ")) {
                    max.y = p.getFloorY();
                } else {
                    max.z = p.getFloorZ();
                }
                p.sendMessage("Max: ("+max.getFloorX()+", "+max.getFloorY()+", "+max.getFloorZ()+")");
            }
            case Item.LIME_DYE -> {
                if (!itemName.contains("Go to")) return;
                if (itemName.contains("1")) {
                    MapsSystem.addNewMapSpawns(p);
                } else if (itemName.contains("2")) {
                    MapsSystem instance = MapsSystem.instances.remove(uuid);
                    if (instance != null) instance.addNewMapFields(p);
                    else p.sendMessage(ChatMsgs.ERROR_PFX + "No menu instance found.");
                }
            }
            case Item.RED_DYE -> {
                if (!itemName.contains("Leave")) return;
                MapsSystem.minMaxMap.remove(uuid);
                MapsSystem.spawnsMap.remove(uuid);
                MapsSystem.instances.remove(uuid);
                p.getInventory().clearAll();
                p.sendMessage("§aYou left the process");
            }
            case Item.NETHER_STAR -> {
                if (!itemName.contains("Set new")) return;
                Vector3 pos = new Vector3(p.getFloorX(), Math.round(p.getY()*100.0)/100.0, p.getFloorZ());
                ArrayList<Vector3> spawns = MapsSystem.spawnsMap.get(uuid);
                spawns.add(pos);
                p.sendMessage("§aAdded new spawn " + spawns.size() + "at ("+pos.x+", "+pos.y+", "+pos.z+")");
            }
            case Item.ORANGE_DYE -> {
                if (!itemName.contains("Teleport")) return;
                ArrayList<Vector3> spawns = MapsSystem.spawnsMap.get(uuid);
                if (!spawns.isEmpty()) {
                    p.teleport(spawns.getLast());
                    p.sendMessage("§eTeleported to spawn "+spawns.size());
                } else { 
                    p.sendMessage("§cNo spawns saved");
                }
            }
            case Item.CLOCK -> {
                if (!itemName.contains("Remove")) return;
                ArrayList<Vector3> spawns = MapsSystem.spawnsMap.get(uuid);
                if (!spawns.isEmpty()) {
                    p.sendMessage("§6Removed spawn "+spawns.size());
                    spawns.removeLast();
                }
                else p.sendMessage("§cNo spawns saved");
            }
        }
    }

}
