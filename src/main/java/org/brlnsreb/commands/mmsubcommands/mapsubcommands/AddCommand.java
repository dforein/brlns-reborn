package org.brlnsreb.commands.mmsubcommands.mapsubcommands;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

import org.cloudburstmc.protocol.bedrock.data.command.CommandParamType;

import org.brlnsreb.BrlnsReb;
import org.brlnsreb.commands.MMOperatorCommand;
import org.brlnsreb.commands.subcommands.SimpleSubCommand;
import org.brlnsreb.minigames.mm.MurderMysteryGame;

import org.powernukkitx.Player;
import org.powernukkitx.command.CommandSender;
import org.powernukkitx.command.data.CommandParameter;
import org.powernukkitx.utils.Config;
import org.powernukkitx.utils.TextFormat;

public class AddCommand extends SimpleSubCommand {
    
    private final BrlnsReb plugin;
    private final MurderMysteryGame game;
    private final MMOperatorCommand mmOpCommand;
    private LinkedList<String> levelNames;
    
    public AddCommand(BrlnsReb plugin, MurderMysteryGame game, MMOperatorCommand fatherCommand) {
        super("add");
        this.setAliases(new String[] {
				"add"
		});

        this.plugin = plugin;
        this.game = game;
        this.mmOpCommand = fatherCommand;
        levelNames = getAllLevelNames();
    }

    @Override
	public boolean execute(CommandSender sender, String commandLabel, String[] args) {

        if (args.length < 11) {
            sender.sendMessage(TextFormat.RED + "Usage: /mmop map add <mapId> <minCoords> <maxCoords> <worldFolder> <mapName>");
            return true;
        }
        
        String mapId = args[2].toLowerCase();

        if (Arrays.asList(game.getConfig().getMaps()).contains(mapId)) {
            sender.sendMessage(TextFormat.RED + "Map ID already exists!");
            return true;
        }

        if (!levelNames.contains(args[9])) {
            sender.sendMessage(TextFormat.RED + "World doesn't exist!");
            return true;
        }

        Config config = plugin.getConfig();
        Player player = (Player) sender;
        String path = "world.arena-regions." + mapId + ".";

        String mapName = args[10];

        for (int i = 11; i < args.length; i++) {
            mapName = mapName + " " + args[i];
        }

        int[] minCoords = new int[3];
        int[] maxCoords = new int[3];

        try {
            minCoords[0] = parseCoordinate(args[3], player.getFloorX());
            minCoords[1] = parseCoordinate(args[4], player.getFloorY());
            minCoords[2] = parseCoordinate(args[5], player.getFloorZ());

            maxCoords[0] = parseCoordinate(args[6], player.getFloorX());
            maxCoords[1] = parseCoordinate(args[7], player.getFloorY());
            maxCoords[2] = parseCoordinate(args[8], player.getFloorZ());

        } catch (NumberFormatException e) {
            sender.sendMessage(TextFormat.RED + "Invalid coordinates!");
        }

        for(int i = 0; i < 3; i++) {
            if (minCoords[i] > maxCoords[i]){
                int tmp = minCoords[i];
                minCoords[i] = maxCoords[i];
                maxCoords[i] = tmp;
            }
        }

        config.set(path + "name", mapName);
        config.set(path + "world", args[9]);
        config.set(path + "min", coordsToString(minCoords));
        config.set(path + "max", coordsToString(maxCoords));
        config.set(path + "night-vision", false);
        config.set(path + "weather", "Clear");
        config.set(path + "builders", new ArrayList<String>());
        config.set(path + "builders-team", "");
        config.set(path + "spawns", new ArrayList<int[]>());

        config.save();
        
        mmOpCommand.refreshCommandsParams();

        sender.sendMessage(TextFormat.GREEN + "New map added successfully!");
        sender.sendMessage(TextFormat.GRAY + "Command parameters autocomplete is not updated, however the server has already recognized the changes.");
        sender.sendMessage(TextFormat.GRAY + "To see the new suggestions, you have to rejoin the server (no need to restart).");

        return true;

    }

    @Override
    public LinkedList<CommandParameter> getParametersList() {
		LinkedList<CommandParameter> parameters = new LinkedList<>();

		parameters.add(CommandParameter.newEnum(this.getName(), this.getAliases()));
        parameters.add(CommandParameter.newType("mapId", CommandParamType.ID));
        parameters.add(CommandParameter.newType("min", CommandParamType.POSITION));
        parameters.add(CommandParameter.newType("max", CommandParamType.POSITION));
        parameters.add(CommandParameter.newEnum("worldFolder", levelNames.toArray(new String[levelNames.size()])));
        parameters.add(CommandParameter.newType("mapName", CommandParamType.RAW_TEXT));

		return parameters;
	}

    public LinkedList<String> getAllLevelNames() {
        LinkedList<String> worldNames = new LinkedList<>();

        File worldsFolder = new File(plugin.getServer().getDataPath() + "/worlds");

        if (worldsFolder.exists() && worldsFolder.isDirectory()) {
            File[] files = worldsFolder.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        File levelDat = new File(file, "level.dat");
                        if (levelDat.exists()) {
                            worldNames.add(file.getName());
                        }
                    }
                }
            }
        }

        return worldNames;
    }

    private int parseCoordinate(String arg, int currentPos) {
        if (arg.startsWith("~")) {
            if (arg.length() == 1) return currentPos;
            return currentPos + Integer.parseInt(arg.substring(1));
        }
        return Integer.parseInt(arg);
    }

    private String coordsToString(int[] coords) {
        return coords[0] + " " + coords[1] + " " + coords[2];
    }

}