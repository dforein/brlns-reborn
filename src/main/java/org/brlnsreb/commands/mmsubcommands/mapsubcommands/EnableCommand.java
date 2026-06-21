package org.brlnsreb.commands.mmsubcommands.mapsubcommands;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.brlnsreb.commands.MMOperatorCommand;
import org.brlnsreb.commands.subcommands.SimpleSubCommand;
import org.brlnsreb.mm.MurderMysteryGame;

import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.TextFormat;

public class EnableCommand extends SimpleSubCommand {
    
    private final MurderMysteryGame game;
    private final MMOperatorCommand mmOpCommand;
    
    public EnableCommand(MurderMysteryGame game, MMOperatorCommand mmOpCommand) {
        super("enable");
        this.setAliases(new String[] {
				"enable"
		});

        this.game = game;
        this.mmOpCommand = mmOpCommand;
    }

    @Override
	public boolean execute(CommandSender sender, String commandLabel, String[] args) {

        if (args.length < 3) {
            sender.sendMessage(TextFormat.RED + "Usage: /mmop map enable <mapId>");
            return true;
        }
        
        Config config = game.getPlugin().getConfig();
        String mapId = args[2].toLowerCase();

        List<String> enabledMaps = game.getConfig().getEnabledMaps();

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

        mmOpCommand.refreshCommandsParams();

        sender.sendMessage(TextFormat.GREEN + "Map enabled!");
        sender.sendMessage(TextFormat.GRAY + "Command parameters autocomplete is not updated, however the server has already recognized the changes.");
        sender.sendMessage(TextFormat.GRAY + "To see the new suggestions, you have to rejoin the server (no need to restart).");

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