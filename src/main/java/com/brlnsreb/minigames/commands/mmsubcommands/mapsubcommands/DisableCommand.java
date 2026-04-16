package com.brlnsreb.minigames.commands.mmsubcommands.mapsubcommands;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import com.brlnsreb.minigames.commands.MMOperatorCommand;
import com.brlnsreb.minigames.commands.subcommands.SubCommand;
import com.brlnsreb.minigames.mm.MurderMysteryGame;

import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.TextFormat;

public class DisableCommand extends SubCommand {
    
    private final MurderMysteryGame game;
    private final MMOperatorCommand mmOpCommand;
    
    public DisableCommand(MurderMysteryGame game, MMOperatorCommand fatherCommand) {
        super("disable");
        this.setAliases(new String[] {
				"disable"
		});

        this.game = game;
        this.mmOpCommand = fatherCommand;
    }

    @Override
	public boolean execute(CommandSender sender, String commandLabel, String[] args) {

        if (args.length < 3) {
            sender.sendMessage(TextFormat.RED + "Usage: /mmop map disable <mapId>");
            return true;
        }
        
        Config config = game.getPlugin().getConfig();
        String mapId = args[2].toLowerCase();

        List<String> enabledMaps = game.getConfig().getEnabledMaps();

        if (!(Arrays.asList(game.getConfig().getMaps()).contains(mapId))) {
            sender.sendMessage(TextFormat.RED + "Map doesn't exists!");
            return true;
        }

        if (!enabledMaps.contains(mapId)) {
            sender.sendMessage(TextFormat.GOLD + "Map is already disabled");
            return true;
        }
            
        enabledMaps.remove(mapId);

        config.set("world.enabled-maps", enabledMaps);
        config.save();

        mmOpCommand.refreshCommandsParams();

        sender.sendMessage(TextFormat.GREEN + "Map disabled!");
        sender.sendMessage(TextFormat.GRAY + "Command parameters autocomplete is not updated, however the server has already recognized the changes.");
        sender.sendMessage(TextFormat.GRAY + "To see the new suggestions, you have to rejoin the server (no need to restart).");

        return true;

    }

    @Override
    public LinkedList<CommandParameter> getParametersList() {
		LinkedList<CommandParameter> parameters = new LinkedList<>();
        List<String> enabledMaps = game.getConfig().getEnabledMaps();

		parameters.add(CommandParameter.newEnum(this.getName(), this.getAliases()));
        parameters.add(CommandParameter.newEnum("mapId", enabledMaps.toArray(new String[enabledMaps.size()])));

		return parameters;
	}

}