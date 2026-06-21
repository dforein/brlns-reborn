package org.brlnsreb.mm.roles;

import java.util.UUID;

import cn.nukkit.Player;
import cn.nukkit.Server;

public class GamePlayer {
    //TODO: abstract to interface, so that it can be put inside customplayer in a mutable variable
    
    private final UUID playerUUID;
    private MMRole role;
    private int goldCollected;
    private boolean alive;
    private long lastShotTime;
    private long lastSwordThrowTime;
    private boolean usedFlash;
    private int expEarned;
    
    public GamePlayer(Player player) {
        this.playerUUID = player.getUniqueId();
        this.role = null;
        this.goldCollected = 0;
        this.alive = true;
        this.lastShotTime = 0;
        this.lastSwordThrowTime = 0;
        this.usedFlash = false;
        this.expEarned = 0;
    }

    public Player getPlayer() {
        return Server.getInstance().getPlayer(playerUUID).orElse(null);
    }
    
    public UUID getUUID() {
        return playerUUID;
    }
    
    public MMRole getRole() {
        return role;
    }
    
    public void setRole(MMRole role) {
        this.role = role;
    }
    
    public int getGoldCollected() {
        return goldCollected;
    }
    
    public void addGold(int amount) {
        this.goldCollected += amount;
    }
    
    public boolean isAlive() {
        return alive;
    }
    
    public void setAlive(boolean alive) {
        this.alive = alive;
    }
    
    public boolean canShoot(double cooldown) {
        return (System.currentTimeMillis() - lastShotTime) >= (cooldown * 1000);
    }
    
    public void recordShot() {
        this.lastShotTime = System.currentTimeMillis();
    }
    
    public boolean canThrowSword(int cooldown) {
        return (System.currentTimeMillis() - lastSwordThrowTime) >= (cooldown * 1000);
    }
    
    public void recordSwordThrow() {
        this.lastSwordThrowTime = System.currentTimeMillis();
    }
    
    public boolean hasUsedFlash() {
        return usedFlash;
    }
    
    public void setUsedFlash(boolean used) {
        this.usedFlash = used;
    }
    
    public boolean canBecomeSheriff(int goldRequired) {
        return goldCollected >= goldRequired && alive;
    }

    public int getExpEarned() {
        return expEarned;
    }

    public void addExp(int amount) {
        this.expEarned += amount;
    }
}