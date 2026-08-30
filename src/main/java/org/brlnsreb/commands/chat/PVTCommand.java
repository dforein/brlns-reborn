package org.brlnsreb.commands.chat;

import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.core.player.PlayerUtils;
import org.brlnsreb.core.player.data.PlayerData;
import org.brlnsreb.listeners.general.ChatListener;
import org.brlnsreb.utils.messages.ChatMsgs;
import org.powernukkitx.command.Command;
import org.powernukkitx.command.CommandContext;
import org.powernukkitx.command.CommandResult;
import org.powernukkitx.command.SenderType;
import org.powernukkitx.command.route.RouteTree;
import org.powernukkitx.command.route.node.RouteNode;
import org.powernukkitx.command.tree.node.StringNode;
import org.powernukkitx.plugin.annotation.CommandDefinition;

@CommandDefinition(
    name = "pvt",
    description = "Send a private message",
    usage = ChatMsgs.INFO_PFX + "Usage: §e/pvt <player> <message>"
)

public class PVTCommand extends Command {

    @Override
    public void buildCommandTree(RouteTree tree) {
        tree.getRoot().senderType(SenderType.PLAYER)                    //whatever player, also non-logged
            .then(RouteNode.argument("player", new StringNode())
                .then(RouteNode.argument("message", new StringNode())
                    .exec(ctx -> {
                        CustomPlayer sender = (CustomPlayer) ctx.getSender();
                        CustomPlayer receiver = PlayerUtils.getPlayer((String) ctx.getArg("player"));

                        if (receiver == null) {
                            return CommandResult.fail(ChatMsgs.ERROR_PFX + "No player found with such name");     //TEXT
                        }

                        return sendPVT(ctx, sender, receiver);
                    })))
            ;//.orElse(ctx -> ctx.getSender().sendMessage(usageMessage)); TODO: enable
    }

    public static CommandResult sendPVT(CommandContext ctx, CustomPlayer sender, CustomPlayer receiver) {
        PlayerData data;
        String message = ChatListener.getMessage(ctx);

        data = receiver.data;
        sender.sendMessage(
            "§l§aPVT§r §3%s §7> §3%s§7: §b%s".formatted(       //TEXT
                "you",
                data.isLogged() ? data.name : receiver.getDisplayName(),
                message
            )
        );

        data = sender.data;
        receiver.sendMessage(
            "§l§aPVT§r §3%s §7> §3%s§7: §b%s".formatted(        //TEXT
                data.isLogged() ? data.name : sender.getDisplayName(),
                "you",
                message
            )
        );

        receiver.data.setLastPvtPlayer(sender);

        return CommandResult.success();
    }

}
