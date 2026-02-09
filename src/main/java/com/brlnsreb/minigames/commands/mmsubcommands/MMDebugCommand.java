package com.brlnsreb.minigames.commands.mmsubcommands;

import java.util.LinkedList;

import com.brlnsreb.minigames.MinigameCore;
import com.brlnsreb.minigames.commands.subcommands.SimpleSubCommand;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.utils.TextFormat;

public class MMDebugCommand extends SimpleSubCommand {
    
    private final MinigameCore plugin;
    
    public MMDebugCommand(MinigameCore plugin) {
        super("debug");
        this.setAliases(new String[] {
				"debug"
		});

        this.plugin = plugin;
    }

    private void runDebug(Player player, String[] args) {
        //everything needing debug
        //reminder: args start from args[1] ("/mm debug {args[1]} {args[2]} ...")

        switch (args[1]) {
            case "sword":
                plugin.getMMGame().getProjectile().throwSword(player);
            default:
                break;
        }
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (!sender.isOp()) {
            sender.sendMessage(TextFormat.RED + "No permission!");
            return true;
        }

        runDebug((Player) sender, args);
        return true;
    }

    @Override
    public CommandParameter[] getParameters() {
		LinkedList<CommandParameter> parameters = new LinkedList<>();
		parameters.add(CommandParameter.newEnum(this.getName(), this.getAliases()));
        parameters.add(CommandParameter.newType("[args...]", CommandParamType.RAWTEXT));
		return parameters.toArray(new CommandParameter[parameters.size()]);
	}

}
