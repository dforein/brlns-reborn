package com.brlnsreb.minigames.commands.subcommands;

import java.util.LinkedList;

import cn.nukkit.command.data.CommandParameter;

public abstract class SubCommand extends SimpleSubCommand {
    
    public SubCommand(String name) {
        super(name);
    }

    public LinkedList<CommandParameter> getParametersList() {
		LinkedList<CommandParameter> parameters = new LinkedList<>();
		parameters.add(CommandParameter.newEnum(this.getName(), this.getAliases()));
        
		return parameters;
	}

	public boolean hasOverloads() {
		return false;
	}

	public LinkedList<LinkedList<CommandParameter>> getParametersOverloads() {
		return null;
	}

	@Override
	public final CommandParameter[] getParameters() { return null; }
    
}
