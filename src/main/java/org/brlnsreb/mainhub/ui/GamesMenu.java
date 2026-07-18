package org.brlnsreb.mainhub.ui;

import org.brlnsreb.core.Configs;
import org.brlnsreb.core.minigame.Minigame;
import org.brlnsreb.core.minigame.MinigameManager;
import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.mainhub.MainHub;
import org.brlnsreb.utils.YamlUtil;
import org.brlnsreb.utils.abstraction.MenuAbstract;

import org.powernukkitx.Player;
import org.powernukkitx.form.window.SimpleForm;
import org.powernukkitx.utils.Config;

public class GamesMenu extends MenuAbstract {

    public static void openMenu(Player player) {
        checkCooldown(player);

        Config config = Configs.getGlobalConfig();

        SimpleForm menu = new SimpleForm(YamlUtil.getStr("items.games.name", config));

        menu.addButton(YamlUtil.getStr("items.games.hub-button-text", config));

        for (Minigame mg : MinigameManager.getMinigames()) {
            menu.addButton(
                YamlUtil.getStr("name", mg.getMessages()) + " §r§7(" + mg.getPlayerCount() + ")"
            );  //TODO: add images?
        }

        int formId = sendForm(player, menu);
        menu.onSubmit((p, response) -> handleResponse((CustomPlayer) p, response.buttonId(), formId));
    }

    public static void handleResponse(CustomPlayer player, int buttonId, int formId) {
        removeForm(formId);

        if (buttonId == 0) {
            MainHub.instance.onJoin(player);
        } else {
            MinigameManager.getMinigames().get(buttonId - 1).onLobbyJoin(player);
        }
    }

}
