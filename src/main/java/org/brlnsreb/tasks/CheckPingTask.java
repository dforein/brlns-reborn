package org.brlnsreb.tasks;

import java.util.Arrays;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.powernukkitx.Player;
import org.powernukkitx.Server;
import org.powernukkitx.plugin.annotation.ScheduleTask;
import org.powernukkitx.scheduler.Task;

@ScheduleTask(period = 30)
public class CheckPingTask extends Task {

    private static final int PING_COUNT = 10;
    private static int index = 0;
    private static ConcurrentHashMap<UUID, long[]> pingHistory = new ConcurrentHashMap<>();
    
    @Override
    public void onRun(int currentTick) {
        for (Entry<UUID, Player> player : Server.getInstance().getOnlinePlayers().entrySet()) {
            long[] pings = pingHistory.get(player.getKey());
            if (pings == null) {
                pings = new long[PING_COUNT];
                pingHistory.put(player.getKey(), pings);
            }

            pings[index] = player.getValue().getPing();
        }

        index++;
        if (index >= PING_COUNT) index = 0;
    }

    public static int getMedianAbsDeviation(Player player) {
        //for measuring the average (absolute) variation of ping
        int median = getMedian(player);
        if (median < 0) return -1;

        long[] pings = pingHistory.get(player.getUniqueId());
        long sumAbsDev = 0;
        int samples = 0;
        for (long ping : pings) {
            if (ping < 0) continue;
            sumAbsDev += Math.abs(ping - median);
            samples++;
        }

        if (samples == 0) return -1;
        return (int) (sumAbsDev / samples);
    }

    public static int getMedian(Player player) {
        //for measuring the average ping (not using mean to avoid giving too much weight to random spikes)
        long[] pings = pingHistory.get(player.getUniqueId());
        if (pings == null) return -1;

        long[] validPings = Arrays.stream(pings).filter(ping -> ping >= 0).toArray();
        if (validPings.length == 0) return -1;

        Arrays.sort(validPings);
        int mid = validPings.length / 2;
        return (int) (validPings.length % 2 == 1 
            ? validPings[mid]
            : ((validPings[mid-1] + validPings[mid]) / 2)
        );
    }

    public static void onLeave(Player player) {
        pingHistory.remove(player.getUniqueId());
    }

}
