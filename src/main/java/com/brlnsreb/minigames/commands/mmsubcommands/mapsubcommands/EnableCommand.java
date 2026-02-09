package com.brlnsreb.minigames.commands.mmsubcommands.mapsubcommands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import com.brlnsreb.minigames.commands.subcommands.SubCommand;
import com.brlnsreb.minigames.mm.MurderMysteryGame;

import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.TextFormat;

public class EnableCommand extends SubCommand {
    
    private final MurderMysteryGame game;
    
    public EnableCommand(MurderMysteryGame game) {
        super("enable");
        this.setAliases(new String[] {
				"enable"
		});

        this.game = game;
    }

    @Override
	public boolean execute(CommandSender sender, String commandLabel, String[] args) {

        if (args.length < 3) {
            sender.sendMessage(TextFormat.RED + "Usage: /mm map enable <mapId>");
            return true;
        }
        
        Config config = game.getPlugin().getConfig();
        String mapId = args[2].toLowerCase();

        List<String> enabledMaps = new ArrayList<>(config.getStringList("world.enabled-maps"));

        if (enabledMaps.contains(mapId)) {
            sender.sendMessage(TextFormat.GOLD + "Map is already enabled");
            return true;
        }

        if (!(Arrays.asList(game.getConfig().getMaps()).contains(mapId))) {
            sender.sendMessage(TextFormat.RED + "Map doesn't exists!");
            return true;
        }
            
        enabledMaps.add(mapId);

        config.set("world.enabled-maps", enabledMaps);
        
        config.save();
        sender.sendMessage(TextFormat.GREEN + "Map enabled!");

        return true;

    }

    @Override
    public LinkedList<CommandParameter> getParametersList() {
		LinkedList<CommandParameter> parameters = new LinkedList<>();

		parameters.add(CommandParameter.newEnum(this.getName(), this.getAliases()));
        parameters.add(CommandParameter.newEnum("mapId", game.getConfig().getMaps()));

		return parameters;
	}

}