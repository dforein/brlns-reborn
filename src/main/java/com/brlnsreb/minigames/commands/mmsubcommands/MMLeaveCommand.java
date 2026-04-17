package com.brlnsreb.minigames.commands.mmsubcommands;

import com.brlnsreb.minigames.commands.subcommands.BasicSubCommand;
import com.brlnsreb.minigames.mm.MurderMysteryGame;

import cn.nukkit.command.CommandSender;
import cn.nukkit.utils.TextFormat;
import cn.nukkit.Player;

public class MMLeaveCommand extends BasicSubCommand {

    private final MurderMysteryGame game;
    
    public MMLeaveCommand(MurderMysteryGame game) {
        super("leave");
        this.setAliases(new String[] {
				"leave"
		});

        this.game = game;
    }

    @Override
	public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        Player player = (Player) sender;

        if (game.leavePlayer(player)){
            if (player.isOnline()) player.sendMessage(TextFormat.YELLOW + "You left the game!");
        } else {
            if (player.isOnline()) player.sendMessage(TextFormat.RED + "You have already left!");
        }

        return true;
    }
}
