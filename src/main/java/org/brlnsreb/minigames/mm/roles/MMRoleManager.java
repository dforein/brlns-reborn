package org.brlnsreb.minigames.mm.roles;

import org.powernukkitx.Player;
import org.powernukkitx.IPlayer;

import java.util.*;

import org.brlnsreb.minigames.mm.MurderMysteryGame;
import org.brlnsreb.minigames.mm.config.MMConfig;
import org.brlnsreb.minigames.mm.systems.ItemManager;

public class MMRoleManager {
    
    private final Map<UUID, GamePlayer> gamePlayers;
    private GamePlayer murderer;
    private GamePlayer sheriff;
    
    public MMRoleManager() {
        this.gamePlayers = new HashMap<>();
    }
    
    public void addPlayer(Player player) {
        gamePlayers.put(player.getUniqueId(), new GamePlayer(player));
    }

    public void removePlayer(IPlayer player) {
        if (player == null) return;

        GamePlayer gp = gamePlayers.remove(player.getUniqueId());

        if (gp != null) {
            if (gp == murderer) {
                murderer = null;
            }
            if (gp == sheriff) {
                sheriff = null;
            }
        }
    }
    
    public GamePlayer getGamePlayer(Player player) {
        return gamePlayers.get(player.getUniqueId());
    }
    
    public void assignRoles(List<Player> playerList) {
        List<Player> shuffled = new ArrayList<>(playerList);
        Collections.shuffle(shuffled);
        int i = 0;
        
        for (Player p : shuffled) {
            GamePlayer gp = getGamePlayer(p);

            if (gp == null) {
                gp = new GamePlayer(p);
                gamePlayers.put(p.getUniqueId(), gp);
            }

            if (gp.getRole() == MMRole.SPECTATOR) continue;
            
            if (i == 0) {
                gp.setRole(MMRole.MURDERER);
                murderer = gp;
            } else if (i == 1) {
                gp.setRole(MMRole.SHERIFF);
                sheriff = gp;
            } else {
                gp.setRole(MMRole.INNOCENT);
            }
            
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
        for (GamePlayer gp : gamePlayers.values()) {
            if (gp.getRole() == MMRole.INNOCENT && 
                gp.isAlive() && 
                gp.getPlayer().isOnline()) {
                innocents.add(gp);
            }
        }
        return innocents;
    }
    
    public int getAliveInnocentsCount() {
        int count = 0;
        for (GamePlayer gp : gamePlayers.values()) {
            if (gp.getRole() == MMRole.INNOCENT && 
                gp.isAlive() && 
                gp.getPlayer().isOnline()) {
                count++;
            }
        }
        return count;
    }
    
    public boolean allInnocentsDead() {
        return getAliveInnocentsCount() == 0;
    }
    
    public boolean isMurdererDead() {
        return murderer == null || !murderer.getPlayer().isOnline() || !murderer.isAlive();
    }
    
    public boolean isSheriffDead() {
        return sheriff == null || !sheriff.getPlayer().isOnline() || !sheriff.isAlive();
    }
    
    public Collection<GamePlayer> getAllPlayers() {
        return gamePlayers.values();
    }

    public Collection<GamePlayer> getOnlinePlayers() {
        return gamePlayers.values().stream()
            .filter(gp -> gp.getPlayer().isOnline())
            .toList();
    }

    public void checkGoldRewards(MurderMysteryGame game) {
        if (!isSheriffDead()) return;

        MMConfig config = game.getConfig();
        int goldRequired = config.getGoldForGun();
        
        for (GamePlayer gp : gamePlayers.values()) {
            if (gp.isAlive() && 
                gp.getRole() == MMRole.INNOCENT && 
                gp.getPlayer().isOnline()) {
                
                if (gp.getGoldCollected() >= goldRequired) {
                    ItemManager.giveYellowDye(gp.getPlayer(), config.getDyeName());
                }
            }
        }
    }
    
    public void clear() {
        gamePlayers.clear();
        murderer = null;
        sheriff = null;
    }
}