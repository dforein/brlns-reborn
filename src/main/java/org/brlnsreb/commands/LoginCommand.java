package org.brlnsreb.commands;

import org.brlnsreb.core.auth.AuthSystem;
import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.utils.messages.ChatMsgs;
import org.powernukkitx.command.Command;
import org.powernukkitx.command.CommandSender;
import org.powernukkitx.plugin.annotation.CommandDefinition;
import org.powernukkitx.plugin.annotation.CommandDefinition.CommandMode;
import org.powernukkitx.utils.TextFormat;

@CommandDefinition(
    name = "login",
    aliases = {"login", "register"},
    description = "Login/Register a §eBroken§6Lens§r account",
    commandMode = CommandMode.RAW
)

public class LoginCommand extends Command {

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (!(sender instanceof CustomPlayer)) {
            sender.sendMessage(TextFormat.RED + "Only players can use this command!");
            return true;
        }

        CustomPlayer player = (CustomPlayer) sender;
        if (player.data.isLogged()) {
            sender.sendMessage(ChatMsgs.INFO_PFX + "You are already logged in!");
            return true;
        }

        AuthSystem.openMenu(player);

        return true;
    }
    
}
