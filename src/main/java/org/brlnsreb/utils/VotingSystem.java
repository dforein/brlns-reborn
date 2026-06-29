package org.brlnsreb.utils;

import cn.nukkit.Player;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class VotingSystem<T> {
    
    private final Map<UUID, T> votes;
    private List<T> availableOptions;
    
    public VotingSystem() {
        this.votes = new HashMap<>();
    }
    
    public void setAvailableOptions(List<T> options) {
        this.availableOptions = options;
    }
    
    public List<T> getAvailableOptions() {
        return availableOptions;
    }
    
    public void vote(Player player, T option) {
        votes.put(player.getUniqueId(), option);
    }
    
    public T getPlayerVote(Player player) {
        return votes.get(player.getUniqueId());
    }
    
    public int getVoteCount(T option) {
        return (int) votes.values().stream().filter(v -> v.equals(option)).count();
    }
    
    public T getMostVoted() {
        if (votes.isEmpty()) {
            return availableOptions.isEmpty() ? null : 
                   availableOptions.get(ThreadLocalRandom.current().nextInt(availableOptions.size()));
        }
        
        Map<T, Integer> voteCounts = new HashMap<>();
        for (T option : votes.values()) {
            voteCounts.put(option, 1 + voteCounts.getOrDefault(option, 0));
        }
        
        int maxVotes = Collections.max(voteCounts.values());
        List<T> winners = new LinkedList<>();
        for (Map.Entry<T, Integer> entry : voteCounts.entrySet()) {
            if (entry.getValue() == maxVotes) {
                winners.add(entry.getKey());
            }
        }
        
        return winners.get(ThreadLocalRandom.current().nextInt(winners.size()));
    }
    
    public Map<T, Integer> getVoteCounts() {
        Map<T, Integer> counts = new HashMap<>();
        for (T option : votes.values()) {
            counts.put(option, counts.getOrDefault(option, 0) + 1);
        }
        return counts;
    }
    
    public void removePlayerVotes(Player player) {
        votes.remove(player.getUniqueId());
    }

}