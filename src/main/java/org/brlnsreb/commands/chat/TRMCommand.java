package org.brlnsreb.commands.chat;

import java.util.List;

import org.brlnsreb.core.minigame.match.MatchTeam;
import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.listeners.general.ChatListener;
import org.brlnsreb.utils.messages.ChatMsgs;
import org.powernukkitx.command.Command;
import org.powernukkitx.command.CommandResult;
import org.powernukkitx.command.SenderType;
import org.powernukkitx.command.route.RouteTree;
import org.powernukkitx.command.route.node.RouteNode;
import org.powernukkitx.command.tree.node.StringNode;
import org.powernukkitx.plugin.annotation.CommandDefinition;

@CommandDefinition(
    name = "trm",
    description = "Talk to your teammate/s in the match",
    usage = ChatMsgs.INFO_PFX + "Usage: §e/trm <message>"
)

public class TRMCommand extends Command {

    @Override
    public void buildCommandTree(RouteTree tree) {
        tree.getRoot().senderType(SenderType.PLAYER)
            .then(RouteNode.argument("message", new StringNode())
                .exec(ctx -> {
                    CustomPlayer sender = (CustomPlayer) ctx.getSender();

                    if (!sender.isPlaying() || sender.matchCurrent.getGame().isPregameCountdown()) {
                        return CommandResult.fail(ChatMsgs.ERROR_PFX + "You are not playing a started match!");  //TEXT
                    }

                    if (sender.matchCurrent instanceof MatchTeam match) {
                        String message = ChatListener.getMessage(ctx);

                        List<CustomPlayer> receivers = match.getTeamGame().getTeamManager().getTeam(sender);
                        receivers.remove(sender);

                        for (CustomPlayer r : receivers) {
                            if (!sender.matchCurrent.getPlayers().contains(r)) continue;
                            r.sendMessage(
                                "§l§aTRM§r §3%s§7: §7%s".formatted(      //TEXT
                                    sender.data.name,
                                    message
                                )
                            );
                        }

                        sender.sendMessage(
                            "§l§aTRM§r §a%d §eyou§7: §7%s".formatted(      //TEXT
                                receivers.size(),
                                message
                            )
                        );

                        return CommandResult.success();
                    } else {
                        return CommandResult.fail(ChatMsgs.ERROR_PFX + "You are playing a solo minigame!");     //TEXT
                    }
                }))
            ;//.orElse(ctx -> ctx.getSender().sendMessage(usageMessage)); TODO: enable
    }

}