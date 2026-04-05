package com.brlnsreb.minigames.mm.systems;

import cn.nukkit.Player;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

// TODO: votingsystem astraction into Utils

public class VotingSystem {
    
    private final Map<String, String> mapVotes;      // playerName -> mapId
    private final Map<String, String> timeVotes;     // playerName -> time
    
    private List<String> availableMaps;
    
    public VotingSystem() {
        this.mapVotes = new ConcurrentHashMap<>();
        this.timeVotes = new ConcurrentHashMap<>();
        this.availableMaps = new ArrayList<>();
    }
    
    public void setAvailableMaps(List<String> maps) {
        this.availableMaps = new ArrayList<>(maps);
    }
    
    public List<String> getAvailableMaps() {
        return new ArrayList<>(availableMaps);
    }
    
    public void voteMap(Player player, String mapId) {
        mapVotes.put(player.getName(), mapId);
    }
    
    public void voteTime(Player player, String time) {
        timeVotes.put(player.getName(), time);
    }
    
    public String getPlayerMapVote(Player player) {
        return mapVotes.getOrDefault(player.getName(), "None");
    }
    
    public String getPlayerTimeVote(Player player) {
        return timeVotes.getOrDefault(player.getName(), "None");
    }
    
    public int getMapVoteCount(String mapId) {
        return (int) mapVotes.values().stream().filter(m -> m.equals(mapId)).count();
    }
    
    public int getTimeVoteCount(String time) {
        return (int) timeVotes.values().stream().filter(t -> t.equals(time)).count();
    }
    
    public String getMostVotedMap() {
        if (mapVotes.isEmpty()) {
            return availableMaps.isEmpty() ? null : availableMaps.get(new Random().nextInt(availableMaps.size()));
        }
        
        Map<String, Integer> voteCounts = new HashMap<>();
        for (String map : mapVotes.values()) {
            voteCounts.put(map, voteCounts.getOrDefault(map, 0) + 1);
        }
        
        int maxVotes = Collections.max(voteCounts.values());
        List<String> winners = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : voteCounts.entrySet()) {
            if (entry.getValue() == maxVotes) {
                winners.add(entry.getKey());
            }
        }
        
        return winners.get(new Random().nextInt(winners.size()));
    }
    
    public String getMostVotedTime(List<String> availableTimes) {
        if (timeVotes.isEmpty()) {
            return availableTimes.get(new Random().nextInt(availableTimes.size()));
        }
        
        Map<String, Integer> voteCounts = new HashMap<>();
        for (String time : timeVotes.values()) {
            voteCounts.put(time, voteCounts.getOrDefault(time, 0) + 1);
        }
        
        int maxVotes = Collections.max(voteCounts.values());
        List<String> winners = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : voteCounts.entrySet()) {
            if (entry.getValue() == maxVotes) {
                winners.add(entry.getKey());
            }
        }
        
        return winners.get(new Random().nextInt(winners.size()));
    }
    
    public void removeMapVote(Player player) {
        mapVotes.remove(player.getName());
    }

    public void removeTimeVote(Player player) {
        timeVotes.remove(player.getName());
    }
    
    public void clear() {
        mapVotes.clear();
        timeVotes.clear();
        availableMaps.clear();
    }
}