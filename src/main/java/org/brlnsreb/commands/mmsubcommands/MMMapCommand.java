package org.brlnsreb.commands.mmsubcommands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.brlnsreb.MinigameCore;
import org.brlnsreb.commands.MMOperatorCommand;
import org.brlnsreb.commands.mmsubcommands.mapsubcommands.AddCommand;
import org.brlnsreb.commands.mmsubcommands.mapsubcommands.AddVolumeCommand;
import org.brlnsreb.commands.mmsubcommands.mapsubcommands.CountBarriersCommand;
import org.brlnsreb.commands.mmsubcommands.mapsubcommands.DisableCommand;
import org.brlnsreb.commands.mmsubcommands.mapsubcommands.EditCommand;
import org.brlnsreb.commands.mmsubcommands.mapsubcommands.EnableCommand;
import org.brlnsreb.commands.mmsubcommands.mapsubcommands.InfoCommand;
import org.brlnsreb.commands.mmsubcommands.mapsubcommands.ListMapsCommand;
import org.brlnsreb.commands.mmsubcommands.mapsubcommands.ReloadBarriersCommand;
import org.brlnsreb.commands.mmsubcommands.mapsubcommands.ReloadCommand;
import org.brlnsreb.commands.mmsubcommands.mapsubcommands.RemoveVolumeCommand;
import org.brlnsreb.commands.mmsubcommands.mapsubcommands.SaveNewSpawnCommand;
import org.brlnsreb.commands.mmsubcommands.mapsubcommands.ScanCommand;
import org.brlnsreb.commands.mmsubcommands.mapsubcommands.ScanForBarriersCommand;
import org.brlnsreb.commands.subcommands.ComplexSubCommand;
import org.brlnsreb.commands.subcommands.SimpleSubCommand;
import org.brlnsreb.mm.MurderMysteryGame;

import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.utils.TextFormat;

public class MMMapCommand extends ComplexSubCommand {

    private final List<SimpleSubCommand> subCommandsList = new ArrayList<>();
    
    public MMMapCommand(MinigameCore plugin, MurderMysteryGame game, MMOperatorCommand fatherCommand) {
        super("map");
        this.setAliases(new String[] {
				"map"
		});

        SimpleSubCommand[] subCommands = new SimpleSubCommand[] {
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

        for (SimpleSubCommand subCommand : subCommands) {
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

            for(SimpleSubCommand subCommand : subCommandsList) {
                if(Arrays.asList(subCommand.getAliases()).contains(subCommandName)) {
                    subCommand.execute(sender, commandLabel, args);
                    return true;
                }
            }
        }

        sender.sendMessage(TextFormat.RED + "Usage:   /mmop map <subcommand> <args>");
        sender.sendMessage(TextFormat.RED + "More info on README.md on github");

        return true;
    }

    public LinkedList<CommandParameter[]> getComplexParametersList() {
        LinkedList<CommandParameter[]> parametersList = new LinkedList<>();

        for (SimpleSubCommand subCommand : subCommandsList) {

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
