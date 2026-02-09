package com.brlnsreb.minigames.commands.subcommands;

import java.util.LinkedList;

import cn.nukkit.command.data.CommandParameter;

public abstract class ComplexSubCommand extends SimpleSubCommand {
    
    public ComplexSubCommand(String name) {
        super(name);
    }

    public abstract LinkedList<CommandParameter[]> getComplexParametersList();

    @Override
	public final CommandParameter[] getParameters() { return null; }

}
