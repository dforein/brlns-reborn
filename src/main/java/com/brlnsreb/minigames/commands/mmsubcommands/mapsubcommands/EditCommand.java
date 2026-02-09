package com.brlnsreb.minigames.commands.mmsubcommands.mapsubcommands;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

import com.brlnsreb.minigames.MinigameCore;
import com.brlnsreb.minigames.commands.subcommands.SubCommand;

import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.TextFormat;

public class EditCommand extends SubCommand {
    
    private final MinigameCore plugin;
    private LinkedList<String> levelNames;
    
    public EditCommand(MinigameCore plugin) {
        super("edit");
        this.setAliases(new String[] {
				"edit"
		});

        this.plugin = plugin;
        levelNames = getAllLevelNames();
    }

    @Override
	public boolean execute(CommandSender sender, String commandLabel, String[] args) {

        if (args.length < 5) {
            sender.sendMessage(TextFormat.RED + "Usage: /mm map edit <mapId> <field> <args>");
            return true;
        }
        
        String mapId = args[2].toLowerCase();

        if (!Arrays.asList(plugin.getMMGame().getConfig().getMaps()).contains(mapId)) {
            sender.sendMessage(TextFormat.RED + "Map ID doesn't exist!");
            return true;
        }

        Config config = plugin.getConfig();
        String path = "world.arena-regions." + mapId + ".";

        //editable fields: name, world, night-vision, weather, builders
        switch (args[3]) {
            case "name":
                String newName = args[4];
                for (int i = 5; i < args.length; i++) {
                    newName = newName + " " + args[i];
                }
                config.set(path + "name", newName);
                break;

            case "world":
                if (!levelNames.contains(args[4])) {
                    sender.sendMessage(TextFormat.RED + "World doesn't exist!");
                    return true;
                }
                config.set(path + "world", args[4]);
                break;
                
            case "night-vision":
                switch (args[4]) {
                    case "true":
                        config.set(path + "night-vision", true);
                        break;
                    case "false":
                        config.set(path + "night-vision", false);
                        break;
                    default:
                        sender.sendMessage(TextFormat.RED + "Invalid value for <nightVision>");
                        return true;
                }
                break;

            case "weather":
                switch (args[4]) {
                    case "clear":
                        config.set(path + "weather", "Clear");
                        break;
                    case "rain":
                        config.set(path + "weather", "Rain");
                        break;
                    case "storm":
                        config.set(path + "weather", "Storm");
                        break;
                    default:
                        sender.sendMessage(TextFormat.RED + "Invalid value for weather");
                        return true;
                }
                break;

            case "builders":
                ArrayList<String> builders = new ArrayList<String>();
                for (int i = 0; i < args.length; i++) {
                    if (i >= 4) {
                        builders.add(args[i]);
                    }
                }
                config.set(path + "builders", builders);
                break;

            default:
                sender.sendMessage(TextFormat.RED + "Invalid field!");
        }

        config.save();
        sender.sendMessage(TextFormat.GREEN + "Map edited successfully!");

        return true;

    }

    @Override
    public LinkedList<LinkedList<CommandParameter>> getParametersOverloads() {
		LinkedList<LinkedList<CommandParameter>> paramList = new LinkedList<LinkedList<CommandParameter>>();
        LinkedList<CommandParameter> parameters1 = new LinkedList<CommandParameter>();
        LinkedList<CommandParameter> parameters2 = new LinkedList<CommandParameter>();
        LinkedList<CommandParameter> parameters3 = new LinkedList<CommandParameter>();
        LinkedList<CommandParameter> parameters4 = new LinkedList<CommandParameter>();
        LinkedList<CommandParameter> parameters5 = new LinkedList<CommandParameter>();

        CommandParameter param1 = CommandParameter.newEnum(this.getName(), this.getAliases());
        CommandParameter param2 = CommandParameter.newEnum("mapId", plugin.getMMGame().getConfig().getMaps());

        parameters1.add(param1);
        parameters1.add(param2);
        parameters1.add(CommandParameter.newEnum("name", new String[] {"name"}));
        parameters1.add(CommandParameter.newType("mapName", CommandParamType.TEXT));
        paramList.add(parameters1);

        parameters2.add(param1);
        parameters2.add(param2);
        parameters2.add(CommandParameter.newEnum("world", new String[] {"world"}));
        parameters2.add(CommandParameter.newEnum("worldFolder", levelNames.toArray(new String[levelNames.size()])));
        paramList.add(parameters2);

		parameters3.add(param1);
        parameters3.add(param2);
        parameters3.add(CommandParameter.newEnum("night-vision", new String[] {"night-vision"}));
        parameters3.add(CommandParameter.newEnum("boolean", new String[] {"true", "false"}));
        paramList.add(parameters3);

        parameters4.add(param1);
        parameters4.add(param2);
        parameters4.add(CommandParameter.newEnum("weather", new String[] {"weather"}));
        parameters4.add(CommandParameter.newEnum("weatherValue", new String[] {"clear", "rain", "storm"}));
        paramList.add(parameters4);

        parameters5.add(param1);
        parameters5.add(param2);
        parameters5.add(CommandParameter.newEnum("builders", new String[] {"builders"}));
        parameters5.add(CommandParameter.newType("buildersNames", CommandParamType.TEXT));
        paramList.add(parameters5);

		return paramList;
	}

    @Override
    public boolean hasOverloads() {
		return true;
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

}