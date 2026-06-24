package org.brlnsreb.commands.mmsubcommands;

import org.brlnsreb.commands.subcommands.BasicSubCommand;
import org.brlnsreb.minigames.mm.MurderMysteryGame;

import cn.nukkit.command.CommandSender;
import cn.nukkit.utils.TextFormat;
import cn.nukkit.Player;

public class MMJoinCommand extends BasicSubCommand {

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

        int out = game.joinPlayer((Player) sender);

        switch (out) {
            case 0:
                sender.sendMessage(TextFormat.GREEN + "You joined the game!");
                break;
            case -2:
                sender.sendMessage(TextFormat.colorize(game.getConfig().getMessage("already-in-game")));
                break;
            case -3:
                sender.sendMessage(TextFormat.colorize(game.getConfig().getMessage("no-slots-available")));
                break;
        }

        return true;

    }
}
