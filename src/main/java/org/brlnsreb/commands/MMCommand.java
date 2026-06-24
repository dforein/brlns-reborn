package org.brlnsreb.commands;

import cn.nukkit.command.CommandSender;
import cn.nukkit.utils.TextFormat;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

import org.brlnsreb.BrlnsReb;
import org.brlnsreb.commands.subcommands.BasicSubCommand;
import org.brlnsreb.commands.mmsubcommands.MMJoinCommand;
import org.brlnsreb.commands.mmsubcommands.MMJoinAllCommand;
import org.brlnsreb.commands.mmsubcommands.MMLeaveCommand;
import org.brlnsreb.mm.MurderMysteryGame;

public class MMCommand extends cn.nukkit.command.Command {

    private final List<BasicSubCommand> simpleSubCommandsList = new ArrayList<>();

    //private final BrlnsReb plugin;
    
    public MMCommand(BrlnsReb plugin, MurderMysteryGame game) {

        super("mm");
        this.setDescription("Murder Mystery commands");

        
        BasicSubCommand[] simpleSubCommands = new BasicSubCommand[] {
            new MMJoinCommand(game),
            new MMJoinAllCommand(plugin, game),
            new MMLeaveCommand(game)
        };

        //this.plugin = plugin;


        this.getCommandParameters().clear();

        for(BasicSubCommand subCommand : simpleSubCommands) {
            simpleSubCommandsList.add(subCommand);
		    this.addCommandParameters(subCommand.getName(), subCommand.getParameters());
        };

    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {

        if(args.length > 0) {
            String subCommandName = args[0].toLowerCase();

            for(BasicSubCommand subCommand : simpleSubCommandsList) {
                if(Arrays.asList(subCommand.getAliases()).contains(subCommandName)) {
                    subCommand.execute(sender, commandLabel, args);
                    return true;
                }
            }
        }

        //args lenght == 0 or wrong subcommand
        sender.sendMessage(TextFormat.RED + "Game:   /mm <join|joinall|leave|start|stop>");
        sender.sendMessage(TextFormat.RED + "Map:    /mm <map|setrules>");
        sender.sendMessage(TextFormat.RED + "Debug:  /mm <debug>");

        return true;
    }

    public void refreshCommandsParams() {
        this.getCommandParameters().clear();

        for(BasicSubCommand subCommand : simpleSubCommandsList) {
		    this.addCommandParameters(subCommand.getName(), subCommand.getParameters());
        };

    }

}