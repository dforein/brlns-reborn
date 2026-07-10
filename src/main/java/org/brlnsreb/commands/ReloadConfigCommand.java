package org.brlnsreb.commands;

import org.brlnsreb.core.ConfigManager;
import org.brlnsreb.core.minigame.MinigameManager;
import org.brlnsreb.generallobby.GeneralLobby;
import org.brlnsreb.utils.YamlUtil;

import org.powernukkitx.command.CommandSender;
import org.powernukkitx.plugin.annotation.CommandDefinition;
import org.powernukkitx.utils.TextFormat;

@CommandDefinition(
    name = "reloadconfig", 
    permission = "admin",
    description = "Reload all configs"
)

public class ReloadConfigCommand extends cn.nukkit.command.Command {
    
    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (!sender.isOp()) return true;

        ConfigManager.reloadConfig();
        YamlUtil.resetCache();

        MinigameManager.onConfigReload();
        GeneralLobby.getInstance().onConfigReload();

        sender.sendMessage(TextFormat.GREEN + "Config file reloaded!");
        return true;
    }
}