package org.brlnsreb.commands.maintenance.minigames;

import org.brlnsreb.commands.maintenance.minigames.abstraction.MapsSystem;
import org.brlnsreb.core.minigame.MinigameType;
import org.brlnsreb.utils.messages.ChatMsgs;
import org.powernukkitx.Player;
import org.powernukkitx.form.window.SimpleForm;

public class MMMapsSystem extends MapsSystem {
    
    public MMMapsSystem() {
        super(MinigameType.MURDER_MYSTERY);
    }

    @Override
    public void openMenu(Player player) {
        SimpleForm menu = createMapsForm();
        menu.addButton("Edit gold spawns mapping", p -> goldSpawnsMapping(p));
        menu.send(player);
    }

    private void goldSpawnsMapping(Player player) {
        player.sendMessage(ChatMsgs.INFO_PFX + "This feature has not been implemented yet.");
        //TODO gold spawns
    }

}
