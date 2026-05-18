package com.brlnsreb.minigames.core.player;

public class PlayerData {

    public String name = null;
    private int exp = -1;
    private double level = -1.0;
    private int levelFloor = -1;
    public int coins = -1;

    public PlayerData() {
        resetData();
    }

    public void resetData() {
        name = null;
        exp = -1;
        level = -1.0;
        levelFloor = -1;
        coins = -1;
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

        int expThreshold = 562500;          //equivalent to 150 levels
        int levelThreshold = 150;
        double expPerHighLevel = 7500.0;    //reciprocal of the derivative of the level function lvl(exp) at x=150

        if (exp < expThreshold) {
            this.level = Math.sqrt((double) exp) / 5.0;     //lvl(exp)
            this.levelFloor = (int) this.level;
        } else {
            // using the derivative of lvl(exp) to get a linear constant growth
            int temp = exp - expThreshold;
            this.level = levelThreshold + temp / expPerHighLevel;
            this.levelFloor = (int) this.level;
        }
    }

    public int getExp() { return this.exp; }
    public double getLevel() { return this.level; }
    public int getFloorLevel() { return this.levelFloor; }

}
