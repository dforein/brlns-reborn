package org.brlnsreb.core.player.data;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.brlnsreb.core.minigame.Minigame;
import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.core.player.PlayerUtils;
import org.brlnsreb.core.player.data.database.PlayerDataManager;

public class PlayerData {

    public String name;      //this is also the account username, if it's null the player is not logged in
    private volatile int coins;
    private volatile int exp;
    private double level;
    private int levelFloor;
    private ConcurrentHashMap<Integer, int[]> stats = new ConcurrentHashMap<>();  //HashMap<[if global: 0; else MinigameType id], [array of stats values, value indexes: StatType id]>
    private Set<String> friends = ConcurrentHashMap.newKeySet();
    private Set<String> receivedFriendRequests = ConcurrentHashMap.newKeySet();
    private Set<String> sentFriendRequests = ConcurrentHashMap.newKeySet();
    private boolean friendsAlerts = true;      //if active, the player receives alerts of friends joining the server/a minigame (default: true)
    private boolean friendsNotify = true;      //if active, friends receive alerts of the player joining the server/a minigame (default: true)

    public PlayerData() {
        resetData();
    }

    public void resetData() {
        this.name = null;
        this.coins = -1;
        this.exp = -1;
        this.level = -1.0;
        this.levelFloor = -1;
        this.stats.clear();
        this.friends.clear();
        this.receivedFriendRequests.clear();
        this.sentFriendRequests.clear();
    }

    public boolean isLogged() {
        return this.name != null;
    }


    //coins

    public void setCoins(int coins) { this.coins = coins; }

    public void addCoins(int deltaCoins) {
        if (this.coins < -deltaCoins) {
            this.coins = 0;
        } else {
            this.coins += deltaCoins;
        }
    }

    public boolean checkCost(int coinsCost) {
        return coinsCost <= this.coins;
    }

    public int getCoins() { return this.coins; }


    //exp and levels

    public void setExp(int exp) { this.exp = exp; }

    public void addExp(int deltaExp) {
        this.setExp(this.exp + deltaExp);
    }

    public void updateLevel() {
        /**
         * the level growth functions are based on values gathered from different yt videos, 
         * at different levels (3-5, 40-60, 149, 7000+, 8000+), considering a more probable 
         * use of exp boosters at higher level (=> higher player longevity) to correct some 
         * weird data (i.e. abnormal level growth compared to the other values at lower levels)
         */

        int expThreshold = 562500;          //equivalent to 150 levels
        int levelThreshold = 150;
        double expPerHighLevel = 7500.0;    //reciprocal of the derivative of the level function lvl(exp) at x=150

        if (this.exp < expThreshold) {
            this.level = Math.sqrt((double) this.exp) / 5.0;     //lvl(exp)
            this.levelFloor = (int) this.level;
        } else {
            // using the derivative of lvl(exp) to get a linear constant growth
            int temp = this.exp - expThreshold;
            this.level = levelThreshold + temp / expPerHighLevel;
            this.levelFloor = (int) this.level;
        }
    }

    public int getExp() { return this.exp; }
    public double getLevel() { return this.level; }
    public int getFloorLevel() { return this.levelFloor; }


    //stats

    public void setStat(int minigameId, int statType, int value) {
        int[] minigameStats = getMinigameStats(minigameId);
        minigameStats[statType - 1] = value;
    }

    public void incrementGlobalStat(StatType stat) {
        incrementStat(0, stat);
    }

    public void incrementStat(int minigameId, StatType stat) {
        int[] minigameStats = getMinigameStats(minigameId);
        minigameStats[stat.id - 1]++;
    }

    private int[] getMinigameStats(int minigameId) {
        int[] minigameStats = this.stats.get(minigameId);

        if (minigameStats == null) {
            this.stats.put(minigameId, new int[StatType.size]);
            minigameStats = this.stats.get(minigameId);

            for (int i = 0; i < StatType.size; i++) {
                minigameStats[i] = -1;
            }
        }

        return minigameStats;
    }

    public int getStat(Minigame minigame, StatType stat) {
        return getStat(minigame.getId(), stat);
    }

    public int getStat(int minigameId, StatType stat) {
        return this.stats.get(minigameId)[stat.id - 1];
    }

    public int getStatsAmount() {
        int statTypeId, count = 0;

        for (int[] values : stats.values()) {
            for (statTypeId = 0; statTypeId < StatType.size; statTypeId++) {
                if (values[statTypeId] != -1) count++;
            }
        }

        return count;
    }

    public ConcurrentHashMap<Integer, int[]> getStats() { return this.stats; }
    

    //friends

    public void addFriend(String name) {
        this.friends.add(name);
        this.receivedFriendRequests.remove(name);
        this.sentFriendRequests.remove(name);
    }

    public CustomPlayer getFriend(String name) { 
        if (!isFriendWith(name)) return null;
        
        UUID friendId = PlayerDataManager.getPlayerId(name);
        if (friendId == null) return null;

        return PlayerUtils.getPlayer(friendId);
    }

    public boolean isFriendWith(String name) { return this.friends.contains(name); }
    public boolean isFriendWith(CustomPlayer player) { return isFriendWith(player.getPlayerData().name); }

    public void removeFriend(String name) { this.friends.remove(name); }
    public void receiveFriendRequest(String name) { this.receivedFriendRequests.add(name); }
    public void removeReceivedFriendRequest(String name) { this.receivedFriendRequests.remove(name); }
    public void sendFriendRequest(String name) { this.sentFriendRequests.add(name); }
    public void removeSentFriendRequest(String name) { this.sentFriendRequests.remove(name); }
    public void setFriendsAlerts(boolean value) { this.friendsAlerts = value; }
    public void setFriendsNotify(boolean value) { this.friendsNotify = value; }

    public Set<String> getFriends() { return this.friends; }
    public Set<String> getReceivedFriendRequests() { return this.receivedFriendRequests; }
    public Set<String> getSentFriendRequests() { return this.sentFriendRequests; }
    public boolean getFriendsAlerts() { return this.friendsAlerts; }
    public boolean getFriendsNotify() { return this.friendsNotify; }

}