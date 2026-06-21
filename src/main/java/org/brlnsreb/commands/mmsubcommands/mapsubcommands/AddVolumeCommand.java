package org.brlnsreb.commands.mmsubcommands.mapsubcommands;

import java.util.LinkedList;

import org.brlnsreb.commands.subcommands.SimpleSubCommand;
import org.brlnsreb.mm.MurderMysteryGame;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.math.Vector3;
import cn.nukkit.utils.TextFormat;

public class AddVolumeCommand extends SimpleSubCommand {
    
    private final MurderMysteryGame game;

    private Vector3 position1 = null;
    private Vector3 position2 = null;
    
    public AddVolumeCommand(MurderMysteryGame game) {
        super("addvolume");
        this.setAliases(new String[] {
				"addvolume"
		});

        this.game = game;
    }

    @Override
	public boolean execute(CommandSender sender, String commandLabel, String[] args) {

        Player player = (Player) sender;

        if (args.length < 4) {
            player.sendMessage(TextFormat.RED + "Usage: /mmop map addvolume <mapId> <from> <to>");
            player.sendMessage(TextFormat.RED + "Usage: /mmop map addvolume <mapId> savecurrentpos");
            player.sendMessage(TextFormat.RED + "Usage: /mmop map addvolume <mapId> usesavedpos");
            return true;
        }

        if (args[3].equalsIgnoreCase("savecurrentpos")) {
            savePos(player);
            
            return true;
        }

        String mapId = args[2];

        if (args[3].equalsIgnoreCase("usesavedpos")) {
            if (position1 != null && position2 != null) {
                game.getMapper().addVolume(mapId, position1, position2, player.getLevel(), player);
            } else {
                sender.sendMessage(TextFormat.RED + "No usable positions saved!");
            }
            
            return true;
        }

        if (args.length < 9) {
            player.sendMessage(TextFormat.RED + "Usage: /mmop map addvolume <mapId> <from> <to>");
            player.sendMessage(TextFormat.RED + "Usage: /mmop map addvolume <mapId> savecurrentpos");
            player.sendMessage(TextFormat.RED + "Usage: /mmop map addvolume <mapId> usesavedpos");
            return true;
        }
        
        try {
            Vector3 pos1 = new Vector3(
                parseCoordinate(args[3], player.getFloorX()),
                parseCoordinate(args[4], player.getFloorY()),
                parseCoordinate(args[5], player.getFloorZ())
            );
            Vector3 pos2 = new Vector3(
                parseCoordinate(args[6], player.getFloorX()),
                parseCoordinate(args[7], player.getFloorY()),
                parseCoordinate(args[8], player.getFloorZ())
            );
            
            game.getMapper().addVolume(mapId, pos1, pos2, player.getLevel(), player);

        } catch (NumberFormatException e) {
            player.sendMessage(TextFormat.RED + "Invalid coordinates!");
        }

        return true;
    }

    public void savePos(Player player) {
        if (position1 == null && position2 == null) {
            position1 = new Vector3(
                player.getFloorX(),
                player.getFloorY(),
                player.getFloorZ()
            );
            player.sendMessage(TextFormat.GRAY + String.format("Position1 saved: %.1f %.1f %.1f", position1.x, position1.y, position1.z));

        } else if (position2 == null) {
            position2 = new Vector3(
                player.getFloorX(),
                player.getFloorY(),
                player.getFloorZ()
            );
            player.sendMessage(TextFormat.GRAY + String.format("Position2 saved: %.1f %.1f %.1f", position1.x, position1.y, position1.z));
            player.sendMessage(TextFormat.GREEN + "Command is ready");

        } else {
            position1 = position2 = null;
            savePos(player);
        }
    }

    @Override
    public LinkedList<LinkedList<CommandParameter>> getParametersOverloads() {
		LinkedList<LinkedList<CommandParameter>> paramList = new LinkedList<LinkedList<CommandParameter>>();
        LinkedList<CommandParameter> parameters1 = new LinkedList<CommandParameter>();
        LinkedList<CommandParameter> parameters2 = new LinkedList<CommandParameter>();
        LinkedList<CommandParameter> parameters3 = new LinkedList<CommandParameter>();

        CommandParameter param1 = CommandParameter.newEnum(this.getName(), this.getAliases());
        CommandParameter param2 = CommandParameter.newEnum("mapId", game.getConfig().getMaps());

        parameters1.add(param1);
        parameters1.add(param2);
        parameters1.add(CommandParameter.newType("from", CommandParamType.POSITION));
        parameters1.add(CommandParameter.newType("to", CommandParamType.POSITION));
        paramList.add(parameters1);

        parameters2.add(param1);
        parameters2.add(param2);
        parameters2.add(CommandParameter.newEnum("savecurrentpos", new String[] {"savecurrentpos"}));
        paramList.add(parameters2);

		parameters3.add(param1);
        parameters3.add(param2);
        parameters3.add(CommandParameter.newEnum("usesavedpos", new String[] {"usesavedpos"}));
        paramList.add(parameters3);

		return paramList;
	}

    @Override
    public boolean hasOverloads() {
		return true;
	}

    private double parseCoordinate(String arg, double currentPos) {
        if (arg.startsWith("~")) {
            if (arg.length() == 1) return currentPos;
            return currentPos + Double.parseDouble(arg.substring(1));
        }
        return Double.parseDouble(arg);
    }

}
