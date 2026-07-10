package com.brlnsreb.minigames.commands.subcommands;

import java.util.LinkedList;

import org.powernukkitx.command.Command;
import org.powernukkitx.command.CommandSender;
import org.powernukkitx.command.data.CommandParameter;

public abstract class BasicSubCommand extends Command {
    
    public BasicSubCommand(String name) {
        super(name);
		
		this.getCommandParameters().clear();
    }

	public CommandParameter[] getParameters() {
		LinkedList<CommandParameter> parameters = new LinkedList<>();
		parameters.add(CommandParameter.newEnum(this.getName(), this.getAliases()));

		return parameters.toArray(new CommandParameter[parameters.size()]);
	}
	
	@Override
	public abstract boolean execute(CommandSender sender, String commandLabel, String[] args);
    
}
