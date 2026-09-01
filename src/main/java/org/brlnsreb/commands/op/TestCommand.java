package org.brlnsreb.commands.op;

import org.brlnsreb.BrlnsReb;
import org.brlnsreb.core.minigame.MinigameType;
import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.utils.SoundUtil;
import org.brlnsreb.utils.messages.ChatMsgs;
import org.brlnsreb.utils.messages.ChatMsgs.Alignment;
import org.cloudburstmc.math.vector.Vector3f;
import org.cloudburstmc.protocol.bedrock.data.Dimension;
import org.cloudburstmc.protocol.bedrock.data.PlayerActionType;
import org.cloudburstmc.protocol.bedrock.data.payload.common.DimensionType;
import org.cloudburstmc.protocol.bedrock.packet.ChangeDimensionPacket;
import org.cloudburstmc.protocol.bedrock.packet.PlayerActionPacket;
import org.powernukkitx.command.Command;
import org.powernukkitx.command.CommandContext;
import org.powernukkitx.command.CommandResult;
import org.powernukkitx.command.CommandSender;
import org.powernukkitx.command.SenderType;
import org.powernukkitx.command.route.RouteTree;
import org.powernukkitx.command.route.node.RouteNode;
import org.powernukkitx.command.tree.node.MessageStringNode;
import org.powernukkitx.level.Sound;
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

                final ChangeDimensionPacket pk = new ChangeDimensionPacket();
                pk.setDimension(DimensionType.from(Dimension.NETHER));
                pk.setPosition(Vector3f.from((float) p.getX(), (float) p.getY(), (float) p.getZ()));
                p.sendPacket(pk);

                final PlayerActionPacket playerActionPacket = new PlayerActionPacket();
                playerActionPacket.setPlayerRuntimeID(p.getId());
                playerActionPacket.setAction(PlayerActionType.CHANGE_DIMENSION_ACK);
                playerActionPacket.setBlockPosition(p.toNetwork().toInt());
                playerActionPacket.setResultPos(p.toNetwork().toInt());
                BrlnsReb.getScheduler().scheduleDelayedTask(() -> p.waitForAck(() -> p.sendPacket(playerActionPacket) ), 40);

                return CommandResult.success();
            }))
            .then(RouteNode.literal("sound1").exec(ctx -> {
                SoundUtil.sendSoundTo(getPlayer(ctx), Sound.RANDOM_CLICK.getSound());
                SoundUtil.sendSoundTo(getPlayer(ctx), SoundUtil.RANDOM_FIZZ_3DFALSE);
                return CommandResult.success();
            }))
            .then(RouteNode.literal("sound2").exec(ctx -> {
                SoundUtil.sendSoundTo(getPlayer(ctx), Sound.RANDOM_FIZZ.getSound());
                return CommandResult.success();
            }));

    }

    private CustomPlayer getPlayer(CommandContext ctx) {
        return (CustomPlayer) ctx.getSender();
    }

}
