package org.brlnsreb.commands;

import java.util.ArrayList;

import org.brlnsreb.core.minigame.match.MinigameMatch;
import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.core.player.PlayerStateType;
import org.brlnsreb.core.player.PlayerUtils;
import org.brlnsreb.core.player.data.PlayerData;
import org.brlnsreb.core.player.data.database.FriendsManager;
import org.brlnsreb.generallobby.GeneralLobby;
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
            §3§o》§r§2▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬§3§o《
            §2— §dadd §7— §aAdd a friend!
            §2— §dremove §7— §aRemove a friend from your friends list
            §2— §daccept §7— §aAccept a friend invite
            §2— §dacceptall §7— §aAccept all friend requests you've received
            §2— §ddeny §7— §aDeny a friend invite
            §2— §ddenyall §7— §aDeny all friend requests you've received
            §2— §dspectate §7— §aSpectate a friend's game
            §2— §djoin §7— §aJoin a friend's game
            §2— §dlist §7— §aView your friend list
            §2— §dalerts §7— §aToggle friend join/left alerts
            §2— §dnotify §7— §aToggle online/joinable status
            §2— §doff §7— §aTurn off friend invites for your current session
            3§o》§r§2▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬§3§o《
            """ //TEXT
)

public class FriendCommand extends Command {

    public FriendCommand() {
        this.enableCommandTree();
    }

    @Override
    public void buildCommandTree(RouteTree tree) {
        CommandResult loginFail = CommandResult.fail(ChatMsgs.errorPfx + "You aren't logged in!");  //TEXT

        //friend add <name>
        RouteNode addNode = RouteNode.literal("add")
            .then(RouteNode.argument("name", new StringNode())
                .exec(ctx -> {
                    CustomPlayer sender = getSender(ctx);
                    String senderName = getPlayerName(sender);
                    if (senderName == null) return loginFail;

                    FriendsManager.sendRequest(senderName, ctx.getArg("name")).thenAccept(outcome -> {
                        sender.sendMessage(
                            switch (outcome) {
                                case OK -> ChatMsgs.successPfx + "Friend request sent to §e" + ctx.getArg("name");
                                //ps: here ADDED_FRIEND isn't OK, it's an extraordinary outcome; while in /friend accept it's normal so it's OK
                                case ADDED_FRIEND -> ChatMsgs.successPfx + "§e" + ctx.getArg("name") + "§a added to your friend list";
                                case CANNOT_FRIEND_SELF -> ChatMsgs.errorPfx + "You cannot send a request to yourself!";
                                case ALREADY_FRIENDS -> ChatMsgs.errorPfx + ctx.getArg("name") + " is already your friend!";
                                case REQUEST_ALREADY_SENT -> ChatMsgs.errorPfx + "You have already sent a request to " + ctx.getArg("name");
                                case REQUESTS_DISABLED -> ChatMsgs.errorPfx + "Sorry, requests are not enabled for " + ctx.getArg("name");
                                default -> ChatMsgs.errorPfx + "Report this error to developers: friend add command";
                            }
                        );
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
                                case OK -> ChatMsgs.successPfx + "§e" + ctx.getArg("name") + "§a removed from your friend list";
                                case NOT_FRIENDS -> ChatMsgs.errorPfx + ctx.getArg("name") + " not found in your friend list!";
                                case DB_ERROR -> ChatMsgs.errorPfx + "Report this error to developers: DB_ERROR";
                                default -> ChatMsgs.errorPfx + "Report this error to developers: friend remove command";
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

                    FriendsManager.acceptRequest(requestReceiverName, ctx.getArg("name")).thenAccept(outcome -> {
                        sender.sendMessage(
                            switch (outcome) {
                                case OK -> ChatMsgs.successPfx + "§e" + ctx.getArg("name") + "§a added to your friend list";
                                case REQUEST_NOT_FOUND -> ChatMsgs.errorPfx + "Request not found from " + ctx.getArg("name");
                                case DB_ERROR -> ChatMsgs.errorPfx + "Report this error to developers: DB_ERROR";
                                default -> ChatMsgs.errorPfx + "Report this error to developers: friend remove command";
                            }
                        );
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
                synchronized (sender.getPlayerData().getFriendLock()) {
                    requestSenderNames = new ArrayList<>(sender.getPlayerData().getReceivedFriendRequests().values());
                }

                for (String requestSenderName : requestSenderNames) {
                    FriendsManager.acceptRequest(requestReceiverName, requestSenderName).thenAccept(outcome -> {
                        sender.sendMessage(
                            switch (outcome) {
                                case OK -> ChatMsgs.successPfx + "§e" + requestSenderName + "§a added to your friend list";
                                case DB_ERROR -> ChatMsgs.errorPfx + "Report this error to developers: DB_ERROR";
                                default -> ChatMsgs.errorPfx + "Report this error to developers: friend remove command";
                            }
                        );
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
                                case OK -> ChatMsgs.successPfx + "Denied friend request from §e" + ctx.getArg("name");
                                case REQUEST_NOT_FOUND -> ChatMsgs.errorPfx + "Request not found from " + ctx.getArg("name");
                                case DB_ERROR -> ChatMsgs.errorPfx + "Report this error to developers: DB_ERROR";
                                default -> ChatMsgs.errorPfx + "Report this error to developers: friend remove command";
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
                synchronized (sender.getPlayerData().getFriendLock()) {
                    requestSenderNames = new ArrayList<>(sender.getPlayerData().getReceivedFriendRequests().values());
                }

                for (String requestSenderName : requestSenderNames) {
                    FriendsManager.denyRequest(requestReceiverName, requestSenderName).thenAccept(outcome -> {
                        sender.sendMessage(
                            switch (outcome) {
                                case OK -> ChatMsgs.successPfx + "Denied friend request from §e" + requestSenderName;
                                case DB_ERROR -> ChatMsgs.errorPfx + "Report this error to developers: DB_ERROR";
                                default -> ChatMsgs.errorPfx + "Report this error to developers: friend remove command";
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

                if (!sender.getPlayerData().isFriendWith((String) ctx.getArg("name"))) {
                    return CommandResult.fail(
                        ChatMsgs.errorPfx + ctx.getArg("name") + " not found in your friend list"
                    );
                }

                CustomPlayer friend = PlayerUtils.getPlayer((String) ctx.getArg("name"));
                if (friend == null) {
                    return CommandResult.fail(
                        ChatMsgs.errorPfx + ctx.getArg("name") + " is not online"
                    );
                }

                if (friend.state == PlayerStateType.TELEPORTING) {
                    return CommandResult.fail(ChatMsgs.errorPfx + "You cannot join " + ctx.getArg("name") + " right now, retry in a few seconds.");
                }

                MinigameMatch match = sender.getMatch();
                if (match != null) match.onLeave(sender);
                switch (friend.state) {
                    case LOBBY:
                        if (friend.currentMinigame == null) {
                            GeneralLobby.instance.onJoin(sender);
                        } else {
                            friend.currentMinigame.onLobbyJoin(sender);
                        }
                        break;
                    case WAITING_LOBBY, PLAYING, SPECTATOR, END_LOBBY:
                        friend.getMatch().onJoin(sender);
                        break;
                    default:
                        GeneralLobby.instance.onJoin(sender);
                        return CommandResult.fail(ChatMsgs.errorPfx + "Report this error to developers: spectate_join_switch.error");
                }

                sender.sendMessage(ChatMsgs.successPfx + "You joined " + ctx.getArg("name") + "!");
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

            .then(RouteNode.argument("pageNumber", new IntNode())
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

                PlayerData data = sender.getPlayerData();
                data.setFriendAlerts(!data.getFriendAlerts());
                FriendsManager.saveFriendsSettings(sender);

                sender.sendMessage(data.getFriendAlerts()
                    ? ChatMsgs.successPfx + "Friend join/left alerts enabled."
                    : ChatMsgs.successPfx + "Friend join/left alerts disabled.");

                return CommandResult.success();
            });

        //friend notify
        RouteNode notifyNode = RouteNode.literal("notify")
            .exec(ctx -> {
                CustomPlayer sender = getSender(ctx);
                if (getPlayerName(sender) == null) return loginFail;

                PlayerData data = sender.getPlayerData();
                data.setFriendNotify(!data.getFriendNotify());
                FriendsManager.saveFriendsSettings(sender);

                sender.sendMessage(data.getFriendNotify()
                    ? ChatMsgs.successPfx + "Online/joinable status on: you will send alerts to your friends."
                    : ChatMsgs.successPfx + "Online/joinable status off: you will not send alerts to your friends.");

                return CommandResult.success();
            });

        //friend list
        RouteNode offNode = RouteNode.literal("off")
            .exec(ctx -> {
                CustomPlayer sender = getSender(ctx);
                if (getPlayerName(sender) == null) return loginFail;

                sender.getPlayerData().setFriendRequestsFlag(false);
                sender.sendMessage(ChatMsgs.successPfx + "Friend invites disabled for the current session.");
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
            .then(offNode);
    }

    private boolean listExec(CommandContext ctx, int currentPage) {
        CustomPlayer sender = getSender(ctx);
        if (getPlayerName(sender) == null) return false;

        ArrayList<String> onlineFriends;
        ArrayList<String> offlineFriends;
        synchronized (sender.getPlayerData().getFriendLock()) {
            onlineFriends = new ArrayList<>(sender.getPlayerData().getOnlineFriends().values());
            offlineFriends = new ArrayList<>(sender.getPlayerData().getOfflineFriends().values());
        }

        int pages = (onlineFriends.size() + offlineFriends.size() + 9) / 10;
        if (currentPage > pages) {
            sender.sendMessage(ChatMsgs.errorPfx + "This friend list page doesn't exist!");
            return true;
        }

        sender.sendMessage(
            "§e——— §aFriend List §7¦ §aPage §e%d§a/§e%d ———".formatted(currentPage, pages)
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

            String message = "§3" + curr.get(currElementIndex) + " §7— ";
            CustomPlayer friend = PlayerUtils.getPlayer(curr.get(currElementIndex));
            if (!online || friend == null) {
                message += "§cOffline";
            } else {
                message += "§aOnline §7(§d" 
                    + (friend.currentMinigame == null 
                        ? GeneralLobby.displayNameTag
                        : friend.currentMinigame.getMinigameType().displayNameTag)
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
        return player.getPlayerData().name;
    }

}