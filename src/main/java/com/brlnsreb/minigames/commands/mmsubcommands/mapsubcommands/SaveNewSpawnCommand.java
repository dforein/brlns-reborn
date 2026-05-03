package com.brlnsreb.minigames.commands.mmsubcommands.mapsubcommands;

import java.util.LinkedList;
import java.util.List;

import com.brlnsreb.minigames.MinigameCore;
import com.brlnsreb.minigames.commands.subcommands.SimpleSubCommand;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.TextFormat;

public class SaveNewSpawnCommand  extends SimpleSubCommand {
    
    private final MinigameCore plugin;

    public SaveNewSpawnCommand(MinigameCore plugin) {
        super("newspawn");
        this.setAliases(new String[] {
				"newspawn"
		});

        this.plugin = plugin;
    }

    @Override
	public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        
        if (args.length < 3) {
            sender.sendMessage(TextFormat.RED + "Usage: /mmop map newspawn <mapId>");
            return true;
        }

        Config config = plugin.getConfig();
        Player player = (Player) sender;
        String mapId = args[2].toLowerCase();

        List<String> spawnsList = (List<String>) config.getStringList("world.arena-regions." + mapId + ".spawns");

        if (spawnsList == null) spawnsList = new LinkedList<>();

        spawnsList.add(coordsToString(new int[] {
            player.getFloorX(),
            player.getFloorY(),
            player.getFloorZ()
        }));

        config.set(
            "world.arena-regions." + mapId + ".spawns", 
            spawnsList
        );
        config.save();

        sender.sendMessage(TextFormat.GREEN + "New spawn in your position saved!");

        return true;

    }

    @Override
    public LinkedList<CommandParameter> getParametersList() {
		LinkedList<CommandParameter> parameters = new LinkedList<>();

		parameters.add(CommandParameter.newEnum(this.getName(), this.getAliases()));
        parameters.add(CommandParameter.newEnum("mapId", plugin.getMMGame().getConfig().getMaps()));

		return parameters;
	}

    private String coordsToString(int[] coords) {
        return coords[0] + " " + coords[1] + " " + coords[2];
    }

}
