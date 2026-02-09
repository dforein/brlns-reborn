package com.brlnsreb.minigames.commands.mmsubcommands;

import com.brlnsreb.minigames.MinigameCore;
import com.brlnsreb.minigames.commands.subcommands.SimpleSubCommand;

import cn.nukkit.command.CommandSender;
import cn.nukkit.level.GameRule;
import cn.nukkit.level.GameRules;
import cn.nukkit.utils.TextFormat;
import cn.nukkit.Player;

public class MMSetRulesCommand extends SimpleSubCommand {

    private final MinigameCore plugin;
    
    public MMSetRulesCommand(MinigameCore plugin) {
        super("setrules");
        this.setAliases(new String[] {
				"setrules"
		});

        this.plugin = plugin;
    }

    @Override
	public boolean execute(CommandSender sender, String commandLabel, String[] args) {

        Player player = (Player) sender;

        if (!player.isOp()) {
            player.sendMessage(TextFormat.RED + "No permission!");
            return true;
        }

        GameRules gameRules = player.getLevel().getGameRules();
        
        plugin.getServer().setDefaultGamemode(Player.ADVENTURE);
        plugin.getServer().setDifficulty(0);
        gameRules.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        gameRules.setGameRule(GameRule.DO_ENTITY_DROPS, false);
        gameRules.setGameRule(GameRule.DO_FIRE_TICK, false);
        gameRules.setGameRule(GameRule.DO_INSOMNIA, false);
        gameRules.setGameRule(GameRule.DO_LIMITED_CRAFTING, true);
        gameRules.setGameRule(GameRule.DO_MOB_LOOT, false);
        gameRules.setGameRule(GameRule.DO_MOB_SPAWNING, false);
        gameRules.setGameRule(GameRule.DO_TILE_DROPS, false);
        gameRules.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
        gameRules.setGameRule(GameRule.DROWNING_DAMAGE, false);
        gameRules.setGameRule(GameRule.FALL_DAMAGE, false);
        gameRules.setGameRule(GameRule.FIRE_DAMAGE, false);
        gameRules.setGameRule(GameRule.FREEZE_DAMAGE, false);
        gameRules.setGameRule(GameRule.LOCATOR_BAR, false);
        gameRules.setGameRule(GameRule.MOB_GRIEFING, false);
        gameRules.setGameRule(GameRule.NATURAL_REGENERATION, true);
        gameRules.setGameRule(GameRule.PROJECTILES_CAN_BREAK_BLOCKS, false);
        gameRules.setGameRule(GameRule.PVP, false);
        gameRules.setGameRule(GameRule.RECIPES_UNLOCK, false);
        gameRules.setGameRule(GameRule.SHOW_COORDINATES, false);
        gameRules.setGameRule(GameRule.SHOW_DAYS_PLAYED, false);
        gameRules.setGameRule(GameRule.TNT_EXPLODES, false);
        
        player.sendMessage(TextFormat.GREEN + "Game rules set!");

        return true;

    }
}
