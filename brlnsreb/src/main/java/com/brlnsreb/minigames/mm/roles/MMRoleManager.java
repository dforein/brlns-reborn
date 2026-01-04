package com.brlnsreb.minigames.mm.roles;

import cn.nukkit.Player;
import com.brlnsreb.minigames.mm.MurderMysteryGame;
import com.brlnsreb.minigames.mm.config.MMConfig;
import com.brlnsreb.minigames.mm.items.ItemManager;
import java.util.*;

public class MMRoleManager {
    
    private final Map<String, GamePlayer> players;
    private GamePlayer murderer;
    private GamePlayer sheriff;
    
    public MMRoleManager() {
        this.players = new HashMap<>();
    }
    
    public void addPlayer(Player player) {
        players.put(player.getName(), new GamePlayer(player));
    }
    
    public void removePlayer(Player player) {
        players.remove(player.getName());
    }
    
    public GamePlayer getGamePlayer(Player player) {
        return players.get(player.getName());
    }
    
    public void assignRoles(List<Player> playerList) {
        players.clear();
        
        List<Player> shuffled = new ArrayList<>(playerList);
        Collections.shuffle(shuffled);
        int i = 0;
        
        for (Player p : shuffled) {
            GamePlayer gp = new GamePlayer(p);
            if (gp.getRole() == MMRole.SPECTATOR) {
                continue;
            }
            
            if (i == 0) {
                gp.setRole(MMRole.MURDERER);
                murderer = gp;
            } else if (i == 1) {
                gp.setRole(MMRole.SHERIFF);
                sheriff = gp;
            } else {
                gp.setRole(MMRole.INNOCENT);
            }
            
            players.put(p.getName(), gp);
            i++;
        }
    }
    
    public GamePlayer getMurderer() {
        return murderer;
    }
    
    public GamePlayer getSheriff() {
        return sheriff;
    }
    
    public void setSheriff(GamePlayer newSheriff) {
        this.sheriff = newSheriff;
        newSheriff.setRole(MMRole.SHERIFF);
    }
    
    public List<GamePlayer> getInnocents() {
        List<GamePlayer> innocents = new ArrayList<>();
        for (GamePlayer gp : players.values()) {
            if (gp.getRole() == MMRole.INNOCENT && gp.isAlive()) {
                innocents.add(gp);
            }
        }
        return innocents;
    }
    
    public int getAliveInnocentsCount() {
        int count = 0;
        for (GamePlayer gp : players.values()) {
            if (gp.getRole() == MMRole.INNOCENT && gp.isAlive()) {
                count++;
            }
        }
        return count;
    }
    
    public boolean allInnocentsDead() {
        return getAliveInnocentsCount() == 0;
    }
    
    public boolean isMurdererDead() {
        return murderer != null && !murderer.isAlive();
    }
    
    public boolean isSheriffDead() {
        return sheriff != null && !sheriff.isAlive();
    }
    
    public Collection<GamePlayer> getAllPlayers() {
        return players.values();
    }

    public void checkGoldRewards(MurderMysteryGame game) {
        if (!isSheriffDead()) return;

        MMConfig config = game.getConfig();
        int goldRequired = config.getGoldForGun();
        
        for (GamePlayer gp : players.values()) {
            if (gp.isAlive() && gp.getRole() == MMRole.INNOCENT) {
                if (gp.getGoldCollected() >= goldRequired) {
                    ItemManager.giveYellowDye(gp.getPlayer(), config.getDyeName());
                }
            }
        }
    }
    
    public void clear() {
        players.clear();
        murderer = null;
        sheriff = null;
    }
}