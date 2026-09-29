package org.brlnsreb.commands.maintenance;

import org.brlnsreb.BrlnsReb;
import org.powernukkitx.command.Command;
import org.powernukkitx.command.CommandSender;
import org.powernukkitx.utils.TextFormat;

public class ToggleSaveCommand extends Command {

    public ToggleSaveCommand() {
        super("togglesave");
        setDescription("Toggle saving worlds at server shutdown");
        setPermission("admin");
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (!sender.isOp()) {
            sender.sendMessage(TextFormat.RED + "No permission!");
            return true;
        }

        if (BrlnsReb.getSave()) {
            BrlnsReb.setSave(false);
            sender.sendMessage(TextFormat.RED + "Saving disabled");
        } else {
            BrlnsReb.setSave(true);
            sender.sendMessage(TextFormat.GREEN + "Saving enabled");
        }
        return true;
    }

}
