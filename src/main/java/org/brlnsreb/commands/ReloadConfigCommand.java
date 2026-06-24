package org.brlnsreb.commands;

import org.brlnsreb.core.auth.AuthSystem;
import org.brlnsreb.core.minigame.MinigameManager;
import org.brlnsreb.generallobby.GeneralLobby;
import org.brlnsreb.utils.YamlUtil;

import cn.nukkit.command.CommandSender;
import cn.nukkit.plugin.annotation.Command;
import cn.nukkit.utils.TextFormat;

@Command(
    name = "reloadconfig", 
    permission = "admin",
    description = "Reload all configs"
)

public class ReloadConfigCommand extends cn.nukkit.command.Command {
    
    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (!sender.isOp()) return true;

        MinigameManager.reloadConfig();
        GeneralLobby.getInstance().reloadConfig();
        YamlUtil.resetCache();
        AuthSystem.reloadConfig();

        sender.sendMessage(TextFormat.GREEN + "Config file reloaded!");
        return true;
    }
}