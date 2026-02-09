package com.brlnsreb.minigames.commands.mmsubcommands.mapsubcommands;

import java.util.Date;
import java.util.LinkedList;

import com.brlnsreb.minigames.commands.subcommands.SubCommand;
import com.brlnsreb.minigames.mm.MurderMysteryGame;
import com.brlnsreb.minigames.mm.systems.GoldSpawnMapper;

import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.utils.TextFormat;

public class InfoCommand extends SubCommand {
    
    private final MurderMysteryGame game;
    
    public InfoCommand(MurderMysteryGame game) {
        super("info");
        this.setAliases(new String[] {
				"info"
		});

        this.game = game;
    }

    @Override
	public boolean execute(CommandSender sender, String commandLabel, String[] args) {

        if (args.length < 3) {
            sender.sendMessage(TextFormat.RED + "Usage: /mm map info <mapId>");
            return true;
        }
        
        GoldSpawnMapper.MapInfo info = game.getMapper().getMapInfo(args[2]);
        if (info == null) {
            sender.sendMessage(TextFormat.RED + "Map not found!");
        } else {
            sender.sendMessage(TextFormat.GREEN + "Map: " + info.name);
            sender.sendMessage(TextFormat.GRAY + "Gold spawns: " + info.spawnCount);
            sender.sendMessage(TextFormat.GRAY + "Modified: " + new Date(info.lastModified));
        }
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