package org.brlnsreb.commands.chat;

import org.brlnsreb.core.player.CustomPlayer;

import cn.nukkit.command.Command;
import cn.nukkit.command.CommandResult;
import cn.nukkit.command.SenderType;
import cn.nukkit.command.route.RouteTree;
import cn.nukkit.command.route.node.RouteNode;
import cn.nukkit.command.tree.node.StringNode;
import cn.nukkit.plugin.annotation.CommandDefinition;

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
        tree.getRoot().senderType(SenderType.PLAYER)
            .then(RouteNode.argument("message", new StringNode())
                .exec(ctx -> {
                    CustomPlayer replyingPlayer = (CustomPlayer) ctx.getSender();
                    CustomPlayer originalPvtPlayer = replyingPlayer.getPlayerData().getLastPvtPlayer();

                    if (originalPvtPlayer == null) {
                        return CommandResult.fail("§l§cERROR§r§c You didn't receive any PVT!");     //TEXT
                    }

                    originalPvtPlayer.sendMessage(
                        "§l§aPVT§r §3" 
                        + replyingPlayer.getPlayerData().name 
                        + " §7> §3you§7: §b" 
                        + ctx.getArg("message")
                    );

                    return CommandResult.success();
                }));
    }

}
