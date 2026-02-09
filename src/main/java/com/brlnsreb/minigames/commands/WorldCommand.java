package com.brlnsreb.minigames.commands;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.brlnsreb.minigames.MinigameCore;

import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.utils.TextFormat;
import cn.nukkit.Player;
import cn.nukkit.level.Level;

public class WorldCommand extends Command {

    MinigameCore plugin;
    
    public WorldCommand(MinigameCore plugin) {
        super("world");
        this.setDescription("Commands for world navigation");
        this.setPermission("mm.admin");

        this.getCommandParameters().clear();

        this.addCommandParameters("list", new CommandParameter[] {
            CommandParameter.newEnum("subcommand", new String[]{"list"})
        });
        this.addCommandParameters("tp", new CommandParameter[] {
            CommandParameter.newEnum("subcommand", new String[]{"tp"}),
            CommandParameter.newEnum("worldName", getAllLevelNames())
        });

        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        Player player = (Player) sender;
        
        switch (args[0].toLowerCase()) {

            case "list":
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

            case "tp":
                if (!player.isOp()) {
                    player.sendMessage(TextFormat.RED + "No permission!");
                    return true;
                }
                
                if (args.length < 2) {
                    player.sendMessage(TextFormat.RED + "Usage: /mm world <world_name>");
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

                return true;
            
            default:
                sender.sendMessage(TextFormat.RED + "Usage: /world <list|tp>");

                return true;
        }
    }

    public String[] getAllLevelNames() {
        List<String> worldNames = new ArrayList<>();

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

        return worldNames.toArray(new String[worldNames.size()]);
    }
}
