package org.brlnsreb.commands.chat;

import java.util.List;

import org.brlnsreb.core.minigame.Minigame;
import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.core.player.data.PlayerData;

import org.powernukkitx.command.Command;
import org.powernukkitx.command.CommandResult;
import org.powernukkitx.command.SenderType;
import org.powernukkitx.command.route.RouteTree;
import org.powernukkitx.command.route.node.RouteNode;
import org.powernukkitx.command.tree.node.StringNode;
import org.powernukkitx.plugin.annotation.CommandDefinition;

@CommandDefinition(
    name = "frm",
    description = "Talk to all your online friends in the current server",
    usage = "/frm <message>"
)

public class FRMCommand extends Command {

    public FRMCommand() {
        this.enableCommandTree();
    }
    
    @Override
    public void buildCommandTree(RouteTree tree) {
        tree.getRoot().senderType(SenderType.PLAYER)
            .then(RouteNode.argument("message", new StringNode())
                .exec(ctx -> {
                    CustomPlayer sender = (CustomPlayer) ctx.getSender();

                    PlayerData senderData = sender.data;
                    Minigame minigame = sender.minigameCurrent;
                    List<String> friends = senderData.getOnlineFriendsKeysCopy();

                    int receiversCount = 0;
                    for (String name : friends) {
                        CustomPlayer friend = senderData.getFriend(name);
                        if (friend == null) continue;
                        if (!friend.getLevel().equals(sender.getLevel())) continue;
 
                        friend.sendMessage(
                            "§l§aFRM %s §3%s§7: §7%s".formatted(      //TEXT
                                minigame != null ? minigame.mgt.displayNameTag : "HUB",
                                senderData.name,
                                ctx.getArg("message")
                            )
                        );
                        receiversCount++;
                    }
                    
                    sender.sendMessage(
                        "§l§aFRM §dCURRENT§r §a%d §eyou§7: §7%s".formatted(      //TODO: CURRENT? //TEXT
                            receiversCount,
                            ctx.getArg("message")
                        )
                    );

                    return CommandResult.success();
                }));
    }

}