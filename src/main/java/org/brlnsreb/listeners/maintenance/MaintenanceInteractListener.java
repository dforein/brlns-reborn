package org.brlnsreb.listeners.maintenance;

import java.util.ArrayList;
import java.util.UUID;

import org.brlnsreb.commands.maintenance.minigames.MMMapsSystem;
import org.brlnsreb.commands.maintenance.minigames.abstraction.MapsSystem;
import org.brlnsreb.minigames.mm.match.game.systems.GoldSpawnMapper.Operation;
import org.brlnsreb.utils.Cooldown;
import org.brlnsreb.utils.messages.ChatMsgs;
import org.powernukkitx.Player;
import org.powernukkitx.event.EventHandler;
import org.powernukkitx.event.Listener;
import org.powernukkitx.event.player.PlayerInteractEvent;
import org.powernukkitx.item.Item;
import org.powernukkitx.math.Vector3;
import org.powernukkitx.nbt.tag.CompoundTag;

public class MaintenanceInteractListener implements Listener {

    private static final Cooldown cooldown = Cooldown.milliseconds(400);
    
    @EventHandler
    public void onItemInteract(PlayerInteractEvent event) {
        Item item = event.getItem();
        if (item == null) return;

        Player p = event.getPlayer();
        UUID uuid = p.getUniqueId();
        if (!cooldown.checkOrAdd(uuid)) return;

        String itemName = item.getDisplayName();
        CompoundTag nbt = item.getNbt();
        if (nbt == null) return;
        CompoundTag tag = nbt.getCompound("blr");
        if (tag == null) return;
        String data = tag.getString("data");

        switch (data) {
            case "minMaxToSpawns" -> MapsSystem.addNewMapSpawns(p);
            case "spawnsToFields" -> {
                MapsSystem instance = MapsSystem.instances.remove(uuid);
                if (instance != null) instance.addNewMapFields(p);
                else p.sendMessage(ChatMsgs.ERROR_PFX + "No form instance found.");
            }

            case "leaveAddNewMap" -> MapsSystem.leaveAddNewMap(p);

            case "minX", "minY", "minZ", "maxX", "maxY", "maxZ" -> {
                Vector3 vec;
                if (data.contains("min")) {
                    vec = MapsSystem.minMaxMap.get(uuid).first();
                } else if (data.contains("max")) {
                    vec = MapsSystem.minMaxMap.get(uuid).second();
                } else return;

                switch (data.charAt(data.length() - 1)) {
                    case 'X' -> vec.x = p.getFloorX();
                    case 'Y' -> vec.y = p.getFloorY();
                    case 'Z' -> vec.z = p.getFloorZ();
                }
                p.sendMessage((data.contains("min") ? "Min" : "Max")+": ("+vec.getFloorX()+", "+vec.getFloorY()+", "+vec.getFloorZ()+")");
            }

            case "setSpawn" -> {
                if (!itemName.contains("Set new")) return;
                Vector3 pos = new Vector3(p.getFloorX(), Math.round(p.getY()*100.0)/100.0, p.getFloorZ());
                ArrayList<Vector3> spawns = MapsSystem.spawnsMap.get(uuid);
                spawns.add(pos);
                p.sendMessage("§aAdded new spawn " + spawns.size() + " at ("+pos.getFloorX()+", "+pos.getFloorY()+", "+pos.getFloorZ()+")");
            }

            case "lastSpawn" -> {
                if (!itemName.contains("Teleport")) return;
                ArrayList<Vector3> spawns = MapsSystem.spawnsMap.get(uuid);
                if (!spawns.isEmpty()) {
                    p.teleport(spawns.getLast());
                    p.sendMessage("§eTeleported to spawn "+spawns.size());
                } else { 
                    p.sendMessage("§cNo spawns saved");
                }
            }

            case "removeSpawn" -> {
                if (!itemName.contains("Remove")) return;
                ArrayList<Vector3> spawns = MapsSystem.spawnsMap.get(uuid);
                if (!spawns.isEmpty()) {
                    p.sendMessage("§6Removed spawn "+spawns.size());
                    spawns.removeLast();
                }
                else p.sendMessage("§cNo spawns saved");
            }

            case "setFirstPos" -> {
                MMMapsSystem.firstPosMap.put(uuid, p.getVector3());
                p.sendMessage("Saved pos1: ("+(int)p.x+", "+(int)p.y+", "+(int)p.z+")");
            }
            case "includeVolume" -> MMMapsSystem.goldMapperOperation(Operation.INCLUDE, p, uuid);
            case "excludeVolume" -> MMMapsSystem.goldMapperOperation(Operation.EXCLUDE, p, uuid);
            case "includeVolumeIgnore" -> MMMapsSystem.goldMapperOperation(Operation.INCLUDE_IGNORE, p, uuid);
            case "excludeVolumeIgnore" -> MMMapsSystem.goldMapperOperation(Operation.EXCLUDE_IGNORE, p, uuid);
            case "spawnsMappingDone" -> MMMapsSystem.finishMapping(p, uuid);
            case "spawnsMappingLeave" -> MMMapsSystem.leaveMapping(p, uuid);
        }
    }

}
