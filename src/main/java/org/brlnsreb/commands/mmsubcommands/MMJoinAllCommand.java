package org.brlnsreb.commands.mmsubcommands;

import java.util.Map;
import java.util.UUID;

import org.brlnsreb.BrlnsReb;
import org.brlnsreb.commands.subcommands.BasicSubCommand;
import org.brlnsreb.mm.MurderMysteryGame;

import cn.nukkit.command.CommandSender;
import cn.nukkit.utils.TextFormat;
import cn.nukkit.Player;

public class MMJoinAllCommand extends BasicSubCommand {

    private final BrlnsReb plugin;
    private final MurderMysteryGame game;
    
    public MMJoinAllCommand(BrlnsReb plugin, MurderMysteryGame game) {
        super("joinall");
        this.setAliases(new String[] {
				"joinall"
		});

        this.plugin = plugin;
        this.game = game;
    }

    @Override
	public boolean execute(CommandSender sender, String commandLabel, String[] args) {

        /*
        if (!sender.isOp()) {
            sender.sendMessage(TextFormat.RED + "No permission!");
            return true;
        }*/

        Map<UUID, Player> playersJoining = plugin.getServer().getOnlinePlayers();

        for (Map.Entry<UUID, Player> entry : playersJoining.entrySet()) {
            int out = game.joinPlayer(entry.getValue());

            if (out == 0) {
                sender.sendMessage(TextFormat.GREEN + "You joined the game!");
            }
        }

        return true;

    }
}
