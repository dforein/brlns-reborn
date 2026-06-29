package org.brlnsreb.commands;

import org.brlnsreb.core.ConfigManager;
import org.brlnsreb.core.minigame.MinigameManager;
import org.brlnsreb.generallobby.GeneralLobby;
import org.brlnsreb.utils.YamlUtil;

import cn.nukkit.command.CommandSender;
import cn.nukkit.plugin.annotation.CommandDefinition;
import cn.nukkit.utils.TextFormat;

@CommandDefinition(
    name = "reloadconfig", 
    permission = "admin",
    description = "Reload all configs"
)

public class ReloadConfigCommand extends cn.nukkit.command.Command {
    
    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (!sender.isOp()) return true;

        ConfigManager.reloadConfigs();
        YamlUtil.resetCache();

        MinigameManager.reloadConfig();
        GeneralLobby.getInstance().reloadConfig();

        sender.sendMessage(TextFormat.GREEN + "Config file reloaded!");
        return true;
    }
}