package org.brlnsreb.commands;

import org.brlnsreb.core.auth.AuthSystem;
import org.brlnsreb.core.minigame.MinigameManager;
import org.brlnsreb.generallobby.GeneralLobby;
import org.brlnsreb.utils.YamlUtil;

import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.utils.TextFormat;

public class ReloadConfigCommand extends Command {
    
    public ReloadConfigCommand() {
        super("reloadconfig");
        this.setDescription("Reload config.yml");
        this.setPermission("admin");
    }

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