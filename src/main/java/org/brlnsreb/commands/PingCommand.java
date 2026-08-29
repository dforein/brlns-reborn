package org.brlnsreb.commands;

import org.brlnsreb.tasks.CheckPingTask;
import org.brlnsreb.utils.messages.Messages;
import org.brlnsreb.utils.messages.ChatMsgs.Alignment;
import org.powernukkitx.Player;
import org.powernukkitx.command.Command;
import org.powernukkitx.command.CommandResult;
import org.powernukkitx.command.SenderType;
import org.powernukkitx.command.route.RouteTree;
import org.powernukkitx.plugin.annotation.CommandDefinition;
import org.powernukkitx.utils.TextFormat;

@CommandDefinition(
    name = "ping",
    description = "Check your ping"
)

public class PingCommand extends Command {

    private enum Grade {
        POOR(TextFormat.DARK_RED),
        BAD(TextFormat.RED),
        AVERAGE(TextFormat.GOLD),
        GOOD(TextFormat.YELLOW),
        EXCELLENT(TextFormat.GREEN),
        UNKNOWN(TextFormat.GRAY);

        public TextFormat color;
        public String str;

        private Grade(TextFormat color) {
            this.color = color;
            this.str = color + this.toString();
        }
    }
    
    @Override
    public void buildCommandTree(RouteTree tree) {
        tree.getRoot().senderType(SenderType.PLAYER)
            .exec(ctx -> {
                Player player = (Player) ctx.getSender();

                int ping = (int) player.getPing(); 
                int median = CheckPingTask.getMedian(player);
                Grade health = getPingGrade(median >= 0 ? median : ping);
                Grade stability = getStability(CheckPingTask.getMedianAbsDeviation(player));
                
                Messages.sendMessageBlock(player, Alignment.LEFT, false,
                    "§e--- §l§dConnection status§r §e---",
                    "§ePing/Latency: " + getPingGrade(ping).color + ping + "ms",
                    "§eAverage Ping/Latency: " + health.color + median + "ms",
                    "§eConnection Health: " + health.str,
                    "§eConnection Stability: " + stability.str,
                    "§eServer Location: §dEUROPE"
                );

                return CommandResult.success();
            });
    }
    
    private Grade getPingGrade(int ping) {
        if (ping < 50)          return Grade.EXCELLENT;
        else if (ping < 150)    return Grade.GOOD;
        else if (ping < 300)    return Grade.AVERAGE;
        else if (ping < 500)    return Grade.BAD;
        else                    return Grade.POOR;
    }

    private Grade getStability(int mad) {
        if (mad < 0)        return Grade.UNKNOWN;
        else if (mad < 10)  return Grade.EXCELLENT;
        else if (mad < 25)  return Grade.GOOD;
        else if (mad < 50)  return Grade.AVERAGE;
        else if (mad < 100) return Grade.BAD;
        else                return Grade.POOR;
    }

}
