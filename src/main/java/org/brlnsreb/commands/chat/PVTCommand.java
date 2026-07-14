package org.brlnsreb.commands.chat;

import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.core.player.PlayerUtils;
import org.brlnsreb.core.player.data.PlayerData;
import org.brlnsreb.utils.ChatMsgs;
import org.powernukkitx.command.Command;
import org.powernukkitx.command.CommandResult;
import org.powernukkitx.command.SenderType;
import org.powernukkitx.command.route.RouteTree;
import org.powernukkitx.command.route.node.RouteNode;
import org.powernukkitx.command.tree.node.RawTextNode;
import org.powernukkitx.command.tree.node.StringNode;
import org.powernukkitx.plugin.annotation.CommandDefinition;

@CommandDefinition(
    name = "pvt",
    description = "Send a private message",
    usage = "/pvt <player> <message>"
)

public class PVTCommand extends Command {
    
    public PVTCommand() {
        this.enableCommandTree();
    }

    @Override
    public void buildCommandTree(RouteTree tree) {
        tree.getRoot().senderType(SenderType.PLAYER)                    //whatever player, also non-logged
            .then(RouteNode.argument("player", new StringNode())
                .then(RouteNode.argument("message", new RawTextNode())
                    .exec(ctx -> {
                        CustomPlayer sender = (CustomPlayer) ctx.getSender();
                        CustomPlayer receiver = PlayerUtils.getPlayer((String) ctx.getArg("player"));

                        if (receiver == null) {
                            return CommandResult.fail(ChatMsgs.ERROR_PFX + "No player found with such name");     //TEXT
                        }

                        PlayerData data;
                        data = receiver.data;
                        sender.sendMessage(
                            "§l§aPVT§r §3%s §7> §3%s§7: §b%s".formatted(       //TEXT
                                "you",
                                data.isLogged() ? data.name : receiver.getDisplayName(),
                                ctx.getArg("message")
                            )
                        );

                        data = sender.data;
                        receiver.sendMessage(
                            "§l§aPVT§r §3%s §7> §3%s§7: §b%s".formatted(        //TEXT
                                 data.isLogged() ? data.name : sender.getDisplayName(),
                                "you",
                                ctx.getArg("message")
                            )
                        );

                        receiver.data.setLastPvtPlayer(sender);

                        return CommandResult.success();
                    })));
    }

}
