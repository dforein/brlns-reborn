package com.brlnsreb.minigames.commands.mmsubcommands.mapsubcommands;

import java.util.List;

import com.brlnsreb.minigames.commands.subcommands.SimpleSubCommand;
import com.brlnsreb.minigames.mm.MurderMysteryGame;

import org.powernukkitx.command.CommandSender;
import org.powernukkitx.utils.TextFormat;

public class ListMapsCommand extends SimpleSubCommand {
    
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