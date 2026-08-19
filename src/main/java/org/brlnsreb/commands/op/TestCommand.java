package org.brlnsreb.commands.op;

import java.util.List;

import org.powernukkitx.command.Command;
import org.powernukkitx.command.CommandResult;
import org.powernukkitx.command.SenderType;
import org.powernukkitx.command.route.RouteTree;
import org.powernukkitx.command.route.node.RouteNode;
import org.powernukkitx.command.tree.node.StringNode;
import org.powernukkitx.plugin.annotation.CommandDefinition;

@CommandDefinition(
    name = "test",
    permission = "admin",
    description = "Test command for debug"
)


public class TestCommand extends Command {
    
    public TestCommand() {
        this.enableCommandTree();
    }

    @Override
    public void buildCommandTree(RouteTree tree) {
        tree.getRoot().senderType(SenderType.ANY)
            .then(RouteNode.argument("arg", new StringNode()).exec(ctx -> {
                return CommandResult.success();
            })
                .suggest(List.of(
                ""
                )));

    }

}
