package org.brlnsreb.core.player.data;

import java.util.concurrent.ConcurrentHashMap;

import org.brlnsreb.core.minigame.Minigame;

public class PlayerData {

    public String name;      //this is also the account username, if it's null the player is not logged in
    private volatile int coins;
    private volatile int exp;
    private double level;
    private int levelFloor;
    private ConcurrentHashMap<Integer, int[]> stats = new ConcurrentHashMap<>();  //HashMap<[if global: 0; else MinigameType id], [array of stats values, value indexes: StatType id]>

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
    }

    public boolean isLogged() {
        return this.name != null;
    }

    public void setCoins(int coins) {
        this.coins = coins;
    }

    public void addCoins(int deltaCoins) {
        if (this.coins < -deltaCoins) {
            this.coins = 0;
        } else {
            this.coins -= deltaCoins;
        }
    }

    public boolean checkCost(int coinsCost) {
        return coinsCost <= this.coins;
    }

    public void addExp(int deltaExp) {
        this.setExp(this.exp + deltaExp);
    }

    public void setExp(int exp) {
        /**
         * the level growth functions are based on values gathered from different yt videos, 
         * at different levels (3-5, 40-60, 149, 7000+, 8000+), considering a more probable 
         * use of exp boosters at higher level (=> higher player longevity) to correct some 
         * weird data (i.e. abnormal level growth compared to the other values at lower levels)
         */
        
        this.exp = exp;
    }

    public void updateLevel() {
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

    public void setStat(int minigameId, int statType, int value) {
        int[] minigameStats = this.stats.get(minigameId);

        if (minigameStats == null) {
            this.stats.put(minigameId, new int[StatType.size]);
            minigameStats = this.stats.get(minigameId);

            for (int i = 0; i < StatType.size; i++) {
                minigameStats[i] = -1;
            }
        }

        minigameStats[statType - 1] = value;
    }

    public void incrementGlobalStat(StatType stat) {
        incrementStat(0, stat);
    }

    public void incrementStat(int minigameId, StatType stat) {
        int[] minigameStats = this.stats.get(minigameId);

        if (minigameStats == null) {
            this.stats.put(minigameId, new int[StatType.size]);
            minigameStats = this.stats.get(minigameId);
            
            for (int i = 0; i < StatType.size; i++) {
                minigameStats[i] = -1;
            }
        }
        
        minigameStats[stat.id - 1]++;
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

    public int getCoins() { return this.coins; }
    public int getExp() { return this.exp; }
    public ConcurrentHashMap<Integer, int[]> getStats() { return this.stats; }
    public double getLevel() { return this.level; }
    public int getFloorLevel() { return this.levelFloor; }

}
