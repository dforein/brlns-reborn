package org.brlnsreb.core.auth;

import org.brlnsreb.BrlnsReb;
import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.core.player.data.database.Outcome;
import org.brlnsreb.core.player.data.database.PlayerDataManager;
import org.brlnsreb.utils.abstraction.MenuAbstract;
import org.brlnsreb.utils.config.Configs;
import org.brlnsreb.utils.config.YamlUtil;
import org.brlnsreb.utils.messages.ChatMsgs;
import org.brlnsreb.utils.messages.Messages;
import org.brlnsreb.utils.messages.ChatMsgs.Alignment;
import org.powernukkitx.Player;
import org.powernukkitx.Server;
import org.powernukkitx.form.response.CustomResponse;
import org.powernukkitx.form.window.CustomForm;
import org.powernukkitx.form.window.SimpleForm;
import org.powernukkitx.scheduler.ServerScheduler;
import org.powernukkitx.utils.Config;

public class AuthSystem extends MenuAbstract {

    private static ServerScheduler scheduler;
    private static Config messages;

    public static void init() {
        scheduler = BrlnsReb.getScheduler();
        messages = Configs.getGlobalMessages();
    }

    public static void openMenu(Player player) {
        if (!checkCooldown(player)) return;

        String path = "auth.menu.";

        CustomForm menu = new CustomForm(messages.getString(path + "title"));
        menu.addLabel(YamlUtil.getStr(path + "label0", messages));
        menu.addInput(YamlUtil.getStr(path + "input1", messages), player.getName());
        menu.addInput(YamlUtil.getStr(path + "input2", messages));
        menu.addToggle(YamlUtil.getStr(path + "toggle3", messages), false);

        int formId = sendForm(player, menu);
        menu.onSubmit((p, response) -> handleResponse((CustomPlayer) p, response, formId));
    }

    public static void handleResponse(CustomPlayer player, CustomResponse response, int formId) {
        removeForm(formId);

        String name = response.getInputResponse(1);
        String password = response.getInputResponse(2);

        player.sendMessage(ChatMsgs.INFO_PFX + "Login in progress. Please wait a moment...");

        if (response.getToggleResponse(3)) {
            registerPlayer(player, name, password);
        } else {
            loginPlayer(player, name, password);
        }
    }

    private static void registerPlayer(CustomPlayer player, String name, String password) {
        PlayerDataManager.registerNewPlayer(player, name, password).thenAccept(outcome -> {
            if (outcome == Outcome.ASYNC_TASK_ALREADY_RUNNING) return;

            if (outcome == Outcome.OK) {
                Messages.sendMessageBlock(player, Alignment.CENTER, true,
                    ChatMsgs.BROKENLENS_GAMES,
                    "§eplay.brlns.reb",
                    "§b@BrokenLensMCPE",
                    "",
                    "§aWelcome §e" + player.data.name + "§a!",
                    "§aHave Fun!"
                );
            }

            player.sendMessage(switch(outcome) {
                case OK -> ChatMsgs.INFO_PFX + YamlUtil.getStr("auth.after-register", messages);
                case INVALID_NAME -> ChatMsgs.ERROR_PFX + "You used an invalid name!";
                case NAME_ALREADY_IN_USE -> ChatMsgs.ERROR_PFX + "Sorry, the name you want is already taken!";
                case PLAYER_ALREADY_LOGGED_IN -> ChatMsgs.ERROR_PFX + "You are already authenticated (username: §e" + player.data.name + "§c)\n"
                                                + ChatMsgs.INFO_PFX + "Type §e/logout §ato switch usernames";
                case DB_ERROR -> ChatMsgs.ERROR_PFX + "Something wrong happened. Report this error to developers: db_error_auth";
                default -> ChatMsgs.ERROR_PFX + "Report this error to developers: switch_register";
            });

            String path = "auth.register-outcome.";
            path += switch (outcome) {
                case OK -> "ok.";
                case INVALID_NAME -> "invalid-name.";
                case NAME_ALREADY_IN_USE -> "name-already-in-use.";
                case PLAYER_ALREADY_LOGGED_IN -> "player-already-logged-in.";
                case DB_ERROR -> "db-error.";
                default -> "error";
            };

            if (path.contains("error")) {
                Server.getInstance().getLogger().error(path);
            }

            SimpleForm responseWindow = new SimpleForm(
                YamlUtil.getStr(path + "title", messages),
                YamlUtil.getStr(path + "content", messages).formatted(name)
            );
            
            scheduler.scheduleTask(() -> responseWindow.addButton("Close").send(player));
        });
    }

    private static void loginPlayer(CustomPlayer player, String name, String password) {
        PlayerDataManager.playerLogin(player, name, password).thenAccept(outcome -> {
            if (outcome == Outcome.ASYNC_TASK_ALREADY_RUNNING) return;

            if (outcome == Outcome.OK) sendLoginMessageBlock(player);
            else player.sendMessage(switch(outcome) {
                case PLAYER_ALREADY_LOGGED_IN -> ChatMsgs.ERROR_PFX + "You are already authenticated (username: §e" + player.data.name + "§c)\n"
                                                + ChatMsgs.INFO_PFX + "Type §e/logout §ato switch usernames";
                case NAME_NOT_FOUND -> ChatMsgs.ERROR_PFX + "The name you typed does not exist!";
                case WRONG_PASSWORD -> ChatMsgs.ERROR_PFX + "Wrong passoword!";
                case DB_ERROR -> ChatMsgs.ERROR_PFX + "Something wrong happened. Report this error to developers: db_error_auth";
                default -> ChatMsgs.ERROR_PFX + "Report this error to developers: switch_login";
            });

            String path = "auth.login-outcome.";
            path += switch (outcome) {
                case OK -> "ok.";
                case PLAYER_ALREADY_LOGGED_IN -> "player-already-logged-in.";
                case NAME_NOT_FOUND -> "name-not-found.";
                case WRONG_PASSWORD -> "wrong-password.";
                case DB_ERROR -> "db-error.";
                default -> "error";
            };

            if (path.contains("error")) {
                Server.getInstance().getLogger().error(path);
            }

            path = YamlUtil.checkConfigPath(path);
            SimpleForm responseWindow = new SimpleForm(
                YamlUtil.getStr(path + "title", messages),
                YamlUtil.getStr(path + "content", messages).formatted(name)
            );
            
            scheduler.scheduleTask(() -> responseWindow.addButton("Close").send(player));
        });
    }

    public static void sendLoginMessageBlock(CustomPlayer player) {
        Messages.sendMessageBlock(player, Alignment.CENTER, true,
            ChatMsgs.BROKENLENS_GAMES,
            "§eplay.brlns.reb",
            "§b@BrokenLensMCPE",
            "",
            "§aWelcome Back §e" + player.data.name + "§a!"
        );
    }

    public static Config getMessages() { return messages; };
}
