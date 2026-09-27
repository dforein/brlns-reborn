package org.brlnsreb.commands;

import java.util.List;

import org.brlnsreb.core.minigame.match.Match;
import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.core.player.PlayerStateType;
import org.brlnsreb.core.player.PlayerUtils;
import org.brlnsreb.core.player.data.PlayerData;
import org.brlnsreb.core.player.data.database.FriendsManager;
import org.brlnsreb.core.player.data.database.Outcome;
import org.brlnsreb.mainhub.MainHub;
import org.brlnsreb.utils.messages.ChatMsgs;
import org.brlnsreb.utils.messages.Messages;
import org.brlnsreb.utils.messages.ChatMsgs.Alignment;
import org.powernukkitx.command.Command;
import org.powernukkitx.command.CommandContext;
import org.powernukkitx.command.CommandResult;
import org.powernukkitx.command.SenderType;
import org.powernukkitx.command.route.RouteTree;
import org.powernukkitx.command.route.node.RouteNode;
import org.powernukkitx.command.tree.node.IntNode;
import org.powernukkitx.command.tree.node.StringNode;
import org.powernukkitx.plugin.annotation.CommandDefinition;
import org.powernukkitx.utils.TextFormat;

@CommandDefinition(
    name = "friend",
    description = "Manage your friends"
)

public class FriendCommand extends Command implements BrlnsCommand {

    @Override
    public void buildCommandTree(RouteTree tree) {
        CommandResult loginFail = CommandResult.fail(ChatMsgs.ERROR_PFX + "You are not logged in!");  //TEXT

        //friend add <name>
        RouteNode addNode = RouteNode.literal("add")
            .then(RouteNode.argument("name", new StringNode())
                .exec(ctx -> {
                    CustomPlayer sender = getSender(ctx);
                    String senderName = sender.data.name;
                    if (senderName == null) return loginFail;
                    String receiverName = ctx.getArg("name");

                    FriendsManager.sendRequest(senderName, receiverName).thenAccept(outcome -> {
                        FriendsManager.sendRequestMessages(outcome, sender, receiverName);
                    });

                    return CommandResult.success();
                }));

        //friend remove <name>
        RouteNode removeNode = RouteNode.literal("remove")
            .then(RouteNode.argument("name", new StringNode())
                .exec(ctx -> {
                    CustomPlayer sender = getSender(ctx);
                    String senderName = sender.data.name;
                    String friendName = ctx.getArg("name");
                    if (senderName == null) return loginFail;

                    FriendsManager.removeFriend(senderName, friendName).thenAccept(outcome -> {
                        FriendsManager.sendRemoveFriendMessages(outcome, sender, friendName);
                    });

                    return CommandResult.success();
                }));
        
        //friend accept <name>
        RouteNode acceptNode = RouteNode.literal("accept")
            .then(RouteNode.argument("name", new StringNode())
                .exec(ctx -> {
                    CustomPlayer sender = getSender(ctx);
                    String requestReceiverName = sender.data.name;
                    if (requestReceiverName == null) return loginFail;
                    String requestSenderName = ctx.getArg("name");

                    FriendsManager.acceptRequest(requestReceiverName, requestSenderName).thenAccept(outcome -> {
                        FriendsManager.sendAcceptRequestMessages(outcome, sender, requestSenderName);
                    });
                    
                    return CommandResult.success();
                }));
        
        //friend acceptall
        RouteNode acceptAllNode = RouteNode.literal("acceptall")
            .exec(ctx -> {
                CustomPlayer sender = getSender(ctx);
                String requestReceiverName = sender.data.name;
                if (requestReceiverName == null) return loginFail;

                List<String> requestSenderNames = sender.data.getReceivedFriendRequestsCopy();

                for (String requestSenderName : requestSenderNames) {
                    FriendsManager.acceptRequest(requestReceiverName, requestSenderName).thenAccept(outcome -> {
                        FriendsManager.sendAcceptRequestMessages(outcome, sender, requestSenderName);
                    });
                }

                return CommandResult.success();
            });

        //friend deny <name>
        RouteNode denyNode = RouteNode.literal("deny")
            .then(RouteNode.argument("name", new StringNode())
                .exec(ctx -> {
                    CustomPlayer sender = getSender(ctx);
                    String requestReceiverName = sender.data.name;
                    if (requestReceiverName == null) return loginFail;

                    FriendsManager.denyRequest(requestReceiverName, ctx.getArg("name")).thenAccept(outcome -> {
                        FriendsManager.sendDenyRequestMessages(outcome, sender, ctx.getArg("name"));
                    });
                    return CommandResult.success();
                }));

        //friend denyall
        RouteNode denyAllNode = RouteNode.literal("denyall")
            .exec(ctx -> {
                CustomPlayer sender = getSender(ctx);
                String requestReceiverName = sender.data.name;
                if (requestReceiverName == null) return loginFail;

                List<String> requestSenderNames = sender.data.getReceivedFriendRequestsCopy();

                for (String requestSenderName : requestSenderNames) {
                    FriendsManager.denyRequest(requestReceiverName, requestSenderName).thenAccept(outcome -> {
                        FriendsManager.sendDenyRequestMessages(outcome, sender, requestSenderName);
                    });
                }

                return CommandResult.success();
            });

        //(friend spectate/join) <name>   (same mechanics in both spectate and join, from what i remember)
        RouteNode spectateJoinNameNode = RouteNode.argument("name", new StringNode())
            .exec(ctx -> {
                CustomPlayer sender = getSender(ctx);
                if (!sender.data.isLogged()) return loginFail;

                String friendName = ctx.getArg("name");
                if (!sender.data.isFriendWith(friendName)) {
                    return CommandResult.fail(
                        ChatMsgs.ERROR_PFX + ctx.getArg("name") + " not found in your friend list."
                    );
                }

                if (joinSpectateFriend(sender, friendName)) {
                    return CommandResult.success();
                } else {
                    return CommandResult.fail();
                }
            });

        //friend spectate <name>
        RouteNode spectateNode = RouteNode.literal("spectate").then(spectateJoinNameNode);
        
        //friend join <name>
        RouteNode joinNode = RouteNode.literal("join").then(spectateJoinNameNode);

        //friend list (<pageNumber>)
        RouteNode listNode = RouteNode.literal("list")
            .exec(ctx -> {
                if (listExec(ctx, 1)) {
                    return CommandResult.success();
                } else {
                    return loginFail;
                }
            })
            
            .then(RouteNode.argument("pageNumber", new IntNode()).optional(true)
                .exec(ctx -> {
                    if (listExec(ctx, ctx.getArg("pageNumber"))) {
                        return CommandResult.success();
                    } else {
                        return loginFail;
                    }
                }));

        //friend alerts
        RouteNode alertsNode = RouteNode.literal("alerts")
            .exec(ctx -> {
                CustomPlayer sender = getSender(ctx);
                if (!sender.data.isLogged()) return loginFail;

                PlayerData data = sender.data;
                data.setFriendAlerts(!data.getFriendAlerts());
                FriendsManager.saveFriendsSettings(sender);

                sender.sendMessage(data.getFriendAlerts()
                    ? ChatMsgs.SUCCESS_PFX + "Friend join/left alerts enabled."
                    : ChatMsgs.SUCCESS_PFX + "Friend join/left alerts disabled.");

                return CommandResult.success();
            });

        //friend notify
        RouteNode notifyNode = RouteNode.literal("notify")
            .exec(ctx -> {
                CustomPlayer sender = getSender(ctx);
                if (!sender.data.isLogged()) return loginFail;

                PlayerData data = sender.data;
                data.setFriendNotify(!data.getFriendNotify());
                FriendsManager.saveFriendsSettings(sender);

                sender.sendMessage(data.getFriendNotify()
                    ? ChatMsgs.SUCCESS_PFX + "Online/joinable status on: you will send alerts to your friends."
                    : ChatMsgs.SUCCESS_PFX + "Online/joinable status off: you will not send alerts to your friends.");

                return CommandResult.success();
            });

        //friend list
        RouteNode offNode = RouteNode.literal("off")
            .exec(ctx -> {
                CustomPlayer sender = getSender(ctx);
                if (!sender.data.isLogged()) return loginFail;

                sender.data.setFriendRequestsFlag(false);
                sender.sendMessage(ChatMsgs.SUCCESS_PFX + "Friend invites disabled for the current session.");
                return CommandResult.success();
            });

        tree.getRoot().senderType(SenderType.PLAYER)
            .then(addNode)
            .then(removeNode)
            .then(acceptNode)
            .then(acceptAllNode)
            .then(denyNode)
            .then(denyAllNode)
            .then(spectateNode)
            .then(joinNode)
            .then(listNode)
            .then(alertsNode)
            .then(notifyNode)
            .then(offNode)
            .orElse(ctx -> {
                if (!(ctx.getSender() instanceof CustomPlayer sender)) {
                    ctx.getSender().sendMessage(TextFormat.RED + "Only players can use this command!");
                    return;
                }
                sender.sendMessage(ChatMsgs.INFO_PFX + "Usage: §e/friend <subcommand>");
                sender.sendMessage(ChatMsgs.INFO_PFX + "Subcommands:");
                Messages.sendMessageBlock(sender, Alignment.LEFT, false,
                    "§dadd §7- §aAdd a friend!",
                    "§dremove §7- §aRemove a friend from your friends list",
                    "§daccept §7- §aAccept a friend invite",
                    "§dacceptall §7- §aAccept all friend requests you've received",
                    "§ddeny §7- §aDeny a friend invite",
                    "§ddenyall §7- §aDeny all friend requests you've received",
                    "§dspectate §7- §aSpectate a friend's game",
                    "§djoin §7- §aJoin a friend's game",
                    "§dlist §7- §aView your friend list",
                    "§dalerts §7- §aToggle friend join/left alerts",
                    "§dnotify §7- §aToggle online/joinable status",
                    "§doff §7- §aTurn off friend invites for your current session"
                );
            });
    }

    private boolean listExec(CommandContext ctx, int currentPage) {
        CustomPlayer sender = getSender(ctx);
        if (!sender.data.isLogged()) return false;

        List<String> onlineFriends = sender.data.getOnlineFriendsCopy();
        List<String> offlineFriends = sender.data.getOfflineFriendsCopy();
        onlineFriends.sort(String.CASE_INSENSITIVE_ORDER);
        offlineFriends.sort(String.CASE_INSENSITIVE_ORDER);

        int pages = (onlineFriends.size() + offlineFriends.size() + 9) / 10;
        if (pages == 0) {
            sender.sendMessage(ChatMsgs.INFO_PFX + "Your friend list is empty.");
            return true;
        }

        if (currentPage < 1 || currentPage > pages) {
            sender.sendMessage(ChatMsgs.ERROR_PFX + "This friend list page doesn't exist!");
            return true;
        }

        sender.sendMessage(
            "§e--- §aFriend List §7| §aPage §e%d§a/§e%d ---".formatted(currentPage, pages)
        );

        List<String> curr;
        boolean online;
        int currElementIndex;
        for (int i = 0; i < 10; i++) {
            currElementIndex = (currentPage - 1) * 10 + i;
            
            if (currElementIndex < onlineFriends.size()) {
                curr = onlineFriends;
                online = true;
            } else {
                currElementIndex -= onlineFriends.size();
                if (currElementIndex < offlineFriends.size()) {
                    curr = offlineFriends;
                    online = false;
                } else {
                    break;
                }
            }

            String message = "§3" + curr.get(currElementIndex) + " §7- ";
            CustomPlayer friend = PlayerUtils.getLoggedPlayer(curr.get(currElementIndex));
            if (!online || friend == null) {
                message += "§cOffline";
            } else {
                message += "§aOnline §7(§d" 
                    + (friend.minigameCurrent == null 
                        ? MainHub.displayNameTagP
                        : friend.minigameCurrent.mgt.displayNameTagP)
                    + "§7)";
            }
            
            sender.sendMessage(message);
        }

        if (currentPage < pages) sender.sendMessage("§aNext page: §e/friend list " + (currentPage + 1));
        
        return true;
    }

    public static boolean joinSpectateFriend(CustomPlayer player, String friendName) {
        CustomPlayer friend = PlayerUtils.getLoggedPlayer(friendName);
        if (friend == null) {
            player.sendMessage(ChatMsgs.ERROR_PFX + friendName + " is not online now.");
            return false;
        }

        if (friend.state == PlayerStateType.TELEPORTING) {
            player.sendMessage(ChatMsgs.ERROR_PFX + "You cannot join " + friendName + " right now, retry in a few seconds.");
            return false;
        }

        Match match = player.matchCurrent;
        if (match != null) match.onLeave(player);
        switch (friend.state) {
            case LOBBY -> {
                if (friend.minigameCurrent == null) {
                    MainHub.instance.onJoin(player);
                } else {
                    friend.minigameCurrent.onLobbyJoin(player);
                }
            }
            
            case WAITING_LOBBY, DEATH_LOBBY -> friend.matchCurrent.onJoin(player);

            case PLAYING, SPECTATOR -> {
                friend.matchCurrent.onJoin(player);
                friend.sendMessage(ChatMsgs.INFO_PFX + "§d" + player.data.name + "§a is now spectating.");
            }

            default -> {
                MainHub.instance.onJoin(player);
                player.sendMessage(ChatMsgs.ERROR_PFX + "Report this error to developers: friend_join_switch_error");
                return false;
            }
        }

        player.sendMessage(ChatMsgs.SUCCESS_PFX + "You joined " + friendName + "!");
        return true;
    }

    private CustomPlayer getSender(CommandContext ctx) {
        return (CustomPlayer) ctx.getSender();
    }

}