package org.brlnsreb.commands.mmsubcommands.mapsubcommands;

import java.util.Date;
import java.util.LinkedList;

import org.brlnsreb.commands.subcommands.SimpleSubCommand;
import org.brlnsreb.minigames.mm.MurderMysteryGame;
import org.brlnsreb.minigames.mm.systems.GoldSpawnMapper;

import org.powernukkitx.command.CommandSender;
import org.powernukkitx.command.data.CommandParameter;
import org.powernukkitx.utils.TextFormat;

public class InfoCommand extends SimpleSubCommand {
    
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
            sender.sendMessage(TextFormat.RED + "Usage: /mmop map info <mapId>");
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