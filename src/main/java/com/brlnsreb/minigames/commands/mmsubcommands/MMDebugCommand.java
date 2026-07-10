package com.brlnsreb.minigames.commands.mmsubcommands;

import java.util.LinkedList;

import org.cloudburstmc.protocol.bedrock.data.actor.ActorFlags;
import org.cloudburstmc.protocol.bedrock.data.command.CommandParamType;

import com.brlnsreb.minigames.MinigameCore;
import com.brlnsreb.minigames.commands.subcommands.BasicSubCommand;
import com.brlnsreb.minigames.core.player.CustomPlayer;
import com.brlnsreb.minigames.lobby.entities.NPCEntity;
import com.brlnsreb.minigames.mm.config.MMConfig;

import org.powernukkitx.Player;
import org.powernukkitx.command.CommandSender;
import org.powernukkitx.command.data.CommandParameter;
import org.powernukkitx.entity.Entity;
import org.powernukkitx.entity.item.EntityItem;
import org.powernukkitx.item.Item;
import org.powernukkitx.level.Position;
import org.powernukkitx.level.format.IChunk;
import org.powernukkitx.level.particle.FloatingTextParticle;
import org.powernukkitx.nbt.tag.CompoundTag;
import org.powernukkitx.utils.ItemHelper;
import org.powernukkitx.utils.TextFormat;

public class MMDebugCommand extends BasicSubCommand {
    
    private final MinigameCore plugin;
    private FloatingTextParticle holo;
    
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
            Position pos = player.getPosition();
            int cx = pos.getFloorX() >> 4;
            int cz = pos.getFloorZ() >> 4;
            switch (args[1]) {
                case "1":
                    Item gold = Item.get(Item.GOLD_INGOT, 0, 1);
                    pos = pos.add(3, 0, 0);

                    CompoundTag nbt = Entity.getDefaultNBT(pos);
                    nbt.putCompound("Item", ItemHelper.write(gold));
                    nbt.putBoolean("Mergeable", false);
                    nbt.putShort("Health", 5);

                    cx = pos.getFloorX() >> 4;
                    cz = pos.getFloorZ() >> 4;

                    EntityItem entity = (EntityItem) Entity.createEntity(
                        Entity.ITEM, 
                        pos.getLevel().getChunk(cx, cz), 
                        nbt
                    );

                    if (entity != null) {
                        entity.addTag("mm_gold");
                        entity.spawnToAll();
                    }

                    break;
                case "2":
                    for (Entity entity2 : player.getLevel().getEntities()) {
                        if (entity2 instanceof EntityItem && entity2.hasTag("mm_gold")) {
                            entity2.close();
                        }
                    }
                    break;
                case "3":
                    player.setDataFlag(ActorFlags.HAS_GRAVITY, false);
                    break;
                case "4":
                    player.setDataFlag(ActorFlags.HAS_GRAVITY, true);
                    break;
                case "5":
                    //plugin.getMMGame().getDeath().createBody(player, player.getNextPosition());
                    break;
                case "npc":
                    Position pos2 = player.getPosition();
                    int cx2 = pos2.getFloorX() >> 4;
                    int cz2 = pos2.getFloorZ() >> 4;

                    if (!pos2.getLevel().isChunkLoaded(cx2, cz2)) {
                        pos2.getLevel().loadChunk(cx2, cz2);
                    }

                    IChunk chunk = (IChunk) pos2.getLevel().getChunk(cx2, cz2);
                    NPCEntity npc = new NPCEntity(chunk, Entity.getDefaultNBT(pos2));

                    npc.setTask(p -> { p.sendMessage("Yea you joined bro or sum like dat"); });
                    npc.setSkin(player.getSkin());
                    npc.updateText("§l§cQuickPlay", "§dJoin: §amm0 §e0§7/§e24");
                    npc.spawnToAll();
                    if (plugin.getDebugVar() == 1) npc.despawnFrom(player);
                    break;
                case "6b":
                    if (!pos.getLevel().isChunkLoaded(cx, cz)) {
                        pos.getLevel().loadChunk(cx, cz);
                    }

                    IChunk chunk2 = (IChunk) pos.getLevel().getChunk(cx, cz);
                    NPCEntity npc2 = new NPCEntity(chunk2, Entity.getDefaultNBT(pos));

                    npc2.setSkin(player.getSkin());
                    npc2.setTextVerticalOffset(Double.parseDouble(args[2]), Double.parseDouble(args[3]));
                    npc2.updateText("§l§cQuickPlay", "§dJoin: §amm0 §e0§7/§e24");
                    npc2.spawnToAll();
                    if (plugin.getDebugVar() == 1) npc2.despawnFrom(player);
                    break;
                case "7":
                    Position pos3 = player.getPosition();
                    Item hoe = Item.get(Item.GOLDEN_HOE, 0, 1);
                    hoe.setCustomName(TextFormat.colorize(config.getSheriffHoeName()));
                    
                    CompoundTag nbt2 = Entity.getDefaultNBT(pos3);
                    nbt2.putCompound("Item", ItemHelper.write(hoe));
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
                case "8":
                    ((CustomPlayer) player).setGameSpectator(true);
                    break;
                case "9":
                    ((CustomPlayer) player).setGameSpectator(false, true);
                    break;
                case "10":
                    holo = new FloatingTextParticle(
                        player,
                        args[2]
                    );
                    player.getLevel().addParticle(holo);
                    break;
                case "12":
                    holo.setTitle(args[2]);
                    break;
                case "13":
                    holo.setInvisible();
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
        parameters.add(CommandParameter.newType("[args...]", CommandParamType.RAW_TEXT));
		return parameters.toArray(new CommandParameter[parameters.size()]);
	}

}
