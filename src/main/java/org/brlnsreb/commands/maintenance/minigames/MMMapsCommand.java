package org.brlnsreb.commands.maintenance.minigames;

import org.brlnsreb.commands.maintenance.minigames.abstraction.MapsCommand;
import org.brlnsreb.core.minigame.MinigameType;

public class MMMapsCommand extends MapsCommand {

    public MMMapsCommand() {
        super(MinigameType.MURDER_MYSTERY, new MMMapsSystem());
    }
    
}