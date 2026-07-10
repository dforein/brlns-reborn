package org.brlnsreb.commands.mmsubcommands.mapsubcommands;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.brlnsreb.commands.subcommands.SimpleSubCommand;
import org.brlnsreb.core.minigame.match.game.Arena;
import org.brlnsreb.BrlnsReb;

import org.powernukkitx.Player;
import org.powernukkitx.command.CommandSender;
import org.powernukkitx.command.data.CommandParameter;
import org.powernukkitx.level.Level;
import org.powernukkitx.math.Vector3;
import org.powernukkitx.utils.Config;
import org.powernukkitx.utils.TextFormat;

public class CountBarriersCommand extends SimpleSubCommand {
    
    private final BrlnsReb plugin;
    
    public CountBarriersCommand(BrlnsReb plugin) {
        super("countbarriers");
        this.setAliases(new String[] {
				"countbarriers"
		});

        this.plugin = plugin;
    }

    @Override
	public boolean execute(CommandSender sender, String commandLabel, String[] args) {

        Player player = (Player) sender;

        if (args.length < 3) {
            player.sendMessage(TextFormat.RED + "Usage: /mmop map countbarriers <mapId>");
            return true;
        }

        String mapId = args[2];
        
        Arena arena = loadArenaFromConfig(mapId, player);
        if (arena == null) {
            player.sendMessage(TextFormat.RED + "Map not found in config: " + mapId);
            return true;
        }
        
        plugin.getServer().getScheduler().scheduleTask(plugin, () -> {
            plugin.getMMGame().getMapper().countBarriers(arena, player);
        }, true);

        return true;

    }
    
    @Override
    public LinkedList<CommandParameter> getParametersList() {
		LinkedList<CommandParameter> parameters = new LinkedList<>();

		parameters.add(CommandParameter.newEnum(this.getName(), this.getAliases()));
        parameters.add(CommandParameter.newEnum("mapId", plugin.getMMGame().getConfig().getMaps()));

		return parameters;
	}

    private Arena loadArenaFromConfig(String mapId, Player player) {

        int X = 0;
        int Y = 1;
        int Z = 2;

        Config config = plugin.getConfig();

        if (!config.exists("world.arena-regions." + mapId)) {
            return null;
        }

        try {
            String worldName;
            if (config.exists("world.arena-regions." + mapId + ".world")) {
                worldName = config.getString("world.arena-regions." + mapId + ".world");
            } else {
                worldName = config.getString("world.default-world");
            }
            
            Level level = plugin.getServer().getLevelByName(worldName);
            if (level == null) {
                player.sendMessage(TextFormat.RED + "World not loaded: " + worldName);
                player.sendMessage(TextFormat.YELLOW + "Use /mm world " + worldName + " to load it");
                return null;
            }

            String rawMinCoords = config.getString("world.arena-regions." + mapId + ".min");
            String rawMaxCoords = config.getString("world.arena-regions." + mapId + ".max");

            Vector3 min = new Vector3(
                parseCoordinate(rawMinCoords, X),
                parseCoordinate(rawMinCoords, Y),
                parseCoordinate(rawMinCoords, Z)
            );
            Vector3 max = new Vector3(
                parseCoordinate(rawMaxCoords, X),
                parseCoordinate(rawMaxCoords, Y),
                parseCoordinate(rawMaxCoords, Z)
            );

            List<String> spawnsRawList = config.getStringList("world.arena-regions." + mapId + ".spawns");
            List<Vector3> spawns = new ArrayList<>();
            
            for (String coords : spawnsRawList) {
                spawns.add(new Vector3(
                    parseCoordinate(coords, X),
                    parseCoordinate(coords, Y),
                    parseCoordinate(coords, Z)
                ));
            }
            
            String arenaName = config.getString("world.arena-regions." + mapId + ".name", mapId);
            
            player.sendMessage(TextFormat.GRAY + "Loaded arena '" + arenaName + "' from world '" + worldName + "'");
            
            return new Arena(arenaName, level, min, max, spawns);
            
        } catch (Exception e) {
            player.sendMessage(TextFormat.RED + "Error loading arena: " + e.getMessage());
            e.printStackTrace();
            return null;
        }

    }

    private double parseCoordinate(String rawCoords, int coord) {
        return Double.parseDouble(
            rawCoords.split("\\s+") [coord]
        );
    }
}
