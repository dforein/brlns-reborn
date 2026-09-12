package org.brlnsreb.commands.maintenance.physics;

import org.brlnsreb.core.levels.LevelManager;
import org.powernukkitx.Player;
import org.powernukkitx.command.Command;
import org.powernukkitx.command.CommandResult;
import org.powernukkitx.command.SenderType;
import org.powernukkitx.command.route.RouteTree;
import org.powernukkitx.command.route.node.RouteNode;

public class PhysicsCommand extends Command {

    public PhysicsCommand() {
        super("physics");
        setDescription("Check or edit world physics");
        setPermission("admin");

        enableCommandTree();
    }

    @Override
    public void buildCommandTree(RouteTree tree) {
        tree.getRoot().senderType(SenderType.PLAYER)
            .then(RouteNode.literal("check").exec(ctx -> {
                Player player = (Player) ctx.getSender();
                Integer range = LevelManager.getPhysicsRangeIn(player.getLevel());
                if (range != null) {
                    player.sendMessage("§2Physics enabled in your level. Block range: " + (range > 0 ? range : "All world"));
                } else {
                    player.sendMessage("§6Physics not enabled in your level");
                }
                return CommandResult.success();
            }))
            .then(RouteNode.literal("settings").exec(ctx -> {
                PhysicsForm.openForm((Player) ctx.getSender());
                return CommandResult.success();
            }));
    }

}
