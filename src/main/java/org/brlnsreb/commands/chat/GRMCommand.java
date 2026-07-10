package org.brlnsreb.commands.chat;

import java.util.Set;

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
    name = "grm",
    description = "Talk to all your online friends in the network",
    usage = "/grm <message>"
)

public class GRMCommand extends Command {
    
    public GRMCommand() {
        this.enableCommandTree();
    }

    @Override
    public void buildCommandTree(RouteTree tree) {
        tree.getRoot().senderType(SenderType.PLAYER)
            .then(RouteNode.argument("message", new StringNode())
                .exec(ctx -> {
                    CustomPlayer sender = (CustomPlayer) ctx.getSender();
                    PlayerData senderData = sender.getPlayerData();
                    String minigameNameTag = sender.currentMinigame.getNameTag();
                    Set<String> receivers = sender.getPlayerData().getFriends();

                    for (String name : receivers) {
                        CustomPlayer friend = senderData.getFriend(name);
                        if (friend == null) continue;
                        
                        friend.sendMessage(
                            "§l§aGRM §d%s§r §3%s§7: §7%s".formatted(      //TEXT
                                minigameNameTag != null ? minigameNameTag.toUpperCase() : "HUB",
                                senderData.name,
                                ctx.getArg("message")
                            )
                        );
                    }

                    sender.sendMessage(
                        "§l§aGRM §dGLOBAL§r §a%d §eyou§7: §7%s".formatted(      //TODO: text §7 or §e?  //TEXT
                            receivers.size(),
                            ctx.getArg("message")
                        )
                    );

                    return CommandResult.success();
                }));
    }

}
