package com.brlnsreb.minigames.commands.mmsubcommands;

import java.util.LinkedList;

import com.brlnsreb.minigames.MinigameCore;
import com.brlnsreb.minigames.commands.subcommands.BasicSubCommand;
import com.brlnsreb.minigames.lobby.entities.NPCEntity;
import com.brlnsreb.minigames.mm.config.MMConfig;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.data.EntityFlag;
import cn.nukkit.entity.item.EntityItem;
import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import cn.nukkit.level.Location;
import cn.nukkit.level.Position;
import cn.nukkit.level.format.IChunk;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.utils.TextFormat;

public class MMDebugCommand extends BasicSubCommand {
    
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

        //if (player instanceof Player) { player = (Player) player; }
        
        if (args.length > 1) {

            //ARGS HERE
            MMConfig config = plugin.getMMGame().getConfig();
            switch (args[1]) {
                case "1":
                    Item gold = Item.get(Item.GOLD_INGOT, 0, 1);
                    Position pos = player.getPosition().add(3, 0, 0);

                    CompoundTag nbt = Entity.getDefaultNBT(pos);
                    nbt.putCompound("Item", NBTIO.putItemHelper(gold));
                    nbt.putBoolean("mm_gold", true);
                    nbt.putBoolean("Mergeable", false);
                    nbt.putShort("Health", 5);

                    int cx = pos.getFloorX() >> 4;
                    int cz = pos.getFloorZ() >> 4;

                    EntityItem entity = (EntityItem) Entity.createEntity(
                        Entity.ITEM, 
                        pos.getLevel().getChunk(cx, cz), 
                        nbt
                    );

                    if (entity != null) {
                        entity.spawnToAll();
                    }

                    break;
                case "2":
                    for (Entity entity2 : player.getLevel().getEntities()) {
                        if (entity2 instanceof EntityItem && entity2.namedTag != null && entity2.namedTag.getBoolean("mm_gold")) {
                            entity2.close();
                        }
                    }
                    break;
                case "3":
                    player.setDataFlag(EntityFlag.HAS_GRAVITY, false);
                    break;
                case "4":
                    player.setDataFlag(EntityFlag.HAS_GRAVITY, true);
                    break;
                case "5":
                    try {
                        Level lobby = plugin.getServer().getLevelByName(config.getLobbyWorld());
                        
                        Vector3 spawnPos = config.getLobbySpawn();
                        Location lobbySpawn = new Location(spawnPos.x, spawnPos.y, spawnPos.z, lobby);

                        int spawnX = spawnPos.getFloorX() >> 4;
                        int spawnZ = spawnPos.getFloorZ() >> 4;

                        for (int x = spawnX - 1; x <= spawnX + 1; x++) {
                            for (int z = spawnZ - 1; z <= spawnZ + 1; z++) {
                                lobby.loadChunk(x, z);
                            }
                        }

                        player.setMotion(new Vector3(0, 0, 0));
                        player.setFlying(false);
                        player.setAllowFlight(false);
                        player.setCheckMovement(false); //add to refreshplayerstate as true but delayed
                        player.setMotion(new Vector3(0, 0, 0));

                        player.teleport(lobbySpawn);

                        plugin.getServer().getScheduler().scheduleDelayedTask(plugin, () -> {
                            if (player.isOnline()) { player.setCheckMovement(true); }
                        }, 80);
                        

                    } catch (Exception e) {
                        plugin.getLogger().error("Error returning player to lobby: " + e.getMessage());
                    }
                    break;
                
                case "6":
                    try {
                        Level lobby = plugin.getServer().getLevelByName(config.getLobbyWorld());
                        
                        Vector3 spawnPos = config.getLobbySpawn();
                        Location lobbySpawn = new Location(spawnPos.x, spawnPos.y, spawnPos.z, lobby);

                        player.setMotion(new Vector3(0, 0, 0));
                        player.setFlying(false);
                        player.setAllowFlight(false);
                        player.setCheckMovement(false);
                        player.setMotion(new Vector3(0, 0, 0));

                        player.teleport(lobbySpawn);

                        plugin.getServer().getScheduler().scheduleDelayedTask(plugin, () -> {
                            if (player.isOnline()) { player.setCheckMovement(true); }
                        }, 80);
                        

                    } catch (Exception e) {
                        plugin.getLogger().error("Error returning player to lobby: " + e.getMessage());
                    }
                    break;
                case "7":
                    if (args.length > 3) {
                    player.setMovementSpeed(Float.parseFloat(args[2])); }
                    player.getFoodData().setFood(Integer.parseInt(args[3]));
                    player.setSprinting(false);
                    break;
                case "8":
                    if (args.length > 2) {
                    player.setMovementSpeed(Float.parseFloat(args[2])); }
                    break;
                case "9":
                    Level lobby = plugin.getServer().getLevelByName(config.getLobbyWorld());
                    
                    Vector3 spawnPos = config.getLobbySpawn();

                    int spawnX = spawnPos.getFloorX() >> 4;
                    int spawnZ = spawnPos.getFloorZ() >> 4;

                    for (int x = spawnX - 1; x <= spawnX + 1; x++) {
                        for (int z = spawnZ - 1; z <= spawnZ + 1; z++) {
                            lobby.unloadChunk(x, z);
                        }
                    }
                    break;
                case "10":
                    Level lobby1 = plugin.getServer().getLevelByName(config.getLobbyWorld());
                        
                    Vector3 spawnPos1 = config.getLobbySpawn();

                    int spawnX1 = spawnPos1.getFloorX() >> 4;
                    int spawnZ1 = spawnPos1.getFloorZ() >> 4;

                    for (int x = spawnX1 - 1; x <= spawnX1 + 1; x++) {
                        for (int z = spawnZ1 - 1; z <= spawnZ1 + 1; z++) {
                            lobby1.loadChunk(x, z);
                        }
                    }
                    break;
                case "11":
                    plugin.getMMGame().getDeath().createBody(player, player.getNextPosition());
                    break;
                case "13":
                    Position pos2 = player.getPosition();
                    int cx2 = pos2.getFloorX() >> 4;
                    int cz2 = pos2.getFloorZ() >> 4;

                    if (!pos2.getLevel().isChunkLoaded(cx2, cz2)) {
                        pos2.getLevel().loadChunk(cx2, cz2);
                    }

                    IChunk chunk = (IChunk) pos2.getLevel().getChunk(cx2, cz2);
                    NPCEntity npc = new NPCEntity(chunk, Entity.getDefaultNBT(pos2));

                    npc.setSkin(player.getSkin());
                    npc.updateLabel("&l&aAmasdihai", "123 sfss sd");
                    npc.spawnToAll();
                    if (plugin.getDebugVar() == 1) npc.despawnFrom(player);
                    break;
                case "14":
                    player.despawnFromAll();
                    break;
                case "15":
                    player.spawnToAll();
                    break;
                case "16":
                    Position pos3 = player.getPosition();
                    Item hoe = Item.get(Item.GOLDEN_HOE, 0, 1);
                    hoe.setCustomName(TextFormat.colorize(config.getSheriffHoeName()));
                    
                    CompoundTag nbt2 = Entity.getDefaultNBT(pos3);
                    nbt2.putCompound("Item", NBTIO.putItemHelper(hoe));
                    nbt2.putShort("Health", 5);
                    nbt2.putShort("Age", -32768);

                    int cx3 = pos3.getFloorX() >> 4;
                    int cz3 = pos3.getFloorZ() >> 4;

                    if (!pos3.getLevel().isChunkLoaded(cx3, cz3)) {
                        pos3.getLevel().loadChunk(cx3, cz3);
                    }

                    EntityItem drop = (EntityItem) Entity.createEntity(
                        Entity.ITEM,
                        pos3.getLevel().getChunk(cx3, cz3),
                        nbt2
                    );
                    
                    if (drop != null) {
                        drop.setNameTagVisible(true);
                        drop.setNameTagAlwaysVisible(true);
                        drop.setNameTag(TextFormat.colorize(config.getSheriffHoeName()));
                        drop.setScale(1.2f);
                        
                        drop.spawnToAll();
                    }
                    break;
                
                //--- control case (when forgetting break) and continue
                case "controlCase_un2c9r8eyn2cr8yq8294cyrq9o":
                    player.sendMessage("ERROR: control case activated");
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

        if (!sender.isPlayer()) {
            sender.sendMessage(TextFormat.RED + "Only players can run this cmd, try instead /mmop debugconsole");
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
