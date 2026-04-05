package com.brlnsreb.minigames.commands;

import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.utils.TextFormat;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

import com.brlnsreb.minigames.MinigameCore;
import com.brlnsreb.minigames.commands.subcommands.ComplexSubCommand;
import com.brlnsreb.minigames.commands.subcommands.SimpleSubCommand;
import com.brlnsreb.minigames.commands.mmsubcommands.MMDebugCommand;
import com.brlnsreb.minigames.commands.mmsubcommands.MMMapCommand;
import com.brlnsreb.minigames.commands.mmsubcommands.MMSetRulesCommand;
import com.brlnsreb.minigames.commands.mmsubcommands.MMStartCommand;
import com.brlnsreb.minigames.commands.mmsubcommands.MMStopCommand;
import com.brlnsreb.minigames.mm.MurderMysteryGame;

public class MMOperatorCommand extends Command {

    private final List<SimpleSubCommand> simpleSubCommandsList = new ArrayList<>();
    private final List<ComplexSubCommand> complexSubCommandsList = new ArrayList<>();

    //private final MinigameCore plugin;
    
    public MMOperatorCommand(MinigameCore plugin, MurderMysteryGame game) {

        super("mmop");
        this.setDescription("Murder Mystery operator commands");
        this.setPermission("admin");

        
        SimpleSubCommand[] simpleSubCommands = new SimpleSubCommand[] {
            new MMStartCommand(game),
            new MMStopCommand(game),
            new MMSetRulesCommand(plugin),
            new MMDebugCommand(plugin)
        };

        ComplexSubCommand[] complexSubCommands = new ComplexSubCommand[] {
            new MMMapCommand(plugin, game, this)
        };

        //this.plugin = plugin;


        this.getCommandParameters().clear();

        for(SimpleSubCommand subCommand : simpleSubCommands) {
            simpleSubCommandsList.add(subCommand);
		    this.addCommandParameters(subCommand.getName(), subCommand.getParameters());
        };

        for(ComplexSubCommand subCommand : complexSubCommands) {
            complexSubCommandsList.add(subCommand);

            int i = 0;
            for (CommandParameter[] subParameters : subCommand.getComplexParametersList())  {
		        this.addCommandParameters(subCommand.getName() + i, subParameters);
                i++;
            }
        };

    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        
        if(args.length > 0) {
            String subCommandName = args[0].toLowerCase();

            for(SimpleSubCommand subCommand : simpleSubCommandsList) {
                if(Arrays.asList(subCommand.getAliases()).contains(subCommandName)) {
                    subCommand.execute(sender, commandLabel, args);
                    return true;
                }
            }

            for(ComplexSubCommand subCommand : complexSubCommandsList) {
                if(Arrays.asList(subCommand.getAliases()).contains(subCommandName)) {
                    subCommand.execute(sender, commandLabel, args);
                    return true;
                }
            }
        }

        //args lenght == 0 or wrong subcommand
        sender.sendMessage(TextFormat.RED + "Game:   /mm <start|stop>");
        sender.sendMessage(TextFormat.RED + "Map/World:    /mm <map|setrules>");
        sender.sendMessage(TextFormat.RED + "Debug:  /mm <debug>");

        return true;
    }

    public void refreshCommandsParams() {
        this.getCommandParameters().clear();

        for(SimpleSubCommand subCommand : simpleSubCommandsList) {
		    this.addCommandParameters(subCommand.getName(), subCommand.getParameters());
        };

        for(ComplexSubCommand subCommand : complexSubCommandsList) {
            int i = 0;
            for (CommandParameter[] subParameters : subCommand.getComplexParametersList()) {
		        this.addCommandParameters(subCommand.getName() + i, subParameters);
                i++;
            }
        };
    }

}