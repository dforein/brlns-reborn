package com.brlnsreb.minigames.commands;

import com.brlnsreb.minigames.core.auth.AuthSystem;
import com.brlnsreb.minigames.core.minigame.MinigameManager;
import com.brlnsreb.minigames.generallobby.GeneralLobby;
import com.brlnsreb.minigames.utils.YamlUtil;

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