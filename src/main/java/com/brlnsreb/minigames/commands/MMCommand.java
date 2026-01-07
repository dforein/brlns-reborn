package com.brlnsreb.minigames.commands;

import cn.nukkit.Server;
import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.data.EntityDataType;
import cn.nukkit.entity.data.EntityDataTypes;
import cn.nukkit.entity.data.EntityFlag;
import cn.nukkit.utils.TextFormat;
import cn.nukkit.math.Vector3;
import cn.nukkit.registry.Registries;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.level.format.IChunk;
import cn.nukkit.level.GameRule;
import cn.nukkit.level.GameRules;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.brlnsreb.minigames.MinigameCore;
import com.brlnsreb.minigames.core.Arena;
import com.brlnsreb.minigames.mm.MurderMysteryGame;
import com.brlnsreb.minigames.mm.entities.DeadBodyEntity;
import com.brlnsreb.minigames.mm.systems.GoldSpawnMapper;

public class MMCommand extends Command {
    
    private final MinigameCore plugin;
    private final MurderMysteryGame game;
    
    public MMCommand(MinigameCore plugin, MurderMysteryGame game) {
        super("mm", "Murder Mystery commands", "/mm <join|leave|start|stop>");
        this.plugin = plugin;
        this.game = game;
        this.setPermission("mm.admin");
    }
    
    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(TextFormat.RED + "Only players can use this command!");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length == 0) {
            player.sendMessage(TextFormat.RED + "Game:   /mm <join|leave|start|stop>");
            player.sendMessage(TextFormat.RED + "Worlds:  /mm <listworlds|world|map|setrules>");
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "join":
                if (game.joinPlayer(player)) {
                    player.sendMessage(TextFormat.GREEN + "You joined the game!");
                } else {
                    player.sendMessage(TextFormat.RED + "Could not join game!");
                }
                return true;
                
            case "leave":
                game.leavePlayer(player);
                player.sendMessage(TextFormat.YELLOW + "You left the game!");
                return true;
            
            case "start":
                if (!player.isOp()) {
                    player.sendMessage(TextFormat.RED + "No permission!");
                    return true;
                }
                
                player.sendMessage(TextFormat.GREEN + "Force starting game...");
                game.forceStart();
                return true;
                
            case "stop":
                if (!player.isOp()) {
                    player.sendMessage(TextFormat.RED + "No permission!");
                    return true;
                }
                
                player.sendMessage(TextFormat.GREEN + "Game stopped!");
                game.forceStop();
                return true;

            case "listworlds":
                if (!player.isOp()) {
                    player.sendMessage(TextFormat.RED + "No permission!");
                    return true;
                }
                
                player.sendMessage(TextFormat.GREEN + "Loaded worlds:");
                for (Level l : plugin.getServer().getLevels().values()) {
                    player.sendMessage(TextFormat.GRAY + "- " + l.getName() + 
                        TextFormat.DARK_GRAY + " (" + l.getPlayers().size() + " players)");
                }
                return true;

            case "world":
                if (!player.isOp()) {
                    player.sendMessage(TextFormat.RED + "No permission!");
                    return true;
                }
                
                if (args.length < 2) {
                    player.sendMessage(TextFormat.RED + "Usage: /mm world <worldName>");
                    player.sendMessage(TextFormat.GRAY + "Loaded worlds:");
                    for (Level l : plugin.getServer().getLevels().values()) {
                        player.sendMessage(TextFormat.GRAY + "- " + l.getName());
                    }
                    return true;
                }
                
                String worldName = args[1];
                
                if (!plugin.getServer().isLevelLoaded(worldName)) {
                    player.sendMessage(TextFormat.YELLOW + "Loading world: " + worldName);
                    
                    if (!plugin.getServer().loadLevel(worldName)) {
                        player.sendMessage(TextFormat.RED + "Failed to load world!");
                        player.sendMessage(TextFormat.GRAY + "Check if folder exists in /worlds/");
                        return true;
                    }
                }
                
                Level level = plugin.getServer().getLevelByName(worldName);
                
                player.teleport(level.getSpawnLocation());
                player.sendMessage(TextFormat.GREEN + "World loaded!");
                player.sendMessage(TextFormat.GRAY + "Use /mm map scan <mapName> to scan spawns");
                return true;
            
            case "map":
                if (!player.isOp()) {
                    player.sendMessage(TextFormat.RED + "No permission!");
                    return true;
                }
                return handleMapCommand(player, args);

            case "setrules":
                if (!player.isOp()) {
                    player.sendMessage(TextFormat.RED + "No permission!");
                    return true;
                }

                Server server = plugin.getServer();
                GameRules gameRules = player.getLevel().getGameRules();
                server.setDefaultGamemode(Player.ADVENTURE);
                server.setDifficulty(0);
                gameRules.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
                gameRules.setGameRule(GameRule.DO_ENTITY_DROPS, false);
                gameRules.setGameRule(GameRule.DO_FIRE_TICK, false);
                gameRules.setGameRule(GameRule.DO_INSOMNIA, false);
                gameRules.setGameRule(GameRule.DO_LIMITED_CRAFTING, true);
                gameRules.setGameRule(GameRule.DO_MOB_LOOT, false);
                gameRules.setGameRule(GameRule.DO_MOB_SPAWNING, false);
                gameRules.setGameRule(GameRule.DO_TILE_DROPS, false);
                gameRules.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
                gameRules.setGameRule(GameRule.DROWNING_DAMAGE, false);
                gameRules.setGameRule(GameRule.FALL_DAMAGE, false);
                gameRules.setGameRule(GameRule.FIRE_DAMAGE, false);
                gameRules.setGameRule(GameRule.FREEZE_DAMAGE, false);
                gameRules.setGameRule(GameRule.LOCATOR_BAR, false);
                gameRules.setGameRule(GameRule.MOB_GRIEFING, false);
                gameRules.setGameRule(GameRule.NATURAL_REGENERATION, true);
                gameRules.setGameRule(GameRule.PROJECTILES_CAN_BREAK_BLOCKS, false);
                gameRules.setGameRule(GameRule.PVP, false);
                gameRules.setGameRule(GameRule.RECIPES_UNLOCK, false);
                gameRules.setGameRule(GameRule.SHOW_COORDINATES, false);
                gameRules.setGameRule(GameRule.SHOW_DAYS_PLAYED, false);
                gameRules.setGameRule(GameRule.TNT_EXPLODES, false);
                
                player.sendMessage(TextFormat.GREEN + "Game rules set!");
                return true;
            
            case "debug":
                if (!player.isOp()) {
                    player.sendMessage(TextFormat.RED + "No permission!");
                    return true;
                }

                runDebug(player, args);
                return true;
            
            case "debugaux":
                if (!player.isOp()) {
                    player.sendMessage(TextFormat.RED + "No permission!");
                    return true;
                }

                runDebugAuxiliary(player);
                return true;
                
            default:
                player.sendMessage(TextFormat.RED + "Usage: /mm <join|leave|start|stop|listworlds|world|map|setrules>");
                return true;
        }
    }

    private boolean handleMapCommand(Player player, String[] args) {
        GoldSpawnMapper mapper = game.getMapper();
        
        if (args.length < 2) {
            player.sendMessage(TextFormat.RED + "Usage:");
            player.sendMessage(TextFormat.GRAY + "/mm map savepos1 - Save current coordinates in variable position1");
            player.sendMessage(TextFormat.GRAY + "/mm map savepos2 - Save current coordinates in variable position2");
            player.sendMessage(TextFormat.GRAY + "/mm map scan <mapName> [true] - Scan gold spawns");
            player.sendMessage(TextFormat.GRAY + "  Add 'true' to treat whitelisted barriers as solid");
            player.sendMessage(TextFormat.GRAY + "/mm map scanforbarriers <mapName> - Scan and save barriers");
            player.sendMessage(TextFormat.GRAY + "/mm map countbarriers <mapName> - Count barriers (no save)");
            player.sendMessage(TextFormat.GRAY + "/mm map remove <mapName> <saved> <x1> <y1> <z1> <x2> <y2> <z2>");
            player.sendMessage(TextFormat.GRAY + "/mm map remove <mapName> sp - Use saved coordinates (need both)");
            player.sendMessage(TextFormat.GRAY + "/mm map add <mapName> <x1> <y1> <z1> <x2> <y2> <z2>");
            player.sendMessage(TextFormat.GRAY + "/mm map reload <mapName>");
            player.sendMessage(TextFormat.GRAY + "/mm map reloadbarriers <mapName>");
            player.sendMessage(TextFormat.GRAY + "/mm map list");
            player.sendMessage(TextFormat.GRAY + "/mm map info <mapName>");
            return true;
        }
        
        String subCmd = args[1].toLowerCase();
        
        switch (subCmd) {
            case "savepos1":
                plugin.getServer().getScheduler().scheduleTask(plugin, () -> {
                    mapper.savePos1(player);
                }, true);

                return false;

            case "savepos2":
                plugin.getServer().getScheduler().scheduleTask(plugin, () -> {
                    mapper.savePos2(player);
                }, true);

                return false;
            
            case "scan":
                if (args.length < 3) {
                    player.sendMessage(TextFormat.RED + "Usage: /mm map scan <mapName> [true]");
                    player.sendMessage(TextFormat.GRAY + "Add 'true' to treat whitelisted barriers as solid blocks");
                    return true;
                }
                
                String mapName = args[2];
                boolean useBarrierWhitelist = false;
                
                if (args.length >= 4 && args[3].equalsIgnoreCase("true")) {
                    useBarrierWhitelist = true;
                }
                
                Arena arena = loadArenaFromConfig(mapName, player);
                if (arena == null) {
                    player.sendMessage(TextFormat.RED + "Map not found in config: " + mapName);
                    return true;
                }
                
                final boolean finalUseWhitelist = useBarrierWhitelist;
                plugin.getServer().getScheduler().scheduleTask(plugin, () -> {
                    mapper.scanArena(arena, mapName, player, finalUseWhitelist);
                }, true);

                return true;
            
            case "scanforbarriers":
                if (args.length < 3) {
                    player.sendMessage(TextFormat.RED + "Usage: /mm map scanforbarriers <mapName>");
                    return true;
                }

                String mapName2 = args[2];
                
                Arena arena2 = loadArenaFromConfig(mapName2, player);
                if (arena2 == null) {
                    player.sendMessage(TextFormat.RED + "Map not found in config: " + mapName2);
                    return true;
                }
                
                plugin.getServer().getScheduler().scheduleTask(plugin, () -> {
                    mapper.scanForBarriers(arena2, mapName2, player);
                }, true);

                return true;
            
            case "countbarriers":
                if (args.length < 3) {
                    player.sendMessage(TextFormat.RED + "Usage: /mm map countbarriers <mapName>");
                    return true;
                }

                String mapName3 = args[2];
                
                Arena arena3 = loadArenaFromConfig(mapName3, player);
                if (arena3 == null) {
                    player.sendMessage(TextFormat.RED + "Map not found in config: " + mapName3);
                    return true;
                }
                
                plugin.getServer().getScheduler().scheduleTask(plugin, () -> {
                    mapper.countBarriers(arena3, player);
                }, true);

                return true;
                
            case "remove":
                if (args.length < 4) {
                    player.sendMessage(TextFormat.RED + "Usage: /mm map remove <mapName> <x1> <y1> <z1> <x2> <y2> <z2>");
                    player.sendMessage(TextFormat.RED + "Usage: /mm map remove <mapName> sp");
                    return true;
                }
                
                String map = args[2];
                String savedPos = args[3];

                if (savedPos.equalsIgnoreCase("sp")) {
                    mapper.removeVolume(map, mapper.position1, mapper.position2, player);
                    return true;
                }

                if (args.length < 9) {
                    player.sendMessage(TextFormat.RED + "Usage: /mm map remove <mapName> <x1> <y1> <z1> <x2> <y2> <z2>");
                    player.sendMessage(TextFormat.RED + "Usage: /mm map remove <mapName> sp");
                    return true;
                }
                
                try {
                    Vector3 pos1 = new Vector3(
                        Double.parseDouble(args[3]),
                        Double.parseDouble(args[4]),
                        Double.parseDouble(args[5])
                    );
                    Vector3 pos2 = new Vector3(
                        Double.parseDouble(args[6]),
                        Double.parseDouble(args[7]),
                        Double.parseDouble(args[8])
                    );
                    
                    mapper.removeVolume(map, pos1, pos2, player);
                } catch (NumberFormatException e) {
                    player.sendMessage(TextFormat.RED + "Invalid coordinates!");
                }
                return true;
                
            case "add":
                if (args.length < 9) {
                    player.sendMessage(TextFormat.RED + "Usage: /mm map add <mapName> <x1> <y1> <z1> <x2> <y2> <z2>");
                    return true;
                }
                
                try {
                    String map2 = args[2];
                    Vector3 pos1 = new Vector3(
                        Double.parseDouble(args[3]),
                        Double.parseDouble(args[4]),
                        Double.parseDouble(args[5])
                    );
                    Vector3 pos2 = new Vector3(
                        Double.parseDouble(args[6]),
                        Double.parseDouble(args[7]),
                        Double.parseDouble(args[8])
                    );
                    
                    mapper.addVolume(map2, pos1, pos2, player.getLevel(), player);
                } catch (NumberFormatException e) {
                    player.sendMessage(TextFormat.RED + "Invalid coordinates!");
                }
                return true;
                
            case "reload":
                if (args.length < 3) {
                    player.sendMessage(TextFormat.RED + "Usage: /mm map reload <mapName>");
                    return true;
                }
                
                if (mapper.reloadMap(args[2])) {
                    player.sendMessage(TextFormat.GREEN + "Map reloaded: " + args[2]);
                } else {
                    player.sendMessage(TextFormat.RED + "Failed to reload map!");
                }
                return true;
            
            case "reloadbarriers":
                if (args.length < 3) {
                    player.sendMessage(TextFormat.RED + "Usage: /mm map reloadbarriers <mapName>");
                    return true;
                }
                
                if (mapper.reloadBarriers(args[2])) {
                    player.sendMessage(TextFormat.GREEN + "Barriers reloaded: " + args[2]);
                } else {
                    player.sendMessage(TextFormat.RED + "Failed to reload barriers!");
                }
                return true;
                
            case "list":
                List<String> maps = mapper.listMaps();
                if (maps.isEmpty()) {
                    player.sendMessage(TextFormat.YELLOW + "No maps found!");
                } else {
                    player.sendMessage(TextFormat.GREEN + "Available maps:");
                    for (String m : maps) {
                        player.sendMessage(TextFormat.GRAY + "- " + m);
                    }
                }
                return true;
                
            case "info":
                if (args.length < 3) {
                    player.sendMessage(TextFormat.RED + "Usage: /mm map info <mapName>");
                    return true;
                }
                
                GoldSpawnMapper.MapInfo info = mapper.getMapInfo(args[2]);
                if (info == null) {
                    player.sendMessage(TextFormat.RED + "Map not found!");
                } else {
                    player.sendMessage(TextFormat.GREEN + "Map: " + info.name);
                    player.sendMessage(TextFormat.GRAY + "Spawns: " + info.spawnCount);
                    player.sendMessage(TextFormat.GRAY + "Modified: " + new Date(info.lastModified));
                }
                return true;
                
            default:
                player.sendMessage(TextFormat.RED + "Unknown subcommand!");
                return true;
        }
    }

    private Arena loadArenaFromConfig(String mapName, Player player) {
        if (!plugin.getConfig().exists("world.arena-regions." + mapName)) {
            return null;
        }

        try {
            String worldName;
            if (plugin.getConfig().exists("world.arena-regions." + mapName + ".world")) {
                worldName = plugin.getConfig().getString("world.arena-regions." + mapName + ".world");
            } else {
                worldName = plugin.getConfig().getString("world.default-world");
            }
            
            Level level = plugin.getServer().getLevelByName(worldName);
            if (level == null) {
                player.sendMessage(TextFormat.RED + "World not loaded: " + worldName);
                player.sendMessage(TextFormat.YELLOW + "Use /mm world " + worldName + " to load it");
                return null;
            }

            List<?> rawMinCoords = plugin.getConfig().getList("world.arena-regions." + mapName + ".min");
            List<?> rawMaxCoords = plugin.getConfig().getList("world.arena-regions." + mapName + ".max");
            
            List<Integer> minCoords = (List<Integer>) rawMinCoords;
            List<Integer> maxCoords = (List<Integer>) rawMaxCoords;
            
            Vector3 min = new Vector3(
                minCoords.get(0).doubleValue(),
                minCoords.get(1).doubleValue(),
                minCoords.get(2).doubleValue()
            );
            Vector3 max = new Vector3(
                maxCoords.get(0).doubleValue(),
                maxCoords.get(1).doubleValue(),
                maxCoords.get(2).doubleValue()
            );

            Object spawnsRaw = plugin.getConfig().getList("world.arena-regions." + mapName + ".spawns");
            List<List<?>> spawnsRawList = (List<List<?>>) spawnsRaw;
            List<Vector3> spawns = new ArrayList<>();
            
            for (List<?> coords : spawnsRawList) {
                spawns.add(new Vector3(
                    ((Number) coords.get(0)).doubleValue(),
                    ((Number) coords.get(1)).doubleValue(),
                    ((Number) coords.get(2)).doubleValue()
                ));
            }
            
            String arenaName = plugin.getConfig().getString("world.arena-regions." + mapName + ".name", mapName);
            
            player.sendMessage(TextFormat.GRAY + "Loaded arena '" + arenaName + "' from world '" + worldName + "'");
            
            return new Arena(arenaName, level, min, max, spawns);
            
        } catch (Exception e) {
            player.sendMessage(TextFormat.RED + "Error loading arena: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private void runDebug(Player player, String[] args) {
        //everything needing debug
        //reminder: args start from args[1] ("/mm debug {args[1]} {args[2]} ...")
        if (args.length < 2) {
            player.sendMessage("§cUsa: /mm debug <comando>");
            player.sendMessage("§7Comandi: check, dead, test");
            return;
        }

        if (args[1].equals("check")) {
            //verifica registrazione
            try {
                Class<?> entityClass = Registries.ENTITY.getEntityClass("mm:dead_body");
                if (entityClass != null) {
                    player.sendMessage("§a✓ Entità 'mm:dead_body' è registrata!");
                    player.sendMessage("§7Classe: " + entityClass.getName());
                } else {
                    player.sendMessage("§c✗ Entità 'mm:dead_body' NON è registrata!");
                }
            } catch (Exception e) {
                player.sendMessage("§cErrore: " + e.getMessage());
            }
        }
        else if (args[1].equals("dead")) {
            //spawna il corpo alla posizione del player (no gravità, quindi non serve height)
            Position pos = player.getPosition().add(2, 0, 2);

            IChunk chunk = (IChunk) pos.getLevel().getChunk(pos.getFloorX() >> 4, pos.getFloorZ() >> 4);
            DeadBodyEntity body = new DeadBodyEntity(chunk, Entity.getDefaultNBT(pos));
            
            //imposta la skin del player
            body.setSkin(player.getSkin());
            
            //imposta rotazione iniziale
            body.setRotation(player.getYaw(), 0);
            
            //spawn
            body.spawnToAll();
            
            //attiva l'animazione dopo un breve delay per assicurarsi che sia spawnato
            boolean fallForward = new java.util.Random().nextBoolean();
            plugin.getServer().getScheduler().scheduleDelayedTask(plugin, () -> {
                body.playFallAnimation(fallForward);
                plugin.getLogger().info("§eAnimazione attivata: " + (fallForward ? "AVANTI" : "INDIETRO"));
            }, 5);
            
            game.getDeadBodies().add(body);
            player.sendMessage("§aCorpo spawnato! Direzione: " + (fallForward ? "AVANTI" : "INDIETRO"));
        }
        else if (args[1].equals("test")) {
            //test con animazione forzata
            Position pos = player.getPosition().add(2, 0, 2);

            IChunk chunk = (IChunk) pos.getLevel().getChunk(pos.getFloorX() >> 4, pos.getFloorZ() >> 4);
            DeadBodyEntity body = new DeadBodyEntity(chunk, Entity.getDefaultNBT(pos));
            
            body.setSkin(player.getSkin());
            body.spawnToAll();
            
            //test: attiva SEMPRE forward
            plugin.getServer().getScheduler().scheduleDelayedTask(plugin, () -> {
                plugin.getLogger().info("§e=== TEST ANIMATION ===");
                body.setDataFlag(EntityFlag.PLAYING_DEAD, true);
                plugin.getLogger().info("PLAYING_DEAD flag: " + body.getDataFlag(EntityFlag.PLAYING_DEAD));
                
                for (Player viewer : body.getViewers().values()) {
                    body.sendData(viewer);
                    plugin.getLogger().info("Data sent to: " + viewer.getName());
                }
            }, 5);
            
            player.sendMessage("§eTest body spawnato - dovrebbe cadere AVANTI");
        }
    }

    private void runDebugAuxiliary(Player victim) {
        //auxiliary function for debug
        
        for (Level level : Server.getInstance().getLevels().values()) {
            for (Entity entity : level.getEntities()) {
                if (entity instanceof DeadBodyEntity || entity.getIdentifier().equals(DeadBodyEntity.IDENTIFIER)) {
                    entity.close();
                }
            }
        }
    }
}