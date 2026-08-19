package org.brlnsreb.commands.op;

import org.powernukkitx.command.Command;
import org.powernukkitx.command.CommandResult;
import org.powernukkitx.command.SenderType;
import org.powernukkitx.command.route.RouteTree;
import org.powernukkitx.command.route.node.RouteNode;
import org.powernukkitx.command.tree.node.MessageStringNode;
import org.powernukkitx.plugin.annotation.CommandDefinition;

@CommandDefinition(
    name = "test",
    permission = "admin",
    description = "Test command for debug"
)


public class TestCommand extends Command {

    @Override
    public void buildCommandTree(RouteTree tree) {
        tree.getRoot().senderType(SenderType.ANY)
            .then(RouteNode.literal("arg").exec(ctx -> {

                return CommandResult.success();
            }))
            .then(RouteNode.literal("msg")
                .then(RouteNode.argument("arg", new MessageStringNode()).exec(ctx -> {
                    ctx.getSender().sendMessage((String) ctx.getArg("arg"));
                    return CommandResult.success();
                })
            ));

    }

}
