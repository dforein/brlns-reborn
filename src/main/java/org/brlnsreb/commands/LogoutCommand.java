package org.brlnsreb.commands;

import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.core.player.data.database.PlayerDataManager;
import org.brlnsreb.utils.ChatMsgs;
import org.powernukkitx.command.Command;
import org.powernukkitx.command.CommandSender;
import org.powernukkitx.plugin.annotation.CommandDefinition;
import org.powernukkitx.plugin.annotation.CommandDefinition.CommandMode;
import org.powernukkitx.utils.TextFormat;

@CommandDefinition(
    name = "logout",
    description = "Log-out from your account",
    commandMode = CommandMode.RAW
)

public class LogoutCommand extends Command {
    
    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (!(sender instanceof CustomPlayer)) {
            sender.sendMessage(TextFormat.RED + "Only players can use this command!");
            return true;
        }

        CustomPlayer player = (CustomPlayer) sender;
        PlayerDataManager.playerLogout(player).thenAccept(outcome -> {
            sender.sendMessage(switch (outcome) {
                case ASYNC_TASK_ALREADY_RUNNING -> ChatMsgs.ERROR_PFX + "Retry in a few seconds.";
                case PLAYER_ALREADY_LOGGED_OUT -> ChatMsgs.INFO_PFX + "You are already logged out!";
                case OK -> ChatMsgs.SUCCESS_PFX + "You logged out from your account.";
                case DB_ERROR -> ChatMsgs.ERROR_PFX + "Report this error to developers: DB_ERROR";
                default -> ChatMsgs.ERROR_PFX + "Report this error to developers: LOGOUT ERROR";
            });
        });

        return true;
    }

}
