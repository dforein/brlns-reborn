package org.brlnsreb.commands.chat;

import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.utils.ChatMsgs;
import org.powernukkitx.command.Command;
import org.powernukkitx.command.CommandResult;
import org.powernukkitx.command.SenderType;
import org.powernukkitx.command.route.RouteTree;
import org.powernukkitx.command.route.node.RouteNode;
import org.powernukkitx.command.tree.node.RawTextNode;
import org.powernukkitx.plugin.annotation.CommandDefinition;

@CommandDefinition(
    name = "reply",
    description = "Reply to the last PVT you've received",
    usage = "/reply <message>"
)

public class ReplyCommand extends Command {
    
    public ReplyCommand() {
        this.enableCommandTree();
    }

    @Override
    public void buildCommandTree(RouteTree tree) {
        tree.getRoot().senderType(SenderType.PLAYER)                   //whatever player, also non-logged
            .then(RouteNode.argument("message", new RawTextNode())
                .exec(ctx -> {
                    CustomPlayer sender = (CustomPlayer) ctx.getSender();
                    CustomPlayer receiver = sender.getPlayerData().getLastPvtPlayer();

                    if (receiver == null) {
                        return CommandResult.fail(ChatMsgs.errorPfx + "You didn't receive any PVT!");     //TEXT
                    }

                    sender.sendMessage(
                        "§l§aPVT§r §3%s §7> §3%s§7: §b%s".formatted(       //TEXT
                            "you",
                            receiver.getPlayerData().name,
                            ctx.getArg("message")
                        )
                    );

                    receiver.sendMessage(
                        "§l§aPVT§r §3%s §7> §3%s§7: §b%s".formatted(        //TEXT
                            sender.getPlayerData().name,
                            "you",
                            ctx.getArg("message")
                        )
                    );

                    return CommandResult.success();
                }));
    }

}
