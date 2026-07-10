package org.brlnsreb.commands;

import org.brlnsreb.BrlnsReb;

import org.powernukkitx.command.CommandSender;
import org.powernukkitx.plugin.annotation.CommandDefinition;
import org.powernukkitx.utils.TextFormat;

@CommandDefinition(
    name = "globalchat",
    permission = "admin",
    description = "Toggle global server chat or local level chat"
)

public class GlobalChatCommand extends cn.nukkit.command.Command {

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (!sender.isOp()) {
            sender.sendMessage(TextFormat.RED + "No permission!");
            return true;
        }

        if (BrlnsReb.getGlobalChat()) {
            BrlnsReb.setGlobalChat(false);
            sender.sendMessage(TextFormat.RED + "Global Chat disabled");
        } else {
            BrlnsReb.setGlobalChat(true);
            sender.sendMessage(TextFormat.GREEN + "Global Chat enabled");
        }
        return true;
    }

}
