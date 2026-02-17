package com.brlnsreb.minigames.commands.mmsubcommands;

import java.util.LinkedList;

import com.brlnsreb.minigames.MinigameCore;
import com.brlnsreb.minigames.commands.subcommands.SimpleSubCommand;
import com.brlnsreb.minigames.mm.systems.BossBarSystem;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.utils.BossBarColor;
import cn.nukkit.utils.DummyBossBar;
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

        //Map<UUID, Player> players = plugin.getServer().getOnlinePlayers();
        //for (Map.Entry<UUID, Player> p : players.entrySet())

        BossBarSystem bossBarSys = new BossBarSystem();

        if (args.length > 1)
            switch (args[1]) {
                case "show1":
                    DummyBossBar bossBar = new DummyBossBar.Builder(player)
                        .text("§l§7ewqeqwe")
                        .length(100.0f)
                        .color(BossBarColor.PURPLE)
                        .build();
                    
                    player.createBossBar(bossBar);
                    break;
                
                case "show2":
                    DummyBossBar bossBar2 = new DummyBossBar.Builder(player)
                        .text("§l§7- §a23 EXP §7¦ §e24 GOLD §7-")
                        .length(100.0f)
                        .color(BossBarColor.PURPLE)
                        .build();
                    
                    player.createBossBar(bossBar2);
                    break;
                
                case "show3":
                    DummyBossBar bossBar3 = new DummyBossBar.Builder(player)
                        .text("§l§7- §a GOLD §7-")
                        .length(100.0f)
                        .color(BossBarColor.PURPLE)
                        .build();
                    
                    player.createBossBar(bossBar3);
                    break;
                
                case "clear":
                    bossBarSys.clearAll(player);
                    break;

                case "controlCase_un2c9r8eyn2cr8yq8294cyrq9o":
                    player.sendMessage("ERROR: control case activated");
                    break;
                default:
                    player.sendMessage("no args");
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
