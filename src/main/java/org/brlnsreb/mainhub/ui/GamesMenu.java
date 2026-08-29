package org.brlnsreb.mainhub.ui;

import org.brlnsreb.core.minigame.Minigame;
import org.brlnsreb.core.minigame.MinigameManager;
import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.mainhub.MainHub;
import org.brlnsreb.utils.abstraction.MenuAbstract;
import org.brlnsreb.utils.config.Configs;
import org.brlnsreb.utils.config.YamlUtil;
import org.powernukkitx.Player;
import org.powernukkitx.form.window.SimpleForm;
import org.powernukkitx.utils.Config;

public class GamesMenu extends MenuAbstract {

    public static void openMenu(Player player) {
        if (!checkCooldown(player)) return;

        Config globalConfig = Configs.getGlobalConfig();

        SimpleForm menu = new SimpleForm(YamlUtil.getStr("lobby.items.games.title", globalConfig));

        menu.addButton(YamlUtil.getStr("lobby.items.games.hub-button-text", globalConfig));

        for (Minigame mg : MinigameManager.getMinigames()) {
            menu.addButton(
                mg.mgt.displayName + " §r§8(§r" + mg.getPlayerCount() + "§8)"
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
