package org.brlnsreb.core.auth;

import org.brlnsreb.core.Configs;
import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.core.player.data.database.Outcome;
import org.brlnsreb.core.player.data.database.PlayerDataManager;
import org.brlnsreb.utils.YamlUtil;
import org.brlnsreb.utils.abstraction.MenuAbstract;

import org.powernukkitx.Player;
import org.powernukkitx.Server;
import org.powernukkitx.form.response.CustomResponse;
import org.powernukkitx.form.window.CustomForm;
import org.powernukkitx.form.window.SimpleForm;
import org.powernukkitx.scheduler.ServerScheduler;
import org.powernukkitx.utils.Config;

public class AuthSystem extends MenuAbstract {

    private static ServerScheduler scheduler;
    private static Config config;

    public static void init() {
        scheduler = Server.getInstance().getScheduler();
        config = Configs.getGlobalMessages();
    }

    public static void openMenu(Player player) {
        if (!checkCooldown(player)) return;

        String path = "auth.menu.";

        CustomForm menu = new CustomForm(config.getString(path + "title"));
        menu.addLabel(YamlUtil.getStr(path + "label0", config));
        menu.addInput(YamlUtil.getStr(path + "input1", config), player.getName());
        menu.addInput(YamlUtil.getStr(path + "input2", config));
        menu.addToggle(YamlUtil.getStr(path + "toggle3", config), false);

        int formId = sendForm(player, menu);
        menu.onSubmit((p, response) -> handleResponse((CustomPlayer) p, response, formId));
    }

    public static void handleResponse(CustomPlayer player, CustomResponse response, int formId) {
        removeForm(formId);

        String name = response.getInputResponse(1);
        String password = response.getInputResponse(2);

        if (response.getToggleResponse(3)) {
            registerPlayer(player, name, password);
        } else {
            loginPlayer(player, name, password);
        }
    }

    private static void registerPlayer(CustomPlayer player, String name, String password) {
        PlayerDataManager.registerNewPlayer(player, name, password).thenAccept(outcome -> {
            if (outcome == Outcome.ASYNC_TASK_ALREADY_RUNNING) return;

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
                YamlUtil.getStr(path + "title", config),
                YamlUtil.getStr(path + "content", config).formatted(name)
            );
            
            scheduler.scheduleTask(() -> responseWindow.addButton("Close").send(player));
        });
    }

    private static void loginPlayer(CustomPlayer player, String name, String password) {
        PlayerDataManager.playerLogin(player, name, password).thenAccept(outcome -> {
            if (outcome == Outcome.ASYNC_TASK_ALREADY_RUNNING) return;

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
                YamlUtil.getStr(path + "title", config),
                YamlUtil.getStr(path + "content", config).formatted(name)
            );
            
            scheduler.scheduleTask(() -> responseWindow.addButton("Close").send(player));
        });
    }

    public static Config getConfig() { return config; };
}
