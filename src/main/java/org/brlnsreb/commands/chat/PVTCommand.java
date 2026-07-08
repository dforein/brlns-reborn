package org.brlnsreb.commands.chat;

import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.core.player.PlayerUtils;

import cn.nukkit.command.Command;
import cn.nukkit.command.CommandResult;
import cn.nukkit.command.SenderType;
import cn.nukkit.command.route.RouteTree;
import cn.nukkit.command.route.node.RouteNode;
import cn.nukkit.command.tree.node.PlayersNode;
import cn.nukkit.command.tree.node.StringNode;
import cn.nukkit.plugin.annotation.CommandDefinition;

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
        tree.getRoot().senderType(SenderType.PLAYER)
            .then(RouteNode.argument("player", new PlayersNode())
                .then(RouteNode.argument("message", new StringNode()))
                    .exec(ctx -> {
                        CustomPlayer receiver = PlayerUtils.getPlayer(ctx.getArg("player"));

                        if (receiver == null) {
                            return CommandResult.fail("§l§cERROR§r§c No player found with such name");     //TEXT
                        }

                        receiver.sendMessage(
                            "§l§aPVT§r §3" 
                            + ctx.getSender()
                            + " §7> §3you§7: §b" 
                            + ctx.getArg("message")
                        );

                        return CommandResult.success();
                    }));
    }

}
