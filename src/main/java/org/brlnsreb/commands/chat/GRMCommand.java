package org.brlnsreb.commands.chat;

import java.util.List;

import org.brlnsreb.core.minigame.Minigame;
import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.core.player.data.PlayerData;
import org.brlnsreb.mainhub.MainHub;
import org.brlnsreb.utils.messages.ChatMsgs;
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
    usage = ChatMsgs.INFO_PFX + "Usage: §e/grm <message>"
)

public class GRMCommand extends Command {

    @Override
    public void buildCommandTree(RouteTree tree) {
        tree.getRoot().senderType(SenderType.PLAYER)
            .then(RouteNode.argument("message", new StringNode())
                .exec(ctx -> {
                    CustomPlayer sender = (CustomPlayer) ctx.getSender();

                    PlayerData senderData = sender.data;
                    if (!senderData.isLogged()) {
                        return CommandResult.fail(ChatMsgs.ERROR_PFX + "You are not logged in!");  //TEXT
                    }
                    
                    Minigame minigame = sender.minigameCurrent;
                    List<String> receivers = sender.data.getOnlineFriendsKeysCopy();

                    for (String name : receivers) {
                        CustomPlayer friend = senderData.getFriend(name);
                        if (friend == null) continue;
                        
                        friend.sendMessage(
                            "§l§aGRM %s §3%s§7: §7%s".formatted(      //TEXT
                                minigame != null ? minigame.mgt.displayNameTagP : MainHub.displayNameTagP,
                                senderData.name,
                                ctx.getArg("message")
                            )
                        );
                    }

                    sender.sendMessage(
                        "§l§aGRM §dGLOBAL§r §a%d §eyou§7: §7%s".formatted(      //TEXT
                            receivers.size(),
                            ctx.getArg("message")
                        )
                    );

                    return CommandResult.success();
                }))
            ;//.orElse(ctx -> ctx.getSender().sendMessage(usageMessage)); TODO: enable
    }

}
