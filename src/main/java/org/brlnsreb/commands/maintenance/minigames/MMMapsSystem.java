package org.brlnsreb.commands.maintenance.minigames;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;

import org.brlnsreb.commands.maintenance.minigames.abstraction.MapsSystem;
import org.brlnsreb.core.minigame.MinigameType;
import org.brlnsreb.minigames.mm.match.game.systems.GoldSpawnMapper;
import org.brlnsreb.minigames.mm.match.game.systems.GoldSpawnMapper.Operation;
import org.brlnsreb.utils.config.YamlUtil;
import org.brlnsreb.utils.items.ItemManager;
import org.brlnsreb.utils.messages.ChatMsgs;
import org.powernukkitx.Player;
import org.powernukkitx.Server;
import org.powernukkitx.form.window.SimpleForm;
import org.powernukkitx.item.Item;
import org.powernukkitx.level.Position;
import org.powernukkitx.math.Vector3;

public class MMMapsSystem extends MapsSystem {

    public static Map<UUID, String> mapIds = new HashMap<>();
    public static Map<UUID, Vector3> firstPosMap = new HashMap<>();
    
    public MMMapsSystem() {
        super(MinigameType.MURDER_MYSTERY);
    }

    @Override
    public void openForm(Player player) {
        SimpleForm form = createMapsForm();
        form.addButton(" §6Gold spawns mapping", p -> goldSpawnsMapping(p));
        form.send(player);
    }

    private void goldSpawnsMapping(Player player) {
        SimpleForm form = new SimpleForm("Select " + mgt.nameTag.toUpperCase() + " map for gold mapping");
        List<String> mapList = getAllMaps();

        form.addButton("§6Go back", p -> openForm(p));
        for (String map : mapList) {
            form.addButton(map, p -> startGoldSpawnsMapping(p, map));
        }

        form.send(player);
    }

    private void startGoldSpawnsMapping(Player player, String mapId) {
        String levelName = maps.getString("maps."+mapId+".world");
        String rawCoords = maps.getStringList("maps."+mapId+".spawns").getFirst();
        Position pos = YamlUtil.parsePosition(rawCoords, Server.getInstance().getLevelByName(levelName));
        if (pos == null || pos.level == null) {
            player.sendMessage(ChatMsgs.ERROR_PFX + "Something went wrong! Check the map world or the player spawns");
            return;
        }

        mapIds.put(player.getUniqueId(), mapId);
        GoldSpawnMapper.startMapping(mapId);

        player.teleport(pos);

        ItemManager.giveItem(player, 0, Item.ARROW, "§fSet first position", "setFirstPos");
        ItemManager.giveItem(player, 1, Item.LAPIS_LAZULI, "§9Include volume §7(overwrite)", "includeVolume");
        ItemManager.giveItem(player, 2, Item.ORANGE_DYE, "§6Exclude volume §7(overwrite)", "excludeVolume");
        ItemManager.giveItem(player, 3, Item.LIGHT_BLUE_DYE, "§bInclude volume §7(ignore blocks already added/removed)", "includeVolumeIgnore");
        ItemManager.giveItem(player, 4, Item.YELLOW_DYE, "§eExclude volume §7(ignore blocks already added/removed)", "excludeVolumeIgnore");
        ItemManager.giveItem(player, 7, Item.LIME_DYE, "§aSave", "spawnsMappingDone");
        ItemManager.giveItem(player, 8, Item.RED_DYE, "§cLeave", "spawnsMappingLeave");

        player.sendMessage(
            String.join(" ", 
                "[§aTips§r] Use the §l§f1st item§r to save your current coordinates as the §o\"first position\",",
                "which will be used later by the other items; of course you can change the §ofirst position§r as you need.\n",
                "[§aTips§r] The §l§9second item§r gets your current coordinates, and uses the §ofirst position§r to scan the volume of blocks",
                "between the §nfirst§r and the §ncurrent§r positions, in order to find valid coordinates where golds can spawn,",
                "and these spaces will be added to the list of gold spawns.\n",
                "[§aTips§r] The §l§63rd item§r works similar but instead it tags all the scanned valid coordinates as invalid and removes all the spawns",
                "in the volume of blocks selected which were added before.\n",
                "[§aTips§r] Finally, the §l§b4th item§r and the §l§e5th item§r work the same as the 2nd and 3rd ones,",
                "but they don't change the spawns already touched before (either marked as valid or invalid)."
            )
        );
    }

    public static void goldMapperOperation(Operation op, Player p, UUID uuid) {
        if (!mapIds.containsKey(uuid)) {
            onError(p);
            return;
        }

        Vector3 pos1 = firstPosMap.get(uuid);
        if (pos1 == null) {
            p.sendMessage("§cFirst pos not selected yet!");
            return;
        }
        p.sendMessage("Executing §6"+op.name()+"§r: ("+(int)pos1.x+", "+(int)pos1.y+", "+(int)pos1.z+") -> ("+(int)p.x+", "+(int)p.y+", "+(int)p.z+")");
        GoldSpawnMapper.volumeOperation(op, p, mapIds.get(uuid), pos1, p);
    }

    public static void finishMapping(Player p, UUID uuid) {
        if (!mapIds.containsKey(uuid)) {
            onError(p);
            return;
        }

        String mapId = mapIds.get(uuid);
        GoldSpawnMapper.finishMapping(mapId);

        ArrayList<UUID> finished = new ArrayList<>();
        for (Entry<UUID, String> entry : mapIds.entrySet()) {
            if (entry.getValue().equals(mapId)) {
                finished.add(entry.getKey());
            }
        }
        for (UUID _uuid : finished) {
            mapIds.remove(_uuid);
            firstPosMap.remove(_uuid);
            Server.getInstance().getPlayer(_uuid).ifPresent(_p -> {
                _p.getInventory().clearAll();
                _p.sendMessage("§aGold spawns saved for "+mapId);
            });
        }
    }

    public static void leaveMapping(Player p, UUID uuid) {
        if (!mapIds.containsKey(uuid)) {
            onError(p);
            return;
        }

        String mapId = mapIds.remove(uuid);
        if (!mapIds.containsValue(mapId)) {
            GoldSpawnMapper.leaveMapping(mapId);
        }
        
        firstPosMap.remove(uuid);
        p.getInventory().clearAll();
        p.sendMessage("§aYou left the process.");
    }

    private static void onError(Player p) {
        p.sendMessage(ChatMsgs.ERROR_PFX + "You are not in a gold mapping process! You can remove all the items manually or with §e/clear");
    }

}
