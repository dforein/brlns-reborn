package org.brlnsreb;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.level.Level;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.registry.Registries;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.TextFormat;

import org.brlnsreb.utils.YamlUtil;

import java.util.ArrayList;

import org.brlnsreb.core.WorldManager;
import org.brlnsreb.core.auth.AuthSystem;
import org.brlnsreb.core.lobby.entities.NPCEntity;
import org.brlnsreb.core.minigame.MinigameManager;
import org.brlnsreb.core.minigame.MinigameType;
import org.brlnsreb.core.player.PlayerDataManager;
import org.brlnsreb.generallobby.GeneralLobby;
import org.brlnsreb.minigames.mm.entities.DeadBodyEntity;
import org.brlnsreb.minigames.mm.entities.ThrownSwordEntity;

public class BrlnsReb extends PluginBase {
    
    private static BrlnsReb instance;
    private static Server server;

    private MinigameManager minigameManager;
    private GeneralLobby generalLobby;

    private final String[] RESOURCES = {
            "global/config.yml",
            "global/database.yml",
            "global/messages.yml",
            "general-lobby/config.yml",
            "general-lobby/messages.yml"
        };

    private static boolean globalChat = false;
    private static boolean saveAtShutdown = false;
    private static int debugVar = 0;
    
    @Override
    public void onLoad() {

        instance = this;
        this.getLogger().info(TextFormat.WHITE + "BrokenLens Reborn server loading...");

        try {
            Registries.ENTITY.registerCustomEntity(this, DeadBodyEntity.class);
            Registries.ENTITY.registerCustomEntity(this, ThrownSwordEntity.class);
            Registries.ENTITY.registerCustomEntity(this, NPCEntity.class);
            Registries.ENTITY.rebuildTag();
            
        } catch (cn.nukkit.registry.RegisterException e) {
            getLogger().error("Error during entities registration: " + e.getMessage());
            throw new RuntimeException(e);
        }
        
    }
    
    @Override
    public void onEnable() {
        server = getServer();

        saveAllResources();

        WorldManager.init();
        AuthSystem.init();
        PlayerDataManager.init();

        prepareGeneralLobby();
        minigameManager = new MinigameManager();

        
        this.getLogger().info(TextFormat.DARK_GREEN + "BrokenLens Reborn server enabled!");
    }

    @Override
    public void beforeStop() {
        MinigameManager.forceStop();

        for (Player p : server.getOnlinePlayers().values()) {
            PlayerDataManager.savePlayerData(p.getUniqueId());
            p.save();
        }
    }
    
    @Override
    public void onDisable() {
        this.getLogger().info(TextFormat.DARK_RED + "BrokenLens Reborn server disabled!");

        if (saveAtShutdown) return;
        for (Level level : new ArrayList<>(server.getLevels().values())) {
            if (!level.getName().equals(server.getDefaultLevel().getName())) {
                server.unloadLevel(level, true);
            }
        }
    }

    private void saveAllResources() {
        for (String file : RESOURCES) { 
            saveResource(file, false); 
        }
        for (MinigameType minigame : MinigameType.values()) {
            saveResource(minigame.getNameTag() + "/config.yml", false);
            saveResource(minigame.getNameTag() + "/messages.yml", false);
        }
    }

    private void prepareGeneralLobby() {
        this.generalLobby = new GeneralLobby();
        Config config = generalLobby.getConfig();

        server.setDefaultLevel(
            server.getLevelByName(config.getString("lobby.world"))
        );

        server.getDefaultLevel().setSpawnLocation(
            YamlUtil.parseVector3Centered(config.getString("lobby.spawn-pos"))
        );

        server.getDefaultLevel().save();
    }

    public static BrlnsReb getInstance() { return instance; }
    public static boolean getGlobalChat() { return globalChat; }
    public static void setGlobalChat(boolean value) { globalChat = value; }
    public static boolean getSave() { return saveAtShutdown; }

    public static void setSave(boolean value) {
        saveAtShutdown = value;

        for (Level level : server.getLevels().values()) {
            level.setAutoSave(saveAtShutdown);
        }
    }

    public static int getDebugVar() { return debugVar; }
    public static void setDebugVar(int value) { debugVar = value; }

    public GeneralLobby getGeneralLobby() { return generalLobby; }
    public MinigameManager getMinigameManager() { return minigameManager; }
}