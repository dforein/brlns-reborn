package org.brlnsreb.commands.op;

import org.brlnsreb.BrlnsReb;
import org.brlnsreb.core.minigame.MinigameType;
import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.mainhub.MainHub;
import org.brlnsreb.utils.ChatMsgs;
import org.brlnsreb.utils.ChatMsgs.Alignment;
import org.cloudburstmc.math.vector.Vector3f;
import org.cloudburstmc.protocol.bedrock.data.Dimension;
import org.cloudburstmc.protocol.bedrock.data.payload.common.DimensionType;
import org.cloudburstmc.protocol.bedrock.packet.ChangeDimensionPacket;
import org.powernukkitx.command.Command;
import org.powernukkitx.command.CommandContext;
import org.powernukkitx.command.CommandResult;
import org.powernukkitx.command.CommandSender;
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
            .then(RouteNode.literal("msg")
                .then(RouteNode.argument("arg", new MessageStringNode()).exec(ctx -> {
                    ctx.getSender().sendMessage((String) ctx.getArg("arg"));
                    return CommandResult.success();
            })))
            .then(RouteNode.literal("block")
                .exec(ctx -> {
                    CommandSender s = ctx.getSender();
                    s.sendMessage(ChatMsgs.BAR);
                    s.sendMessage("§2-§r");
                    s.sendMessage(ChatMsgs.buildBlockContent(Alignment.CENTER,
                        ChatMsgs.BROKENLENS_GAMES,
                        "",
                        "§7- " + MinigameType.MURDER_MYSTERY.displayName + " §7-",
                        "",
                        "§7 Starting in 10 seconds..."
                    ));
                    s.sendMessage("§2-§r");
                    s.sendMessage(ChatMsgs.BAR);
                    return CommandResult.success();
            }))
            .then(RouteNode.literal("loadingscreen").exec(ctx -> {
                CustomPlayer p = getPlayer(ctx);

                sendCDPk(p, Dimension.NETHER);
                scheduleDelayedTask(2, () -> sendCDPk(p, null));
                scheduleDelayedTask(4, () -> {
                    p.teleport(MainHub.instance.getMap().spawn);
                    sendCDPk(p, Dimension.OVERWORLD);
                });
                scheduleDelayedTask(6, () -> sendCDPk(p, null));

                return CommandResult.success();
            }));

    }

    private CustomPlayer getPlayer(CommandContext ctx) {
        return (CustomPlayer) ctx.getSender();
    }

    private void sendCDPk(CustomPlayer p, Dimension dim) {
        final ChangeDimensionPacket pk = new ChangeDimensionPacket();
        pk.setDimension(DimensionType.from(dim));
        pk.setPosition(Vector3f.from((float) p.getX(), (float) p.getY(), (float) p.getZ()));
        pk.setRespawn(false);
        pk.setLoadingScreenId(2831038);
        p.sendPacket(pk);
    }

    private void scheduleDelayedTask(int seconds, Runnable task) {
        BrlnsReb.getScheduler().scheduleDelayedTask(BrlnsReb.instance, task, seconds*20);
    }

}
