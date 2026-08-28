package org.brlnsreb.commands.op;

import org.brlnsreb.core.minigame.MinigameType;
import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.utils.ChatMsgs;
import org.brlnsreb.utils.ChatMsgs.Alignment;
import org.powernukkitx.command.Command;
import org.powernukkitx.command.CommandResult;
import org.powernukkitx.command.CommandSender;
import org.powernukkitx.command.SenderType;
import org.powernukkitx.command.route.RouteTree;
import org.powernukkitx.command.route.node.RouteNode;
import org.powernukkitx.command.tree.node.DoubleNode;
import org.powernukkitx.command.tree.node.MessageStringNode;
import org.powernukkitx.plugin.annotation.CommandDefinition;
import org.powernukkitx.utils.TextFormat;

@CommandDefinition(
    name = "test",
    permission = "admin",
    description = "Test command for debug"
)


public class TestCommand extends Command {

    @Override
    public void buildCommandTree(RouteTree tree) {
        tree.getRoot().senderType(SenderType.ANY)
            .then(RouteNode.literal("msg")
                .then(RouteNode.argument("arg", new MessageStringNode()).exec(ctx -> {
                    ctx.getSender().sendMessage((String) ctx.getArg("arg"));
                    return CommandResult.success();
            })))
            .then(RouteNode.literal("block")
                .then(RouteNode.argument("coeff", new DoubleNode()).exec(ctx -> {
                    CommandSender s = ctx.getSender();
                    s.sendMessage(ChatMsgs.BAR);
                    s.sendMessage("§2-§r");
                    s.sendMessage(buildBlockContent(Alignment.CENTER, ctx.getArg("coeff"),
                        ChatMsgs.BROKENLENS_GAMES,
                        "",
                        "§7- " + MinigameType.MURDER_MYSTERY.displayName + " §7-",
                        "",
                        "§7 Starting in 10 seconds..."
                    ));
                    s.sendMessage("§2-§r");
                    s.sendMessage(ChatMsgs.BAR);
                    return CommandResult.success();
            })));

    }

    private String buildBlockContent(Alignment alignment, double coeff, String... lines) {
        StringBuilder strBuilder = new StringBuilder();
        int i, spaces;

        for (String line : lines) {
            strBuilder.append("§2-§r");

            if (alignment == Alignment.CENTER) {
                spaces = (int) (40 - TextFormat.clean(line).length() * coeff - 1.5) / 2;
                for (i = 0; i < spaces; i++) strBuilder.append("§l §r");
            } else if (alignment == Alignment.LEFT) {
                strBuilder.append("§l §r");
            }

            strBuilder.append(line);
            strBuilder.append("§r\n");
        }

        return strBuilder.toString();
    }

}
