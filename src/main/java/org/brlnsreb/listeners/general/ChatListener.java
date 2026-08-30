package org.brlnsreb.listeners.general;

import org.brlnsreb.BrlnsReb;
import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.utils.messages.ChatMsgs;
import org.powernukkitx.event.EventHandler;
import org.powernukkitx.event.Listener;
import org.powernukkitx.event.player.PlayerChatEvent;
import org.powernukkitx.event.player.PlayerCommandPreprocessEvent;
import org.powernukkitx.Server;
import org.powernukkitx.command.CommandContext;
import org.powernukkitx.level.Level;
import org.powernukkitx.plugin.annotation.EventListener;
import org.powernukkitx.utils.TextFormat;

@EventListener
public class ChatListener implements Listener {

    public static final String curlyBrktOpenCode = "§§[§§curly";
    public static final String curlyBrktCloseCode = "§§]§§curly";

    private String[] CHAT_COMMANDS_0 = {        //0 args before text
        "/grm",
        "/frm",
        "/reply"
    };

    private String[] CHAT_COMMANDS_1 = {        //1 arg before text (e.g. the player name)
        "/pvt"
    };

    public static String getMessage(CommandContext ctx) {
        String message = ctx.getArg("message");
        message = message.substring(1, message.length() - 1)
            .replace(curlyBrktOpenCode, "{")
            .replace(curlyBrktCloseCode, "}");
        return TextFormat.colorize(message);
    }

    @EventHandler
    public void onChat(PlayerChatEvent event) {
        CustomPlayer player = (CustomPlayer) event.getPlayer();

        if (player.isPlaying() || player.isGameSpectator()) {
            if (!player.matchCurrent.getGame().onChat(player, event)) {
                event.setCancelled();
                return;
            }
        }

        String formatted = switch (player.state) {
            case PLAYING -> player.ingameChatNameTag + "§7: ";
            case SPECTATOR -> ChatMsgs.SPEC_PFX + player.data.name + ": ";
            default -> (player.data.getFloorLevel() < 1000 ? " " : "") + player.getNameTag() + "§7: ";
        } + event.getMessage();

        filterPlayerBySenderLevel(event);
        filterPlayerByChatSettings(event);

        event.setCancelled();
        Server.getInstance().broadcastMessage(formatted, event.getRecipients());
    }

    private void filterPlayerBySenderLevel(PlayerChatEvent event) {
        //avoid players chatting in different worlds
        if (BrlnsReb.getGlobalChat()) return;

        Level senderLevel = event.getPlayer().getLevel();
        event.getRecipients().removeIf(recipient -> 
            (recipient instanceof CustomPlayer player) &&
            !player.getLevel().equals(senderLevel)
        );
    }

    private void filterPlayerByChatSettings(PlayerChatEvent event) {
        event.getRecipients().removeIf(recipient ->
            (recipient instanceof CustomPlayer player) &&
            !player.data.isChatVisible()
        );
    }

    @EventHandler
    public void onCommandPreprocess(PlayerCommandPreprocessEvent event) {
        CustomPlayer player = (CustomPlayer) event.getPlayer();

        switch (player.state) {
            case PLAYING, SPECTATOR:
                if (!player.matchCurrent.getGame().onCommandPreprocess(player, event)) {
                    event.setCancelled();
                    return;
                }
            
            default: break;
        }

        boolean preprocessed = false;
        String command = event.getMessage().stripLeading();

        for (String root : CHAT_COMMANDS_0) {
            if (!command.startsWith(root) || command.equals(root)) continue;
            
            preprocessed = true;
            command = command.replace("{", curlyBrktOpenCode).replace("}", curlyBrktCloseCode);
            command = command.replaceFirst(root + " ", root + " {") + "}";
            event.setMessage(command);
        }
        if (preprocessed) return;
        
        for (String root : CHAT_COMMANDS_1) {
            if (!command.startsWith(root)) continue;

            preprocessed = true;
            command = command.replace("{", curlyBrktOpenCode).replace("}", curlyBrktCloseCode);
            command = command.replaceAll("(" + root + "\\s+\\w+\\s)", "$1{") + "}";
            event.setMessage(command);
        }
        if (preprocessed) return;
    }

}
