package org.brlnsreb.core.player.data;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.brlnsreb.core.minigame.Minigame;
import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.core.player.PlayerUtils;
import org.brlnsreb.core.player.data.database.PlayerDataManager;

import org.powernukkitx.Player;

public class PlayerData {

    public String name;      //this is also the account username, if it's null the player is not logged in
    
    private int coins;
    private int exp;
    
    final double EXP_PER_LEVEL = 2262.5;
    private double level;
    private int levelFloor;
    
    private ConcurrentHashMap<Integer, int[]> stats = new ConcurrentHashMap<>();  //HashMap<[if global: 0; else MinigameType id], [array of stats values, value indexes: StatType id]>
    
    private Map<String, String> offlineFriends = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    private Map<String, String> onlineFriends = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    private Map<String, String> receivedFriendRequests = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    private Map<String, String> sentFriendRequests = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    private boolean friendAlerts = true;      //if active, the player receives alerts of friends joining the server/a minigame
    private boolean friendNotify = true;      //if active, friends receive alerts of the player joining the server/a minigame
    private boolean friendRequests = true;    //if active, the player can receive friend requests (default: true)

    private UUID lastPvtPlayerId = null;

    private final Object accountLock = new Object();
    private final Object friendLock = new Object();

    private boolean isChatVisible = true;

    public PlayerData() {
        resetData();
    }

    public void resetData() {
        synchronized (accountLock) {
            this.name = null;
            this.coins = -1;
            this.exp = -1;
            this.level = -1.0;
            this.levelFloor = -1;
        }
        this.stats.clear();
        synchronized (friendLock) {
            this.offlineFriends.clear();
            this.onlineFriends.clear();
            this.receivedFriendRequests.clear();
            this.sentFriendRequests.clear();
        }
    }

    public boolean isLogged() {
        return this.name != null;
    }


    //coins

    public void setCoins(int coins) {
        synchronized (accountLock) { this.coins = coins; }
    }

    public void addCoins(int deltaCoins) {
        synchronized (accountLock) {
            if (this.coins < -deltaCoins) {
                this.coins = 0;
            } else {
                this.coins += deltaCoins;
            }
        }
    }

    public boolean checkCost(int coinsCost) {
        synchronized (accountLock) { return coinsCost <= this.coins; }
    }

    public int getCoins() {
        synchronized (accountLock) { return this.coins; }
    }


    //exp and levels

    public void setExp(int exp) { 
        synchronized (accountLock) { this.exp = exp; }
    }

    public void addExp(int deltaExp) {
        synchronized (accountLock) { this.setExp(this.exp + deltaExp); }
    }

    /**
     * The level growth function is based on values gathered from different yt videos, 
     * at different levels (3-5, 40-60, 149, 7000+, 8000+). It seems constant.
     */
    public void updateLevel() {
        synchronized (accountLock) {
            this.level = exp / EXP_PER_LEVEL;
            this.levelFloor = (int) this.level;
        }
    }

    public int getExp() { synchronized (accountLock) { return this.exp; } }
    public double getLevel() { synchronized (accountLock) { return this.level; } }
    public int getFloorLevel() { synchronized (accountLock) { return this.levelFloor; } }


    //stats

    public void setStat(int minigameId, int statType, int value) {
        int[] minigameStats = getMinigameStats(minigameId);
        synchronized (minigameStats) {
            minigameStats[statType] = value;
        }
    }

    public void incrementGlobalStat(StatType stat) {
        incrementStat(0, stat);
    }

    public void incrementStat(int minigameId, StatType stat) {
        int[] minigameStats = getMinigameStats(minigameId);
        synchronized (minigameStats) {
            minigameStats[stat.id]++;
        }
    }

    private int[] getMinigameStats(int minigameId) {
        return this.stats.computeIfAbsent(minigameId, k -> {
            int[] newStats = new int[StatType.size];
            Arrays.fill(newStats, -1);
            return newStats;
        });
    }

    public int getStat(Minigame minigame, StatType stat) {
        return getStat(minigame.mgt.id, stat);
    }

    public int getStat(int minigameId, StatType stat) {
        int[] minigameStats = this.stats.get(minigameId);
        if (minigameStats == null) return -1;
        synchronized (minigameStats) {
            return minigameStats[stat.id];
        }
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

    public void addFriend(String name, boolean removeRequests) {
        synchronized (friendLock) {
            if (PlayerDataManager.getPlayerId(name) == null) {
                this.offlineFriends.put(name.toLowerCase(), name);
            } else {
                this.onlineFriends.put(name.toLowerCase(), name);
            }

            if (removeRequests) {
                this.receivedFriendRequests.remove(name.toLowerCase());
                this.sentFriendRequests.remove(name.toLowerCase());
            }
        }
    }

    public void removeFriend(String name) { 
        synchronized (friendLock) {
            this.offlineFriends.remove(name.toLowerCase()); 
            this.onlineFriends.remove(name.toLowerCase());
        }
    }

    public void addOnlineFriend(String name) {
        addOnlineFriend(name.toLowerCase(), name);
    }

    public void addOnlineFriend(String nameLowerCase, String name) {
        synchronized (friendLock) {
            if (isFriendWith(name)) {
                this.onlineFriends.put(nameLowerCase, name);
                this.offlineFriends.remove(nameLowerCase);
            }
        }
    }

    public void removeOnlineFriend(String name) { 
        synchronized (friendLock) {
            String removed = this.onlineFriends.remove(name.toLowerCase()); 
            if (removed != null) this.offlineFriends.put(name.toLowerCase(), name);
        }
    }

    public CustomPlayer getFriend(String name) {
        if (!isFriendWith(name)) return null;
        
        UUID friendId = PlayerDataManager.getPlayerId(name);
        if (friendId == null) return null;

        return PlayerUtils.getPlayer(friendId);
    }

    public boolean isFriendWith(String name) { 
        synchronized (friendLock) {
            return this.offlineFriends.containsKey(name.toLowerCase())
                || this.onlineFriends.containsKey(name.toLowerCase()); 
        }
    }

    public boolean isFriendWith(CustomPlayer player) { return isFriendWith(player.data.name); }

    public boolean hasSentRequestTo(String name) {
        return this.sentFriendRequests.containsKey(name.toLowerCase());
    }

    public boolean hasReceivedRequestFrom(String name) {
        return this.receivedFriendRequests.containsKey(name.toLowerCase());
    }

    public void receiveFriendRequest(String name) { synchronized (friendLock) { this.receivedFriendRequests.put(name.toLowerCase(), name); } }
    public void removeReceivedFriendRequest(String name) { synchronized (friendLock) { this.receivedFriendRequests.remove(name.toLowerCase()); } }
    public void sendFriendRequest(String name) { synchronized (friendLock) { this.sentFriendRequests.put(name.toLowerCase(), name); } }
    public void removeSentFriendRequest(String name) { synchronized (friendLock) { this.sentFriendRequests.remove(name.toLowerCase()); } }
    public void setFriendAlerts(boolean value) { synchronized (friendLock) { this.friendAlerts = value; } }
    public void setFriendNotify(boolean value) { synchronized (friendLock) { this.friendNotify = value; } }
    public void setFriendRequestsFlag(boolean value) { synchronized (friendLock) { this.friendRequests = value; } }

    public Map<String, String> getOfflineFriends() { synchronized (friendLock) { return this.offlineFriends; } }
    public List<Entry<String, String>> getOfflineFriendsEntriesCopy() {
        synchronized (friendLock) { return new ArrayList<>(this.offlineFriends.entrySet()); }
    }
    public Map<String, String> getOnlineFriends() { synchronized (friendLock) { return this.onlineFriends; } }
    public List<String> getOnlineFriendsKeysCopy() {
        synchronized (friendLock) { return new ArrayList<>(this.onlineFriends.keySet()); }
    }
    public Map<String, String> getReceivedFriendRequests() { synchronized (friendLock) { return this.receivedFriendRequests; } }
    public Map<String, String> getSentFriendRequests() { synchronized (friendLock) { return this.sentFriendRequests; } }
    public boolean getFriendAlerts() { synchronized (friendLock) { return this.friendAlerts; } }
    public boolean getFriendNotify() { synchronized (friendLock) { return this.friendNotify; } }
    public boolean getFriendRequestsFlag() { synchronized (friendLock) {return this.friendRequests; } }


    //PVT and reply commands

    public void setLastPvtPlayer(Player player) { this.lastPvtPlayerId = player.getUniqueId(); }
    public CustomPlayer getLastPvtPlayer() {
        if (lastPvtPlayerId == null) return null;
        CustomPlayer player = PlayerUtils.getPlayer(lastPvtPlayerId);
        return player;
    }


    //locks
    public Object getAccountLock() { return this.accountLock; }
    public Object getFriendLock() { return this.friendLock; }


    //chat
    public void toggleChatVisible() { this.isChatVisible = !this.isChatVisible; }
    public boolean isChatVisible() { return this.isChatVisible; }

}