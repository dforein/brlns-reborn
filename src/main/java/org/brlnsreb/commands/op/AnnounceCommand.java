package org.brlnsreb.commands.op;

import org.brlnsreb.BrlnsReb;
import org.brlnsreb.listeners.general.ChatListener;
import org.powernukkitx.Player;
import org.powernukkitx.Server;
import org.powernukkitx.command.Command;
import org.powernukkitx.command.CommandSender;
import org.powernukkitx.plugin.annotation.CommandDefinition;
import org.powernukkitx.plugin.annotation.CommandDefinition.CommandMode;
import org.powernukkitx.utils.TextFormat;

@CommandDefinition (
    name = "announce",
    permission = "admin",
    description = "Make a global announcement",
    commandMode = CommandMode.RAW
)

public class AnnounceCommand extends Command {

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (!sender.isOp()) {
            sender.sendMessage(TextFormat.RED + "No permission!");
            return true;
        }

        String message = "§l§5Announcement §r§6" + (sender instanceof Player 
            ? ChatListener.getMessage(args[0])
            : String.join(" ", args)
        );
        Server.getInstance().broadcastMessage(message, Server.getInstance().getOnlinePlayers().values());
        BrlnsReb.logger.info(message);

        return true;
    }

}