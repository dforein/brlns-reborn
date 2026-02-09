package com.brlnsreb.minigames.commands.mmsubcommands.mapsubcommands;

import java.util.List;

import com.brlnsreb.minigames.commands.subcommands.SubCommand;
import com.brlnsreb.minigames.mm.MurderMysteryGame;

import cn.nukkit.command.CommandSender;
import cn.nukkit.utils.TextFormat;

public class ListMapsCommand extends SubCommand {
    
    private final MurderMysteryGame game;
    
    public ListMapsCommand(MurderMysteryGame game) {
        super("listmaps");
        this.setAliases(new String[] {
				"listmaps"
		});

        this.game = game;
    }

    @Override
	public boolean execute(CommandSender sender, String commandLabel, String[] args) {

        List<String> maps = game.getMapper().listMaps();
        if (maps.isEmpty()) {
            sender.sendMessage(TextFormat.YELLOW + "No maps found!");
        } else {
            sender.sendMessage(TextFormat.GREEN + "Available maps:");
            for (String m : maps) {
                sender.sendMessage(TextFormat.GRAY + "- " + m);
            }
        }
        return true;

    }

}