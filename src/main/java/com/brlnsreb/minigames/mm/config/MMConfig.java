package com.brlnsreb.minigames.mm.config;

import cn.nukkit.utils.Config;
import cn.nukkit.utils.ConfigSection;

import java.util.ArrayList;
import java.util.List;

import cn.nukkit.math.Vector3;

// TODO: separate messages.yml from config.yml and categorize by minigame
// TODO: config abstraction into Utils

public class MMConfig {
    
    private final Config config;
    
    public MMConfig(Config config) {
        this.config = config;
    }
    
    public int getMinPlayers() {
        return config.getInt("game.min-players", 3);
    }
    
    public int getMaxPlayers() {
        return config.getInt("game.max-players", 20);
    }

    public int getPregameCountdown() {
        return config.getInt("game.pregame-countdown", 10);
    }
    
    public int getGameDuration() {
        return config.getInt("game.game-duration", 420);
    }
    
    public int getSwordThrowCooldown() {
        return config.getInt("murderer.sword-throw-cooldown", 15);
    }
    
    public double getSwordThrowSpeed() {
        return config.getDouble("murderer.sword-throw-speed", 1.5);
    }
    
    public int getBlindnessDuration() {
        return config.getInt("murderer.blindness-duration", 10);
    }
    
    public double getShootCooldown() {
        return config.getDouble("sheriff.shoot-cooldown", 0.2);
    }
    
    public boolean isFriendlyFireDeath() {
        return config.getBoolean("sheriff.friendly-fire-death", true);
    }
    
    public double getRaycastMaxDistance() {
        return config.getDouble("sheriff.raycast-max-distance", 100);
    }
    
    public double getRaycastStep() {
        return config.getDouble("sheriff.raycast-step", 0.2);
    }
    
    public int getGoldForGun() {
        return config.getInt("innocents.gold-for-gun", 5);
    }
    
    public double getAnimationDuration() {
        return config.getDouble("death.animation-duration", 0.4167);
    }
    
    public float getHeadYawOffset() {
        return (float)config.getDouble("death.head-yaw-offset", 35);
    }
    
    public float getHeadPitchOffset() {
        return (float)config.getDouble("death.head-pitch-offset", 40);
    }
    
    public int getRedstoneDrop() {
        return config.getInt("death.redstone-drop", 2);
    }
    
    public int getGoldSpawnIntervalMin() {
        return config.getInt("gold.spawn-interval-min", 5);
    }
    
    public int getGoldSpawnIntervalMax() {
        return config.getInt("gold.spawn-interval-max", 15);
    }
    
    public String getMessage(String key) {
        return config.getString("messages.prefix", "") + config.getString("messages." + key, "ERROR");
    }

    public String getMessageNoPrefix(String key) {
        return config.getString("messages." + key, "ERROR");
    }

    public Vector3 getLobbySpawn() {
        double x = config.getDouble("lobby.spawn.x", 0);
        double y = config.getDouble("lobby.spawn.y", 65);
        double z = config.getDouble("lobby.spawn.z", 0);
        return new Vector3(x, y, z);
    }

    public String getLobbyWorld() {
        return config.getString("lobby.spawn.world", "lobby");
    }

    public int getDeathBlindness() {
        return config.getInt("death.blindness-duration", 2);
    }

    public String getSheriffHoeName() {
        return config.getString("sheriff.hoe-name", "&bSheriff's Hoe");
    }

    public String getMurdererSwordName() {
        return config.getString("murderer.sword-name", "&cMurderer's Sword");
    }

    public String getMurdererBlazeRodName() {
        return config.getString("murderer.blaze-rod-name", "&o&l&6Lights Out");
    }

    public String getDyeName() {
        return config.getString("innocents.dye-name", "&o&l&eCraft The Gun &r&7( Hold / Right Click)");
    }

    public List<String> getEnabledMaps() {
        List<String> maps = new ArrayList<>();

        Object mapsObj = config.get("world.enabled-maps");
        if (mapsObj instanceof List) {
            List<?> rawList = (List<?>) mapsObj;
            for (Object obj : rawList) {
                if (obj instanceof String) {
                    maps.add((String) obj);
                }
            }
        }
        
        return maps;
    }

    public String[] getMaps() {
        ConfigSection section = config.getSection("world.arena-regions");
        if (section == null) return new String[0];
        
        return section.getKeys(false).toArray(new String[0]);
    }

    public String getCountdownBossbar() {
        return config.getString("messages.countdown-start", "&l&eStarting in &c{seconds}s");
    }

    public int getMaxCountdown() {
        return config.getInt("game.max-countdown", 120);
    }

    public int getShortenedCountdown() {
        return config.getInt("game.shortened-countdown", 10);
    }

    public int getMinPlayersStart() {
        return config.getInt("game.min-players-start", 5);
    }

    public String getCountdownBossbarLong() {
        return config.getString("messages.countdown-bossbar-long", "&l&eStarting in &6{minutes}m {seconds}s");
    }

    public String getCountdownBossbarMedium() {
        return config.getString("messages.countdown-bossbar-medium", "&l&eStarting in &6{seconds}s");
    }

    public String getCountdownBossbarShort() {
        return config.getString("messages.countdown-bossbar-short", "&l&eStarting in &c{seconds}s");
    }

    public int getExpPerGold() {
        return config.getInt("exp.per-gold", 10);
    }

    public int getExpPerKill() {
        return config.getInt("exp.per-kill", 15);
    }

    public int getExpSheriffWin() {
        return config.getInt("exp.sheriff-won", 600);
    }

    public int getExpSheriffKilled() {
        return config.getInt("exp.sheriff-kill", 100);
    }

    public int getEndTime() {
        return config.getInt("game.end-time", 7);
    }

    public String getSpectatorItemName() {
        return config.getString("spectator.item-name", "&l&eTeleport to Player");
    }

    public int getMurdererTrackThreshold() {
        return config.getInt("murderer.time-start-tracking", 60);
    }

    public List<String> getAvailableTimes() {
        return config.getStringList("lobby.game-poll.times");
    }
    
    public String getMapDisplayName(String mapKey) {
        return config.getString("world.arena-regions." + mapKey + ".name", mapKey);
    }

    public List<String> getMapBuilders(String mapKey) {
        return config.getStringList("world.arena-regions." + mapKey + ".builders");
    }
    
    public String getMapWeather(String mapKey) {
        return config.getString("world.arena-regions." + mapKey + ".weather", "clear");
    }

    public String getRulesItemName() {
        return config.getString("lobby.rules-book.item-name", "&o&l&fRules &7- Hold / Right Click");
    }

    public String getGamePollItemName() {
        return config.getString("lobby.game-poll.item-name", "&o&l&dGame Poll &7- Hold / Right Click");
    }
}