package com.brlnsreb.minigames.commands;

import com.brlnsreb.minigames.MinigameCore;

import org.powernukkitx.command.Command;
import org.powernukkitx.command.CommandSender;
import org.powernukkitx.utils.TextFormat;

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
