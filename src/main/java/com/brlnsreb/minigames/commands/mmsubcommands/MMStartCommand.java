package com.brlnsreb.minigames.commands.mmsubcommands;

import com.brlnsreb.minigames.commands.subcommands.BasicSubCommand;
import com.brlnsreb.minigames.mm.MurderMysteryGame;

import org.powernukkitx.command.CommandSender;
import org.powernukkitx.utils.TextFormat;

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
