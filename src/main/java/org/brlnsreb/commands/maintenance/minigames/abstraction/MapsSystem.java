package org.brlnsreb.commands.maintenance.minigames.abstraction;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.brlnsreb.BrlnsReb;
import org.brlnsreb.core.levels.LevelManager;
import org.brlnsreb.core.minigame.MinigameType;
import org.brlnsreb.utils.abstraction.FormAbstract;
import org.brlnsreb.utils.config.Configs;
import org.brlnsreb.utils.items.ItemManager;
import org.brlnsreb.utils.level.Weather;
import org.powernukkitx.Player;
import org.powernukkitx.Server;
import org.powernukkitx.form.window.CustomForm;
import org.powernukkitx.form.window.ModalForm;
import org.powernukkitx.form.window.SimpleForm;
import org.powernukkitx.item.Item;
import org.powernukkitx.math.Vector3;
import org.powernukkitx.utils.Config;

import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ObjectObjectImmutablePair;

public class MapsSystem extends FormAbstract {

    protected final MinigameType mgt;
    protected final Config maps;

    public static final ConcurrentHashMap<UUID, Pair<Vector3, Vector3>> minMaxMap = new ConcurrentHashMap<>();
    public static final ConcurrentHashMap<UUID, ArrayList<Vector3>> spawnsMap = new ConcurrentHashMap<>();
    public static final ConcurrentHashMap<UUID, MapsSystem> instances = new ConcurrentHashMap<>();
    
    public MapsSystem(MinigameType mgt) {
        this.mgt = mgt;
        this.maps = Configs.getConfig(mgt.nameTag + "/maps.yml");

        BrlnsReb.getScheduler().scheduleDelayedRepeatingTask(BrlnsReb.instance, 
            () -> {
                removeOfflinePlayers(minMaxMap);
                removeOfflinePlayers(spawnsMap);
                removeOfflinePlayers(instances);
            }, 
            5*60*20, 5*60*20
        );
    }

    private void removeOfflinePlayers(ConcurrentHashMap<UUID, ? extends Object> hashMap) {
        ArrayList<UUID> removed = new ArrayList<>();
        for (UUID uuid : hashMap.keySet()) {
            if (Server.getInstance().getPlayer(uuid) == null) {
                removed.add(uuid);
            }
        }
        for (UUID uuid : removed) {
            hashMap.remove(uuid);
        }
    }

    public void openForm(Player player) {
        createMapsForm().send(player);
    }

    protected SimpleForm createMapsForm() {
        SimpleForm form = new SimpleForm(mgt.nameTag.toUpperCase() + " map settings");

        form.addButton("Edit default map", p -> editDefaultMap(p));
        form.addButton("Edit enabled maps", p -> enableMaps(p));
        form.addButton("Edit maps", p -> editMaps(p));

        return form;
    }


    protected void editDefaultMap(Player player) {
        CustomForm form = new CustomForm(mgt.nameTag.toUpperCase() + " default map");
        List<String> enabledMaps = maps.getStringList("enabled-maps");

        form.addLabel("§7Choose the default map from the list of enabled maps.\n" 
                + "It is used as fallback in case another map is not available for some reason.");
        form.addDropdown("Default map", enabledMaps, enabledMaps.indexOf(maps.getString("default-map")));

        form.send(player);
        form.onSubmit((p, response) -> {
            maps.set("default-map", response.getDropdownResponse(1).elementText());
            maps.save();
            openForm(player);
        });
    }

    protected void enableMaps(Player player) {
        SimpleForm form = new SimpleForm(mgt.nameTag.toUpperCase() + " enabled maps");
        List<String> enabledMaps = maps.getStringList("enabled-maps");
        List<String> disabledMaps = getAllMaps();
        disabledMaps.removeAll(enabledMaps);

        form.addButton("§l§6Go back", p -> openForm(player));
        for (String eMap : enabledMaps) {
            form.addButton(eMap + " §7(§aENABLED§7)", p -> {
                enabledMaps.remove(eMap);
                maps.set("enabled-maps", enabledMaps);
                maps.save();
                enableMaps(player);
            });
        }
        for (String dMap : disabledMaps) {
            form.addButton(dMap + " §7(§cDISABLED§7)", p -> {
                enabledMaps.add(dMap);
                maps.set("enabled-maps", enabledMaps);
                maps.save();
                enableMaps(player);
            });
        }

        form.send(player);
    }

    protected void editMaps(Player player) {
        SimpleForm form = new SimpleForm(mgt.nameTag.toUpperCase() + " maps");
        List<String> mapList = getAllMaps();

        form.addButton("§l§6Go back", p -> openForm(player));
        form.addButton("§2Add§r new map", p -> addNewMapMinMax(player, this));
        form.addButton("§cRemove§r a map", p -> removeMap(player));
        for (String map : mapList) {
            form.addButton("Edit " + map, p -> editMap(player, map));
        }

        form.send(player);
    }


    protected static void addNewMapMinMax(Player player, MapsSystem instance) {
        minMaxMap.put(player.getUniqueId(), new ObjectObjectImmutablePair<>(new Vector3(), new Vector3()));
        instances.put(player.getUniqueId(), instance);

        player.getInventory().clearAll();
        ItemManager.giveItem(player, 0, Item.LIGHT_BLUE_DYE, "§bSet MIN X §r§7(from your X)", "minX");
        ItemManager.giveItem(player, 1, Item.LIGHT_BLUE_DYE, "§bSet MIN Y §r§7(from your Y)", "minY");
        ItemManager.giveItem(player, 2, Item.LIGHT_BLUE_DYE, "§bSet MIN Z §r§7(from your Z)", "minZ");
        ItemManager.giveItem(player, 3, Item.CYAN_DYE, "§3Set MAX X §r§7(from your X)", "maxX");
        ItemManager.giveItem(player, 4, Item.CYAN_DYE, "§3Set MAX Y §r§7(from your Y)", "maxY");
        ItemManager.giveItem(player, 5, Item.CYAN_DYE, "§3Set MAX Z §r§7(from your Z)", "maxZ");
        ItemManager.giveItem(player, 7, Item.LIME_DYE, "§aGo to Next Step", "minMaxToSpawns");
        ItemManager.giveItem(player, 8, Item.RED_DYE, "§cLeave", "leaveAddNewMap");

        player.sendMessage(
            "[§aTips§r] Go to your map world with §e/world tp§r, then use the items given to set with" 
            + " your own position each §lMinimum§r and §lMaximum§r coordinate of your map, because §cbelow Min§r"
            + " and §cabove Max§r players will die for §dvoid§r. Also Min and Max will be used in certain minigame"
            + " settings to scan the map (e.g. MurderMystery for gold spawn mapping)."
        );
    }

    public static void addNewMapSpawns(Player player) {
        spawnsMap.put(player.getUniqueId(), new ArrayList<>());

        player.getInventory().clearAll();
        ItemManager.giveItem(player, 0, Item.NETHER_STAR, "§dSet new Spawn Point §r§7(from your position)", "setSpawn");
        ItemManager.giveItem(player, 1, Item.ORANGE_DYE, "§eTeleport to last Spawn Point saved", "lastSpawn");
        ItemManager.giveItem(player, 2, Item.CLOCK, "§cRemove last Spawn Point saved", "removeSpawn");
        ItemManager.giveItem(player, 7, Item.LIME_DYE, "§aGo to Next Step", "spawnsToFields");
        ItemManager.giveItem(player, 8, Item.RED_DYE, "§cLeave", "leaveAddNewMap");

        player.sendMessage(
            "[§aTips§r] Use the items to fill and manage the spawns list (=list of possible player spawns at game start)."
        );
    }

    public void addNewMapFields(Player player) { addNewMapFields(player, null); }
    protected void addNewMapFields(Player player, String error) {
        player.getInventory().clearAll();
        CustomForm form = new CustomForm("Add new " + mgt.nameTag.toUpperCase() + " map");

        if (error != null) form.addLabel("§cError: "+ error);
        form.addInput("§lMap Id§r §7used in maps.yml and here\n(only alphabet letters, numbers, underscores)", "e.g. thegrandhotel");
        form.addInput("§lName§r §7the actual name displayed to players", "e.g. The Grand Hotel");
        form.addInput("§lWorld§r §7the folder name of the world", "e.g. the_grand_hotel or mapId");
        form.addToggle("§lNight vision§r", true);
        form.addDropdown("§lWeather§r", Weather.getStringList());
        form.addInput("§lBuilders§r §7divided by commas\n(NO unnecessary spaces for commas)", "e.g. Alice,Bob Bobby,Chloe_rine");
        form.addInput("§lBuilders team§r", "e.g. @BrokenLensCommunity");
        form.addToggle("§lEnable map§r", true);

        form.send(player);
        form.onSubmit((p, response) -> {
            int i = error == null ? 0 : 1;
            String mapId = response.getInputResponse(i++);
            if (!mapId.matches("^[a-zA-Z0-9_-]+$")) {
                addNewMapFields(p, "Map Id contains invalid characters: avoid using spaces, special characters, underscores, etc.");
                return;
            }
            String name = response.getInputResponse(i++);
            String world = response.getInputResponse(i++);
            if (!LevelManager.getAllLevelNames().contains(world)) {
                addNewMapFields(p, "World folder does not exist.");
                return;
            }

            String path = "maps." + mapId + ".";
            Vector3 min = minMaxMap.get(p.getUniqueId()).first();
            Vector3 max = minMaxMap.get(p.getUniqueId()).second();
            List<String> spawns = spawnsMap.get(p.getUniqueId()).stream()
                .map(v -> v.x+" "+v.y+" "+v.z)
                .collect(Collectors.toUnmodifiableList());

            maps.set(path + "name", name);
            maps.set(path + "world", world);
            maps.set(path + "min", Math.min(min.x, max.x)+" "+Math.min(min.y, max.y)+" "+Math.min(min.z, max.z));
            maps.set(path + "max", Math.max(max.x, min.x)+" "+Math.max(max.y, min.y)+" "+Math.max(max.z, min.z));
            maps.set(path + "night-vision", response.getToggleResponse(i++));
            maps.set(path + "weather", response.getDropdownResponse(i++).elementText());
            maps.set(path + "builders", response.getInputResponse(i++).split(","));
            maps.set(path + "builders-team", response.getInputResponse(i++));
            maps.set(path + "spawns", spawns);
            if (response.getToggleResponse(i++)) {
                var enabledMaps = maps.getStringList("enabled-maps");
                enabledMaps.add(mapId);
                maps.set("enabled-maps", enabledMaps);
            }
            maps.save();
            minMaxMap.remove(p.getUniqueId());
            spawnsMap.remove(p.getUniqueId());

            editMaps(p);
        });
    }

    public static void leaveAddNewMap(Player player) {
        ModalForm form = new ModalForm("Leave process");

        form.content("Are you sure to leave the process?");
        form.yes("Yes", p -> {
            UUID uuid = p.getUniqueId();
            minMaxMap.remove(uuid);
            spawnsMap.remove(uuid);
            instances.remove(uuid);
            p.getInventory().clearAll();
            p.sendMessage("§aYou left the process");
        });
        form.no("No", p -> {});
        form.send(player);
    }


    protected void removeMap(Player player) {
        SimpleForm form = new SimpleForm("Remove " + mgt.nameTag.toUpperCase() + " map");
        List<String> mapList = getAllMaps();

        form.addButton("§6Go back", p -> editMaps(player));
        for (String map : mapList) {
            form.addButton(map, p -> confirmRemoveMap(player, map));
        }

        form.send(player);
    }

    protected void confirmRemoveMap(Player player, String map) {
        ModalForm form = new ModalForm("Remove "+map);

        form.content("Are you sure to §cremove permanently§r "+map+"?");
        form.yes("Yes", p -> {
            maps.remove("maps." + map);
            List<String> enabledMaps = maps.getStringList("enabled-maps");
            if (enabledMaps.contains(map)) {
                enabledMaps.remove(map);
                maps.set("enabled-maps", enabledMaps);
            }
            maps.save();
            removeMap(p);
        });
        form.no("No", p -> removeMap(p));
        form.send(player);
    }


    protected void editMap(Player player, String mapId) { editMap(player, mapId, null); }
    protected void editMap(Player player, String mapId, String error) {
        CustomForm form = new CustomForm("Edit " + mgt.nameTag.toUpperCase() + " map");
        String path = "maps." + mapId + ".";

        if (error != null) form.addLabel("§cError: "+ error);
        form.addInput("§lMap Id§r §7used in maps.yml and here\n(only alphabet letters, numbers, underscores)",
            "e.g. the-grand-hotel", 
            mapId
        );
        form.addInput(
            "§lName§r §7the actual name displayed to players", 
            "e.g. The Grand Hotel", 
            maps.getString(path + "name")
        );
        form.addInput(
            "§lWorld§r §7the folder name of the world", 
            "e.g. the_grand_hotel", 
            maps.getString(path + "world")
        );
        form.addInput(
            "§lMin§r §7the minimum coordinates below which players die",
            "e.g. 12 -2 10",
            maps.getString(path + "min")
        );
        form.addInput(
            "§lMax§r §7the maximum coordinates above which players die",
            "e.g. 213 40 324",
            maps.getString(path + "max")
        );
        form.addToggle("§lNight vision§r", maps.getBoolean(path + "night-vision"));
        form.addDropdown(
            "§lWeather§r", 
            Weather.getStringList(), 
            Weather.getStringList().indexOf(maps.getString(path + "weather"))
        );
        form.addInput(
            "§lBuilders§r §7divided by commas", 
            "e.g. Alice,Bob Bobby,Chloe_rine", 
            String.join(",", maps.getStringList(path + "builders"))
        );
        form.addInput(
            "§lBuilders team§r", 
            "e.g. @BrokenLensCommunity", 
            maps.getString(path + "builders-team")
        );

        form.send(player);
        form.onSubmit((p, response) -> {
            int i = error == null ? 0 : 1;
            String newMapId = response.getInputResponse(i++);
            if (!newMapId.matches("^[a-zA-Z0-9_-]+$")) {
                editMap(p, mapId, "Map Id contains invalid characters: avoid using spaces, special characters, underscores, etc.");
                return;
            }
            String name = response.getInputResponse(i++);
            String world = response.getInputResponse(i++);
            if (!LevelManager.getAllLevelNames().contains(world)) {
                editMap(p, mapId, "World folder does not exist.");
                return;
            }
            String min = response.getInputResponse(i++);
            String max = response.getInputResponse(i++);
            if (!min.matches("^-?\\d+ -?\\d+ -?\\d+$")) {
                editMap(p, mapId, "Invalid Min coordinates: they must be 3 whole numbers separated by a single space between each other.");
                return;
            }
            if (!max.matches("^-?\\d+ -?\\d+ -?\\d+$")) {
                editMap(p, mapId, "Invalid Max coordinates: they must be 3 whole numbers separated by a single space between each other.");
                return;
            }

            String path_ = "maps." + newMapId + ".";

            if (!mapId.equals(newMapId)) maps.remove("maps." + mapId);
            maps.set(path_ + "name", name);
            maps.set(path_ + "world", world);
            maps.set(path_ + "min", min);
            maps.set(path_ + "max", max);
            maps.set(path_ + "night-vision", response.getToggleResponse(i++));
            maps.set(path_ + "weather", response.getDropdownResponse(i++).elementText());
            maps.set(path_ + "builders", response.getInputResponse(i++).split(","));
            maps.set(path_ + "builders-team", response.getInputResponse(i++));
            maps.save();

            editMaps(p);
        });
    }


    protected List<String> getAllMaps() {
        return new ArrayList<>(maps.getSection("maps").getKeys(false));
    }

}
