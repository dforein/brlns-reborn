package org.brlnsreb.commands.mmsubcommands.mapsubcommands;

import java.util.LinkedList;

import org.brlnsreb.commands.subcommands.SimpleSubCommand;
import org.brlnsreb.mm.MurderMysteryGame;

import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.utils.TextFormat;

public class ReloadCommand extends SimpleSubCommand {
    
    private final MurderMysteryGame game;
    
    public ReloadCommand(MurderMysteryGame game) {
        super("reload");
        this.setAliases(new String[] {
				"reload"
		});

        this.game = game;
    }

    @Override
	public boolean execute(CommandSender sender, String commandLabel, String[] args) {

        if (args.length < 3) {
            sender.sendMessage(TextFormat.RED + "Usage: /mmop map reload <mapId>");
            return true;
        }
        
        if (game.getMapper().reloadMap(args[2])) {
            sender.sendMessage(TextFormat.GREEN + "Map reloaded: " + args[2]);
        } else {
            sender.sendMessage(TextFormat.RED + "Failed to reload map!");
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