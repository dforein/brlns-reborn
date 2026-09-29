package org.brlnsreb.commands.maintenance.minigames.abstraction;

import org.brlnsreb.core.minigame.MinigameType;
import org.powernukkitx.Player;
import org.powernukkitx.command.Command;
import org.powernukkitx.command.CommandSender;
import org.powernukkitx.utils.TextFormat;

public abstract class MapsCommand extends Command {

    private final MapsSystem form;

    public MapsCommand(MinigameType mgt, MapsSystem form) {
        super(mgt.nameTag);
        setDescription("Manage maps for " + mgt.displayName);
        setPermission("admin");

        this.form = form;
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (!sender.isOp()) {
            sender.sendMessage(TextFormat.RED + "No permission!");
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(TextFormat.RED + "Only players can use this command!");
            return true;
        }

        form.openForm(player);
        return true;
    }
    
}
