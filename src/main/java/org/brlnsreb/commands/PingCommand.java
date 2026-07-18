package org.brlnsreb.commands;

import org.brlnsreb.utils.ChatMsgs;
import org.brlnsreb.utils.ChatMsgs.Alignment;
import org.powernukkitx.Player;
import org.powernukkitx.command.Command;
import org.powernukkitx.command.CommandResult;
import org.powernukkitx.command.SenderType;
import org.powernukkitx.command.route.RouteTree;
import org.powernukkitx.plugin.annotation.CommandDefinition;
import org.powernukkitx.utils.TextFormat;

//TODO: rewrite ping command

@CommandDefinition(
    name = "ping",
    description = "Check your ping",
    usage = "/ping or /ping <player>"
)

public class PingCommand extends Command {
    
    public PingCommand() {
        this.enableCommandTree();
    }
    
    @Override
    public void buildCommandTree(RouteTree tree) {
        tree.getRoot().senderType(SenderType.PLAYER)
            .exec(ctx -> {
                Player player = (Player) ctx.getSender();
                long ping = player.getPing(); 
                
                player.sendMessage(ChatMsgs.buildString(Alignment.LEFT, 
                    "§e--- §l§dConnection status§r §e---",
                    "§ePing/latency: " + getPingColor(ping) + ping + "ms"
                    //TODO ping
                ));

                return CommandResult.success();
            });
    }
    
    private TextFormat getPingColor(long ping) {
        if (ping < 80) {
            return TextFormat.GREEN;
        } else if (ping < 180) {
            return TextFormat.YELLOW;
        } else if (ping < 300) {
            return TextFormat.GOLD;
        } else {
            return TextFormat.RED;
        }
    }

}
