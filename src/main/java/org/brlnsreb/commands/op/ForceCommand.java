package org.brlnsreb.commands.op;

import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.utils.messages.ChatMsgs;
import org.powernukkitx.command.Command;
import org.powernukkitx.command.CommandResult;
import org.powernukkitx.command.SenderType;
import org.powernukkitx.command.route.RouteTree;
import org.powernukkitx.command.route.node.RouteNode;
import org.powernukkitx.plugin.annotation.CommandDefinition;

@CommandDefinition(
    name = "force", 
    permission = "admin",
    description = "Force match start or stop",
    usage = "/force <start|stop>"
)

public class ForceCommand extends Command {

    @Override
    public void buildCommandTree(RouteTree tree) {
        tree.getRoot().senderType(SenderType.PLAYER)
            .then(RouteNode.literal("start").exec(ctx -> {
                CustomPlayer player = (CustomPlayer) ctx.getSender();

                if (player.matchCurrent == null) {
                    return CommandResult.fail(ChatMsgs.ERROR_PFX + "You are not in a waiting lobby.");
                }

                switch (player.matchCurrent.state()) {
                    case WAITING_LOBBY, LOBBY_COUNTDOWN:
                        player.matchCurrent.getWaitingLobby().forceStart();
                        return CommandResult.success();

                    default:
                        return CommandResult.fail(ChatMsgs.ERROR_PFX + "You are not in a waiting lobby.");
                }
            }))
            .then(RouteNode.literal("stop").exec(ctx -> {
                CustomPlayer player = (CustomPlayer) ctx.getSender();

                if (player.matchCurrent == null) {
                    return CommandResult.fail(ChatMsgs.ERROR_PFX + "You are not in a match.");
                }

                player.matchCurrent.forceStop();
                return CommandResult.success();
            }));
    }

}
