package org.brlnsreb.commands.chat;

import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.utils.ChatMsgs;
import org.powernukkitx.command.Command;
import org.powernukkitx.command.CommandResult;
import org.powernukkitx.command.SenderType;
import org.powernukkitx.command.route.RouteTree;
import org.powernukkitx.command.route.node.RouteNode;
import org.powernukkitx.command.tree.node.StringNode;
import org.powernukkitx.plugin.annotation.CommandDefinition;

@CommandDefinition(
    name = "reply",
    description = "Reply to the last PVT you've received",
    usage = ChatMsgs.INFO_PFX + "Usage: §e/reply <message>"
)

public class ReplyCommand extends Command {

    @Override
    public void buildCommandTree(RouteTree tree) {
        tree.getRoot().senderType(SenderType.PLAYER)                   //whatever player, also non-logged
            .then(RouteNode.argument("message", new StringNode())
                .exec(ctx -> {
                    CustomPlayer sender = (CustomPlayer) ctx.getSender();
                    CustomPlayer receiver = sender.data.getLastPvtPlayer();

                    if (receiver == null) {
                        return CommandResult.fail(ChatMsgs.ERROR_PFX + "You didn't receive any PVT!");     //TEXT
                    }

                    sender.sendMessage(
                        "§l§aPVT§r §3%s §7> §3%s§7: §b%s".formatted(       //TEXT
                            "you",
                            receiver.data.name,
                            ctx.getArg("message")
                        )
                    );

                    receiver.sendMessage(
                        "§l§aPVT§r §3%s §7> §3%s§7: §b%s".formatted(        //TEXT
                            sender.data.name,
                            "you",
                            ctx.getArg("message")
                        )
                    );

                    return CommandResult.success();
                }))
            .orElse(ctx -> ctx.getSender().sendMessage(usageMessage));
    }

}
