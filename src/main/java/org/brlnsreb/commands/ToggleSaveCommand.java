package org.brlnsreb.commands;

import org.brlnsreb.MinigameCore;

import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.utils.TextFormat;

public class ToggleSaveCommand extends Command {

    private final MinigameCore plugin;
    
    public ToggleSaveCommand(MinigameCore plugin) {
        super("togglesave");
        this.setDescription("Toggle saving worlds at server shutdown");
        this.setPermission("admin");

        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (!sender.isOp()) {
            sender.sendMessage(TextFormat.RED + "No permission!");
            return true;
        }

        if (plugin.getSave()) {
            plugin.setSave(false);
            sender.sendMessage(TextFormat.RED + "Saving disabled");
        } else {
            plugin.setSave(true);
            sender.sendMessage(TextFormat.GREEN + "Saving enabled");
        }
        return true;
    }

}
