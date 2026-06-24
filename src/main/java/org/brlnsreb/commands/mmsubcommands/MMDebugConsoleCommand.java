package org.brlnsreb.commands.mmsubcommands;

import java.util.LinkedList;

import org.cloudburstmc.protocol.bedrock.data.command.CommandParamType;

import org.brlnsreb.BrlnsReb;
import org.brlnsreb.commands.subcommands.BasicSubCommand;
import org.brlnsreb.mm.config.MMConfig;

import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.utils.TextFormat;

public class MMDebugConsoleCommand extends BasicSubCommand {
    
    private final BrlnsReb plugin;
    
    public MMDebugConsoleCommand(BrlnsReb plugin) {
        super("debugconsole");
        this.setAliases(new String[] {
				"debugconsole"
		});

        this.plugin = plugin;
    }

    private void runDebug(CommandSender sender, String[] args) {
        //everything needing debug
        //reminder: args start from args[1] ("/mm debug {args[1]} {args[2]} ...")

        //Map<UUID, Player> players = plugin.getServer().getOnlinePlayers();
        //for (Map.Entry<UUID, Player> p : players.entrySet())

        //if (player instanceof Player) { player = (Player) player; }
        boolean done = true;

        
        if (args.length > 1) {

            //ARGS HERE
            MMConfig config = plugin.getMMGame().getConfig();
            switch (args[1]) {
                case "load":
                    plugin.getServer().loadLevel("museum");
                    break;
                case "unload":
                    plugin.getServer().unloadLevel(plugin.getServer().getLevelByName("museum"));
                    break;
                    
                
                //--- control case (when forgetting break) and continue
                case "controlCase_un2c9r8eyn2cr8yq8294cyrq9o":
                    sender.sendMessage("ERROR: control case activated");
                    break;
                default:
                    done = false;
            }



            //--- always useful
            if (done) return;
            switch (args[1]) {
                case "enable":
                    plugin.getMMGame().checkEnoughPlayers = true;
                    sender.sendMessage("checkEnoughPlayers = true");
                    break;
                case "disable":
                    plugin.getMMGame().checkEnoughPlayers = false;
                    sender.sendMessage("checkEnoughPlayers = false");
                    break;
                case "set":
                    BrlnsReb.setDebugVar(Integer.parseInt(args[2]));
                    sender.sendMessage("debugVar = " + BrlnsReb.getDebugVar());
                    break;
                case "get":
                    sender.sendMessage("debugVar = " + BrlnsReb.getDebugVar());
                    break;
                default:
                    sender.sendMessage("no args");
                    break;
            }
        }
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (!sender.isOp()) {
            sender.sendMessage(TextFormat.RED + "No permission!");
            return true;
        }

        runDebug(sender, args);

        return true;
    }

    @Override
    public CommandParameter[] getParameters() {
		LinkedList<CommandParameter> parameters = new LinkedList<>();
		parameters.add(CommandParameter.newEnum(this.getName(), this.getAliases()));
        parameters.add(CommandParameter.newType("[args...]", CommandParamType.RAW_TEXT));
		return parameters.toArray(new CommandParameter[parameters.size()]);
	}

}
