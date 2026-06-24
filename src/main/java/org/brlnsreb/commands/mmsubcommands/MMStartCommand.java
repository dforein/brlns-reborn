package org.brlnsreb.commands.mmsubcommands;

import org.brlnsreb.commands.subcommands.BasicSubCommand;
import org.brlnsreb.minigames.mm.MurderMysteryGame;

import cn.nukkit.command.CommandSender;
import cn.nukkit.utils.TextFormat;

public class MMStartCommand extends BasicSubCommand {

    private final MurderMysteryGame game;
    
    public MMStartCommand(MurderMysteryGame game) {
        super("start");
        this.setAliases(new String[] {
				"start"
		});

        this.game = game;
    }

    @Override
	public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (!sender.isOp()) {
            sender.sendMessage(TextFormat.RED + "No permission!");
            return true;
        }
        
        sender.sendMessage(TextFormat.GREEN + "Force starting mm game...");
        game.forceStart();

        return true;
    }
}
