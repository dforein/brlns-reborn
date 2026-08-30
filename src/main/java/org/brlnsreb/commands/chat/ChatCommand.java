package org.brlnsreb.commands.chat;

import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.utils.messages.ChatMsgs;
import org.powernukkitx.command.Command;
import org.powernukkitx.command.CommandSender;
import org.powernukkitx.plugin.annotation.CommandDefinition;
import org.powernukkitx.plugin.annotation.CommandDefinition.CommandMode;
import org.powernukkitx.utils.TextFormat;

@CommandDefinition(
    name = "chat",
    description = "Enable/Disable the chat",
    commandMode = CommandMode.RAW
)

public class ChatCommand extends Command {
    
    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (sender instanceof CustomPlayer player) {
            player.data.toggleChatVisible();
            player.sendMessage(ChatMsgs.SUCCESS_PFX + (player.data.isChatVisible() ? "Enabled" : "Disabled") + "the chat.");
        } else {
            sender.sendMessage(TextFormat.RED + "Only players can use this command!");
        }
        return true;
    }

}
