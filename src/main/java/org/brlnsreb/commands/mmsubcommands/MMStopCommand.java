package org.brlnsreb.commands.mmsubcommands;

import org.brlnsreb.commands.subcommands.BasicSubCommand;
import org.brlnsreb.mm.MurderMysteryGame;

import cn.nukkit.command.CommandSender;
import cn.nukkit.utils.TextFormat;

public class MMStopCommand extends BasicSubCommand {

    private final MurderMysteryGame game;
    
    public MMStopCommand(MurderMysteryGame game) {
        super("stop");
        this.setAliases(new String[] {
				"stop"
		});

        this.game = game;
    }

    @Override
	public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (!sender.isOp()) {
            sender.sendMessage(TextFormat.RED + "No permission!");
            return true;
        }
        
        sender.sendMessage(TextFormat.GREEN + "Game stopped!");
        game.forceStop();

        return true;
    }
}
