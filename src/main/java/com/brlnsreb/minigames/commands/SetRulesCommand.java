package com.brlnsreb.minigames.commands;

import com.brlnsreb.minigames.MinigameCore;

import org.powernukkitx.command.Command;
import org.powernukkitx.command.CommandSender;
import org.powernukkitx.command.data.CommandParameter;
import org.powernukkitx.utils.TextFormat;
import org.powernukkitx.Player;
import org.powernukkitx.level.GameRule;
import org.powernukkitx.level.GameRules;
import org.powernukkitx.level.format.LevelProvider;

public class SetRulesCommand extends Command {

    MinigameCore plugin;
    
    public SetRulesCommand(MinigameCore plugin) {
        super("setrules");
        this.setDescription("Set optimal gamerules for the world you're in");
        this.setPermission("admin");

        this.getCommandParameters().clear();

        this.addCommandParameters("levelType", new CommandParameter[] {
            CommandParameter.newEnum("levelType", new String[]{
                "lobby",
                "mm-arena"
            })
        });

        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (!sender.isPlayer()) {
            sender.sendMessage(TextFormat.RED + "Only players can use this command!");
            return true;
        }

        if (!sender.isOp()) {
            sender.sendMessage(TextFormat.RED + "No permission!");
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(TextFormat.RED + "Usage: /setrules <levelType>");
            return true;
        }

        Player player = (Player) sender;
        GameRules gameRules = player.getLevel().getGameRules();

        //particular
        switch (args[0].toLowerCase()) {

            case "lobby":
                gameRules.setGameRule(GameRule.NATURAL_REGENERATION, true);
                gameRules.setGameRule(GameRule.PVP, false);
                break;

            case "mm-arena":
                gameRules.setGameRule(GameRule.NATURAL_REGENERATION, false);
                gameRules.setGameRule(GameRule.PVP, true);
                break;
            
            default:
                player.sendMessage(TextFormat.RED + "Usage: /setrules <levelType>");
                return true;
        }

        //universal (gets updated every time a new game needs something particular)
        plugin.getServer().setDefaultGamemode(Player.ADVENTURE);
        plugin.getServer().setDifficulty(0);
        gameRules.setGameRule(GameRule.PVP, true);
        gameRules.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        gameRules.setGameRule(GameRule.DO_ENTITY_DROPS, false);
        gameRules.setGameRule(GameRule.DO_FIRE_TICK, false);
        gameRules.setGameRule(GameRule.DO_INSOMNIA, false);
        gameRules.setGameRule(GameRule.DO_LIMITED_CRAFTING, true);
        gameRules.setGameRule(GameRule.DO_MOB_LOOT, false);
        gameRules.setGameRule(GameRule.DO_MOB_SPAWNING, false);
        gameRules.setGameRule(GameRule.DO_TILE_DROPS, false);
        gameRules.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
        gameRules.setGameRule(GameRule.SHOW_DAYS_PLAYED, false);
        gameRules.setGameRule(GameRule.COMMAND_BLOCKS_ENABLED, true);
        gameRules.setGameRule(GameRule.COMMAND_BLOCK_OUTPUT, false);
        gameRules.setGameRule(GameRule.SEND_COMMAND_FEEDBACK, true);
        gameRules.setGameRule(GameRule.RECIPES_UNLOCK, false);
        gameRules.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true);
        gameRules.setGameRule(GameRule.SHOW_COORDINATES, false);
        gameRules.setGameRule(GameRule.TNT_EXPLODES, false);
        gameRules.setGameRule(GameRule.PROJECTILES_CAN_BREAK_BLOCKS, false);
        gameRules.setGameRule(GameRule.MOB_GRIEFING, false);
        gameRules.setGameRule(GameRule.LOCATOR_BAR, false);
        gameRules.setGameRule(GameRule.DROWNING_DAMAGE, false);
        gameRules.setGameRule(GameRule.FALL_DAMAGE, false);
        gameRules.setGameRule(GameRule.FIRE_DAMAGE, false);
        gameRules.setGameRule(GameRule.FREEZE_DAMAGE, false);

        LevelProvider levelProvider = player.getLevel().requireProvider();
        levelProvider.setGameRules(gameRules);
        levelProvider.saveLevelData();

        return true;
    }

}
