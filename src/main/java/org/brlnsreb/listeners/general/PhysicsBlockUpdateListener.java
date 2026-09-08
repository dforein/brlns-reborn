package org.brlnsreb.listeners.general;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.brlnsreb.BrlnsReb;
import org.brlnsreb.core.levels.LevelManager;
import org.powernukkitx.Player;
import org.powernukkitx.Server;
import org.powernukkitx.block.Block;
import org.powernukkitx.block.BlockFallable;
import org.powernukkitx.block.BlockLiquid;
import org.powernukkitx.block.BlockRedstoneWire;
import org.powernukkitx.event.EventHandler;
import org.powernukkitx.event.Listener;
import org.powernukkitx.event.block.BlockUpdateEvent;
import org.powernukkitx.level.Level;
import org.powernukkitx.math.Vector3;

public class PhysicsBlockUpdateListener implements Listener {

    private final Map<Integer, Set<Vector3>> pendingBlocks = new ConcurrentHashMap<>();

    public PhysicsBlockUpdateListener() {
        if (BrlnsReb.isUnderMaintenance()) {
            BrlnsReb.getScheduler().scheduleDelayedRepeatingTask(BrlnsReb.instance, 
                this::checkPendingBlocks, 20, 20
            );
        }
    }

    private void checkPendingBlocks() {
        var entryIterator = pendingBlocks.entrySet().iterator();
        while (entryIterator.hasNext()) {
            var entry = entryIterator.next();
            Level level = Server.getInstance().getLevel(entry.getKey());
            if (level == null) {
                entryIterator.remove();
                continue;
            }

            Integer range = LevelManager.getPhysicsRangeIn(level);
            if (range == null) continue;    //physics not enabled

            Set<Vector3> blockVects = entry.getValue();
            if (range == -1) {              //physics enabled in all world
                for (Vector3 blockVect : blockVects) {
                    Block block = level.getBlock(blockVect);
                    if (!block.isAir() && checkBlockType(block)) {
                        level.scheduleUpdate(block, 1);
                    }
                }
                entryIterator.remove();
                continue;
            }
            
            //physics enabled in player range
            if (level.getPlayers().values().isEmpty()) continue;

            Set<Vector3> removed = ConcurrentHashMap.newKeySet();
            for (Player p : level.getPlayers().values()) {
                for (Vector3 blockVect : blockVects) {
                    if (blockVect.distanceManhattan(p) <= range) {
                        Block block = level.getBlock(blockVect);
                        if (!block.isAir() && checkBlockType(block)) {
                            level.scheduleUpdate(block, 1);
                            removed.add(blockVect);
                        }
                    }
                }
            }
            
            if (!removed.isEmpty()) entry.getValue().removeAll(removed);
            if (blockVects.isEmpty()) entryIterator.remove();
        }
    }

    @EventHandler
    public void onBlockUpdate(BlockUpdateEvent event) {
        //by default the physics are disabled; 
        //if the level is inside enabledPhysicsLevels, the physics are enabled

        if (!BrlnsReb.isUnderMaintenance()) {
            if (LevelManager.arePhysicsEnabledIn(event.getBlock().getLevel())) return;
            event.setCancelled(true);

        } else {
            Block block = event.getBlock();
            Integer range = LevelManager.getPhysicsRangeIn(block.getLevel());
            
            if (range == null) {        //not enabled for all world (not present in EnabledPhysicsLevels)
                event.setCancelled(true);
                addPendingBlock(block);
                return;
            }

            if (range == -1) return;    //enabled for all world
            
            if (block.getLevel().getPlayers().isEmpty()) {
                event.setCancelled(true);
                addPendingBlock(block);
                return;
            }

            for (Player p : block.getLevel().getPlayers().values()) {
                if (block.distanceManhattan(p.add(0, 1)) <= range) return;
            }
            event.setCancelled(true);
            addPendingBlock(block);
        }
    }

    private void addPendingBlock(Block block) {
        if (checkBlockType(block)) {
            pendingBlocks.computeIfAbsent(block.getLevel().getId(), k -> ConcurrentHashMap.newKeySet())
                .add(block.getVector3());
        }
    }

    private boolean checkBlockType(Block block) {
        if (block instanceof BlockLiquid || block instanceof BlockFallable || block instanceof BlockRedstoneWire) {
            return true;
        }
        return false;
    }

}
