package org.brlnsreb.commands;

import org.brlnsreb.core.Configs;
import org.brlnsreb.core.minigame.MinigameManager;
import org.brlnsreb.mainhub.MainHub;
import org.brlnsreb.utils.YamlUtil;
import org.powernukkitx.command.Command;
import org.powernukkitx.command.CommandSender;
import org.powernukkitx.plugin.annotation.CommandDefinition;
import org.powernukkitx.utils.TextFormat;

@CommandDefinition(
    name = "reloadconfig", 
    permission = "admin",
    description = "Reload all configs"
)

public class ReloadConfigCommand extends Command {
    
    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (!sender.isOp()) return true;

        Configs.reloadConfig();
        YamlUtil.resetCache();

        MinigameManager.onConfigReload();
        MainHub.instance.onConfigReload();

        sender.sendMessage(TextFormat.GREEN + "Config file reloaded!");
        return true;
    }
}