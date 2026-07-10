package com.brlnsreb.minigames.commands.subcommands;

import java.util.LinkedList;

import org.powernukkitx.command.data.CommandParameter;

public abstract class ComplexSubCommand extends BasicSubCommand {
    
    public ComplexSubCommand(String name) {
        super(name);
    }

    public abstract LinkedList<CommandParameter[]> getComplexParametersList();

    @Override
	public final CommandParameter[] getParameters() { return null; }

}
