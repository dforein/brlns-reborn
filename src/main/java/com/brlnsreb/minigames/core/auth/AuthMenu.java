package com.brlnsreb.minigames.core.auth;

import com.brlnsreb.minigames.MinigameCore;
import com.brlnsreb.minigames.core.player.CustomPlayer;
import com.brlnsreb.minigames.core.player.PlayerDataManager;
import com.brlnsreb.minigames.core.player.PlayerDataManager.Outcome;
import com.brlnsreb.minigames.utils.MenuAbstract;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.form.window.CustomForm;
import cn.nukkit.form.window.SimpleForm;
import cn.nukkit.scheduler.ServerScheduler;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.TextFormat;

public class AuthMenu extends MenuAbstract {

    private static final ServerScheduler scheduler = Server.getInstance().getScheduler();
    private static final Config config = new Config(MinigameCore.getInstance().getDataFolder() + "general-lobby/config.yml", Config.YAML);

    public AuthMenu() {
        super();
    }

    public void openMenu(Player player) {
        checkCooldown(player);

        String path = "auth.menu.";

        CustomForm menu = new CustomForm(config.getString(path + "title"));
        menu.addLabel(config.getString(path + "label0"));
        menu.addInput(TextFormat.colorize(config.getString(path + "input1")), player.getName());
        menu.addInput(config.getString(path + "input2"));
        menu.addToggle(config.getString(path + "toggle3"), false);

        menu.send(player);
    }

    public static void handleResponse(CustomPlayer player, CustomForm window) {
        if (window.response() == null) return;

        String name = window.response().getInputResponse(1);
        String password = window.response().getInputResponse(2);
        

        if (window.response().getToggleResponse(3)) {
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
                case DB_ERROR -> "db-error";
                default -> "error";
            };

            if (path.contains("error")) {
                Server.getInstance().getLogger().error(path);
            }

            SimpleForm responseWindow = new SimpleForm(
                config.getString(path + "title"),
                config.getString(path + "config").formatted(name)
            );
            
            scheduler.scheduleTask(() -> {
                responseWindow.addButton("Close").send(player);
            });
        });
    }

    private static void loginPlayer(CustomPlayer player, String name, String password) {
        PlayerDataManager.playerLogin(player, name, password).thenAccept(outcome -> {
            if (outcome == Outcome.ASYNC_TASK_ALREADY_RUNNING) return;

            String path = "auth.register-outcome.";
            path += switch (outcome) {
                case OK -> "ok.";
                case PLAYER_ALREADY_LOGGED_IN -> "player-already-logged-in.";
                case NAME_NOT_FOUND -> "name-not-found.";
                case WRONG_PASSWORD -> "wrong-password.";
                case DB_ERROR -> "db-error";
                default -> "error";
            };

            if (path.contains("error")) {
                Server.getInstance().getLogger().error(path);
            }

            SimpleForm responseWindow = new SimpleForm(
                config.getString(path + "title"),
                config.getString(path + "config").formatted(name)
            );
            
            scheduler.scheduleTask(() -> {
                responseWindow.addButton("Close").send(player);
            });
        });
    }

    public static Config getConfig() { return config; };
}
