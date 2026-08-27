package org.brlnsreb.commands;

import java.util.ArrayList;

import org.brlnsreb.core.minigame.match.Match;
import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.core.player.PlayerStateType;
import org.brlnsreb.core.player.PlayerUtils;
import org.brlnsreb.core.player.data.PlayerData;
import org.brlnsreb.core.player.data.database.FriendsManager;
import org.brlnsreb.core.player.data.database.Outcome;
import org.brlnsreb.mainhub.MainHub;
import org.brlnsreb.utils.ChatMsgs;
import org.powernukkitx.command.Command;
import org.powernukkitx.command.CommandContext;
import org.powernukkitx.command.CommandResult;
import org.powernukkitx.command.SenderType;
import org.powernukkitx.command.route.RouteTree;
import org.powernukkitx.command.route.node.RouteNode;
import org.powernukkitx.command.tree.node.IntNode;
import org.powernukkitx.command.tree.node.StringNode;
import org.powernukkitx.plugin.annotation.CommandDefinition;

@CommandDefinition(
    name = "friend",
    description = "Manage your friends",
    usage = """
            §l§eINFO§r §aUsage: §e/friend <subcommand>
            §l§eINFO§r §aSubcommands:
            §3》§r§2▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬§3《
            §2- §dadd §7- §aAdd a friend!
            §2- §dremove §7- §aRemove a friend from your friends list
            §2- §daccept §7- §aAccept a friend invite
            §2- §dacceptall §7- §aAccept all friend requests you've received
            §2- §ddeny §7- §aDeny a friend invite
            §2- §ddenyall §7- §aDeny all friend requests you've received
            §2- §dspectate §7- §aSpectate a friend's game
            §2- §djoin §7- §aJoin a friend's game
            §2- §dlist §7- §aView your friend list
            §2- §dalerts §7- §aToggle friend join/left alerts
            §2- §dnotify §7- §aToggle online/joinable status
            §2- §doff §7- §aTurn off friend invites for your current session
            §3》§r§2▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬§3《
            """ //TEXT
)

public class FriendCommand extends Command {

    @Override
    public void buildCommandTree(RouteTree tree) {
        CommandResult loginFail = CommandResult.fail(ChatMsgs.ERROR_PFX + "You are not logged in!");  //TEXT

        //friend add <name>
        RouteNode addNode = RouteNode.literal("add")
            .then(RouteNode.argument("name", new StringNode())
                .exec(ctx -> {
                    CustomPlayer sender = getSender(ctx);
                    String senderName = getPlayerName(sender);
                    if (senderName == null) return loginFail;
                    String receiverName = ctx.getArg("name");

                    FriendsManager.sendRequest(senderName, receiverName).thenAccept(outcome -> {
                        sender.sendMessage(
                            switch (outcome) {
                                case OK -> ChatMsgs.SUCCESS_PFX + "Friend request sent to §e" + receiverName;
                                //ps: here ADDED_FRIEND isn't OK, it's an extraordinary outcome; while in /friend accept it's normal so it's OK
                                case ADDED_FRIEND -> ChatMsgs.SUCCESS_PFX + "§e" + receiverName + "§a added to your friend list";
                                case NAME_NOT_FOUND -> ChatMsgs.ERROR_PFX + "Does not exist such a player named " + receiverName;
                                case CANNOT_FRIEND_SELF -> ChatMsgs.ERROR_PFX + "You cannot send a request to yourself!";
                                case ALREADY_FRIENDS -> ChatMsgs.ERROR_PFX + receiverName + " is already your friend!";
                                case REQUEST_ALREADY_SENT -> ChatMsgs.ERROR_PFX + "You have already sent a request to " + receiverName;
                                case REQUESTS_DISABLED -> ChatMsgs.ERROR_PFX + "Sorry, requests are not enabled for " + receiverName;
                                default -> ChatMsgs.ERROR_PFX + "Report this error to developers: friend add command";
                            }
                        );

                        CustomPlayer receiver = PlayerUtils.getPlayer(receiverName);
                        if (receiver != null) {
                            if (outcome == Outcome.OK) {
                                receiver.sendMessage(ChatMsgs.INFO_PFX + "§3" + senderName + "§a wants to be your friend! \n§e/friend accept/deny " + senderName);
                            } else if (outcome == Outcome.ADDED_FRIEND) {
                                receiver.sendMessage(ChatMsgs.INFO_PFX + "§e" + senderName + "§a added to your friend list");
                            }
                        }
                    });

                    return CommandResult.success();
                }));

        //friend remove <name>
        RouteNode removeNode = RouteNode.literal("remove")
            .then(RouteNode.argument("name", new StringNode())
                .exec(ctx -> {
                    CustomPlayer sender = getSender(ctx);
                    String senderName = getPlayerName(sender);
                    if (senderName == null) return loginFail;

                    FriendsManager.removeFriend(senderName, ctx.getArg("name")).thenAccept(outcome -> {
                        sender.sendMessage(
                            switch (outcome) {
                                case OK -> ChatMsgs.SUCCESS_PFX + "§e" + ctx.getArg("name") + "§a removed from your friend list";
                                case NOT_FRIENDS -> ChatMsgs.ERROR_PFX + ctx.getArg("name") + " not found in your friend list!";
                                case DB_ERROR -> ChatMsgs.ERROR_PFX + "Report this error to developers: DB_ERROR";
                                default -> ChatMsgs.ERROR_PFX + "Report this error to developers: friend remove command";
                            }
                        );
                    });

                    return CommandResult.success();
                }));
        
        //friend accept <name>
        RouteNode acceptNode = RouteNode.literal("accept")
            .then(RouteNode.argument("name", new StringNode())
                .exec(ctx -> {
                    CustomPlayer sender = getSender(ctx);
                    String requestReceiverName = getPlayerName(sender);
                    if (requestReceiverName == null) return loginFail;
                    String requestSenderName = ctx.getArg("name");

                    FriendsManager.acceptRequest(requestReceiverName, requestSenderName).thenAccept(outcome -> {
                        sender.sendMessage(
                            switch (outcome) {
                                case OK -> ChatMsgs.SUCCESS_PFX + "§e" + requestSenderName + "§a added to your friend list";
                                case REQUEST_NOT_FOUND -> ChatMsgs.ERROR_PFX + "Request not found from " + requestSenderName;
                                case DB_ERROR -> ChatMsgs.ERROR_PFX + "Report this error to developers: DB_ERROR";
                                default -> ChatMsgs.ERROR_PFX + "Report this error to developers: friend remove command";
                            }
                        );

                        if (outcome != Outcome.OK) return;
                        CustomPlayer requestSender = PlayerUtils.getPlayer(requestSenderName);
                        if (requestSender == null) return;

                        requestSender.sendMessage(ChatMsgs.INFO_PFX + "§e" + requestReceiverName + "§a added to your friend list");
                    });
                    
                    return CommandResult.success();
                }));
        
        //friend acceptall
        RouteNode acceptAllNode = RouteNode.literal("acceptall")
            .exec(ctx -> {
                CustomPlayer sender = getSender(ctx);
                String requestReceiverName = getPlayerName(sender);
                if (requestReceiverName == null) return loginFail;

                ArrayList<String> requestSenderNames;
                synchronized (sender.data.getFriendLock()) {
                    requestSenderNames = new ArrayList<>(sender.data.getReceivedFriendRequests().values());
                }

                for (String requestSenderName : requestSenderNames) {
                    FriendsManager.acceptRequest(requestReceiverName, requestSenderName).thenAccept(outcome -> {
                        sender.sendMessage(
                            switch (outcome) {
                                case OK -> ChatMsgs.SUCCESS_PFX + "§e" + requestSenderName + "§a added to your friend list";
                                case DB_ERROR -> ChatMsgs.ERROR_PFX + "Report this error to developers: DB_ERROR";
                                default -> ChatMsgs.ERROR_PFX + "Report this error to developers: friend remove command";
                            }
                        );

                        if (outcome != Outcome.OK) return;
                        CustomPlayer requestSender = PlayerUtils.getPlayer(requestSenderName);
                        if (requestSender == null) return;
                        
                        requestSender.sendMessage(ChatMsgs.INFO_PFX + "§e" + requestReceiverName + "§a added to your friend list");
                    });
                }

                return CommandResult.success();
            });

        //friend deny <name>
        RouteNode denyNode = RouteNode.literal("deny")
            .then(RouteNode.argument("name", new StringNode())
                .exec(ctx -> {
                    CustomPlayer sender = getSender(ctx);
                    String requestReceiverName = getPlayerName(sender);
                    if (requestReceiverName == null) return loginFail;

                    FriendsManager.denyRequest(requestReceiverName, ctx.getArg("name")).thenAccept(outcome -> {
                        sender.sendMessage(
                            switch (outcome) {
                                case OK -> ChatMsgs.SUCCESS_PFX + "Denied friend request from §e" + ctx.getArg("name");
                                case REQUEST_NOT_FOUND -> ChatMsgs.ERROR_PFX + "Request not found from " + ctx.getArg("name");
                                case DB_ERROR -> ChatMsgs.ERROR_PFX + "Report this error to developers: DB_ERROR";
                                default -> ChatMsgs.ERROR_PFX + "Report this error to developers: friend remove command";
                            }
                        );
                    });
                    return CommandResult.success();
                }));

        //friend denyall
        RouteNode denyAllNode = RouteNode.literal("denyall")
            .exec(ctx -> {
                CustomPlayer sender = getSender(ctx);
                String requestReceiverName = getPlayerName(sender);
                if (requestReceiverName == null) return loginFail;

                ArrayList<String> requestSenderNames;
                synchronized (sender.data.getFriendLock()) {
                    requestSenderNames = new ArrayList<>(sender.data.getReceivedFriendRequests().values());
                }

                for (String requestSenderName : requestSenderNames) {
                    FriendsManager.denyRequest(requestReceiverName, requestSenderName).thenAccept(outcome -> {
                        sender.sendMessage(
                            switch (outcome) {
                                case OK -> ChatMsgs.SUCCESS_PFX + "Denied friend request from §e" + requestSenderName;
                                case DB_ERROR -> ChatMsgs.ERROR_PFX + "Report this error to developers: DB_ERROR";
                                default -> ChatMsgs.ERROR_PFX + "Report this error to developers: friend remove command";
                            }
                        );
                    });
                }

                return CommandResult.success();
            });

        //(friend spectate/join) <name>   (same mechanics in both spectate and join, from what i remember)
        RouteNode spectateJoinNameNode = RouteNode.argument("name", new StringNode())
            .exec(ctx -> {
                CustomPlayer sender = getSender(ctx);
                if (getPlayerName(sender) == null) return loginFail;

                String friendName = ctx.getArg("name");
                if (!sender.data.isFriendWith(friendName)) {
                    return CommandResult.fail(
                        ChatMsgs.ERROR_PFX + ctx.getArg("name") + " not found in your friend list."
                    );
                }

                CustomPlayer friend = PlayerUtils.getPlayer(friendName);
                if (friend == null) {
                    return CommandResult.fail(
                        ChatMsgs.ERROR_PFX + friendName + " is not online."
                    );
                }

                if (friend.state == PlayerStateType.TELEPORTING) {
                    return CommandResult.fail(ChatMsgs.ERROR_PFX + "You cannot join " + friendName + " right now, retry in a few seconds.");
                }

                Match match = sender.matchCurrent;
                if (match != null) match.onLeave(sender);
                switch (friend.state) {
                    case LOBBY:
                        if (friend.minigameCurrent == null) {
                            MainHub.instance.onJoin(sender);
                        } else {
                            friend.minigameCurrent.onLobbyJoin(sender);
                        }
                        break;
                    case WAITING_LOBBY, DEATH_LOBBY:
                        friend.matchCurrent.onJoin(sender);
                        break;
                    case PLAYING, SPECTATOR:
                        friend.matchCurrent.onJoin(sender);
                        friend.sendMessage(ChatMsgs.INFO_PFX + "§d" + getPlayerName(sender) + "§a is now spectating.");
                        break;
                    default:
                        MainHub.instance.onJoin(sender);
                        return CommandResult.fail(ChatMsgs.ERROR_PFX + "Report this error to developers: spectate_join_switch.error");
                }

                sender.sendMessage(ChatMsgs.SUCCESS_PFX + "You joined " + friendName + "!");
                return CommandResult.success();
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
                if (getPlayerName(sender) == null) return loginFail;

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
                if (getPlayerName(sender) == null) return loginFail;

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
                if (getPlayerName(sender) == null) return loginFail;

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
            ;//.orElse(ctx -> ctx.getSender().sendMessage(usageMessage)); TODO: enable
    }

    private boolean listExec(CommandContext ctx, int currentPage) {
        CustomPlayer sender = getSender(ctx);
        if (getPlayerName(sender) == null) return false;

        ArrayList<String> onlineFriends;
        ArrayList<String> offlineFriends;
        synchronized (sender.data.getFriendLock()) {
            onlineFriends = new ArrayList<>(sender.data.getOnlineFriends().values());
            offlineFriends = new ArrayList<>(sender.data.getOfflineFriends().values());
        }

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
            "§e--- §aFriend List §7¦ §aPage §e%d§a/§e%d ---".formatted(currentPage, pages)
        );

        ArrayList<String> curr;
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
            CustomPlayer friend = PlayerUtils.getPlayer(curr.get(currElementIndex));
            if (!online || friend == null) {
                message += "§cOffline";
            } else {
                message += "§aOnline §7(§d" 
                    + (friend.minigameCurrent == null 
                        ? MainHub.displayNameTag
                        : friend.minigameCurrent.mgt.displayNameTag)
                    + "§7)";
            }
            
            sender.sendMessage(message);
        }

        if (currentPage < pages) sender.sendMessage("§aNext page: §e/friend list " + (currentPage + 1));
        
        return true;
    }

    private CustomPlayer getSender(CommandContext ctx) {
        return (CustomPlayer) ctx.getSender();
    }

    private String getPlayerName(CustomPlayer player) {
        return player.data.name;
    }

}