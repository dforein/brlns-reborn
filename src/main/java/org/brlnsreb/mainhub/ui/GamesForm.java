package org.brlnsreb.mainhub.ui;

import org.brlnsreb.core.minigame.Minigame;
import org.brlnsreb.core.minigame.MinigameManager;
import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.mainhub.MainHub;
import org.brlnsreb.utils.abstraction.FormAbstract;
import org.brlnsreb.utils.config.Configs;
import org.brlnsreb.utils.config.YamlUtil;
import org.powernukkitx.Player;
import org.powernukkitx.form.window.SimpleForm;
import org.powernukkitx.utils.Config;

public class GamesForm extends FormAbstract {

    public static void openForm(Player player) {
        if (!checkCooldown(player)) return;

        Config globalConfig = Configs.getGlobalConfig();

        SimpleForm form = new SimpleForm(YamlUtil.getStr("lobby.items.games.title", globalConfig));

        form.addButton(
            YamlUtil.getStr("lobby.items.games.hub-button-text", globalConfig),
            p -> MainHub.instance.onJoin((CustomPlayer) p)
        );

        for (Minigame mg : MinigameManager.getMinigames()) {
            form.addButton(
                mg.mgt.displayName + " §r§8(§r" + mg.getPlayerCount() + "§8)",
                p -> mg.onLobbyJoin((CustomPlayer) p)
            );  //TODO: add images?
        }

        form.send(player);
    }

}
