package com.brlnsreb.minigames.commands.mmsubcommands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import com.brlnsreb.minigames.MinigameCore;
import com.brlnsreb.minigames.commands.MMOperatorCommand;
import com.brlnsreb.minigames.commands.mmsubcommands.mapsubcommands.AddCommand;
import com.brlnsreb.minigames.commands.mmsubcommands.mapsubcommands.AddVolumeCommand;
import com.brlnsreb.minigames.commands.mmsubcommands.mapsubcommands.CountBarriersCommand;
import com.brlnsreb.minigames.commands.mmsubcommands.mapsubcommands.DisableCommand;
import com.brlnsreb.minigames.commands.mmsubcommands.mapsubcommands.EditCommand;
import com.brlnsreb.minigames.commands.mmsubcommands.mapsubcommands.EnableCommand;
import com.brlnsreb.minigames.commands.mmsubcommands.mapsubcommands.InfoCommand;
import com.brlnsreb.minigames.commands.mmsubcommands.mapsubcommands.ListMapsCommand;
import com.brlnsreb.minigames.commands.mmsubcommands.mapsubcommands.ReloadBarriersCommand;
import com.brlnsreb.minigames.commands.mmsubcommands.mapsubcommands.ReloadCommand;
import com.brlnsreb.minigames.commands.mmsubcommands.mapsubcommands.RemoveVolumeCommand;
import com.brlnsreb.minigames.commands.mmsubcommands.mapsubcommands.SaveNewSpawnCommand;
import com.brlnsreb.minigames.commands.mmsubcommands.mapsubcommands.ScanCommand;
import com.brlnsreb.minigames.commands.mmsubcommands.mapsubcommands.ScanForBarriersCommand;
import com.brlnsreb.minigames.commands.subcommands.ComplexSubCommand;
import com.brlnsreb.minigames.commands.subcommands.SubCommand;
import com.brlnsreb.minigames.mm.MurderMysteryGame;

import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.utils.TextFormat;

public class MMMapCommand extends ComplexSubCommand {

    private final List<SubCommand> subCommandsList = new ArrayList<>();
    
    public MMMapCommand(MinigameCore plugin, MurderMysteryGame game, MMOperatorCommand fatherCommand) {
        super("map");
        this.setAliases(new String[] {
				"map"
		});

        SubCommand[] subCommands = new SubCommand[] {
            new ScanCommand(plugin),
            new ScanForBarriersCommand(plugin),
            new CountBarriersCommand(plugin),
            new AddVolumeCommand(game),
            new RemoveVolumeCommand(game),
            new ListMapsCommand(game),
            new InfoCommand(game),
            new ReloadCommand(game),
            new ReloadBarriersCommand(game),
            new SaveNewSpawnCommand(plugin),
            new EnableCommand(game, fatherCommand),
            new DisableCommand(game, fatherCommand),
            new AddCommand(plugin, game, fatherCommand),
            new EditCommand(plugin)
        };

        for (SubCommand subCommand : subCommands) {
            subCommandsList.add(subCommand);
        }
    }

    @Override
	public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (!sender.isOp()) {
            sender.sendMessage(TextFormat.RED + "No permission!");
            return true;
        }

        if(args.length > 1) {
            String subCommandName = args[1].toLowerCase();

            for(SubCommand subCommand : subCommandsList) {
                if(Arrays.asList(subCommand.getAliases()).contains(subCommandName)) {
                    subCommand.execute(sender, commandLabel, args);
                    return true;
                }
            }
        }

        sender.sendMessage(TextFormat.RED + "Usage:   /mm map <subcommand> <args>");
        sender.sendMessage(TextFormat.RED + "More info on README.md on github");

        return true;
    }

    public LinkedList<CommandParameter[]> getComplexParametersList() {
        LinkedList<CommandParameter[]> parametersList = new LinkedList<>();

        for (SubCommand subCommand : subCommandsList) {

            List<List<CommandParameter>> overloadsToProcess = new LinkedList<>();

            //getting parameters
            if (subCommand.hasOverloads()) {
                for (List<CommandParameter> originalSubList : subCommand.getParametersOverloads()) {
                    overloadsToProcess.add(new LinkedList<>(originalSubList));
                }
            } else {
                overloadsToProcess.add(new LinkedList<>(subCommand.getParametersList()));
            }

            //adding "map" subcommand param
            for (List<CommandParameter> parameters : overloadsToProcess) {
                ((LinkedList<CommandParameter>) parameters).push(
                    CommandParameter.newEnum(this.getName(), this.getAliases())
                );

                parametersList.add(parameters.toArray(new CommandParameter[0]));
            }
        }

        return parametersList;
    }

}
