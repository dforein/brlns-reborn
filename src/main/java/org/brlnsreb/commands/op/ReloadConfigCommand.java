package org.brlnsreb.commands.op;

import org.brlnsreb.core.minigame.MinigameManager;
import org.brlnsreb.mainhub.MainHub;
import org.brlnsreb.utils.config.Configs;
import org.brlnsreb.utils.config.YamlUtil;
import org.brlnsreb.utils.messages.ChatMsgs;
import org.powernukkitx.command.Command;
import org.powernukkitx.command.CommandSender;
import org.powernukkitx.plugin.annotation.CommandDefinition;
import org.powernukkitx.plugin.annotation.CommandDefinition.CommandMode;

@CommandDefinition(
    name = "reloadconfig", 
    permission = "admin",
    description = "Reload all configs",
    commandMode = CommandMode.RAW
)

public class ReloadConfigCommand extends Command {
    
    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (!sender.isOp()) return true;

        Configs.reloadConfig();
        YamlUtil.resetCache();

        MinigameManager.onConfigReload();
        MainHub.instance.onConfigReload();

        sender.sendMessage(ChatMsgs.SUCCESS_PFX + "Config files reloaded!");
        return true;
    }
}