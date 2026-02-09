package com.brlnsreb.minigames.commands.mmsubcommands;

import com.brlnsreb.minigames.commands.subcommands.SimpleSubCommand;
import com.brlnsreb.minigames.mm.MurderMysteryGame;

import cn.nukkit.command.CommandSender;
import cn.nukkit.utils.TextFormat;
import cn.nukkit.Player;

public class MMJoinCommand extends SimpleSubCommand {

    private final MurderMysteryGame game;
    
    public MMJoinCommand(MurderMysteryGame game) {
        super("join");
        this.setAliases(new String[] {
				"join"
		});

        this.game = game;
    }

    @Override
	public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (game.joinPlayer((Player) sender)) {
            sender.sendMessage(TextFormat.GREEN + "You joined the game!");
        } else {
            sender.sendMessage(TextFormat.RED + "Could not join game!");
        }

        return true;
    }
}
