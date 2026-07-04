package org.brlnsreb.core;

import java.util.HashMap;

import org.brlnsreb.BrlnsReb;

import cn.nukkit.utils.Config;

public class ConfigManager {
    
    private static final HashMap<String, Config> configMap = new HashMap<>();

    public static Config getGlobalConfig() {
        return getConfig("global/config.yml");
    }

    public static Config getConfig(String filePath) {
        Config config = configMap.get(filePath);
        if (config != null) return config;

        config = new Config(BrlnsReb.getInstance().getDataFolder() + filePath, Config.YAML);
        configMap.put(filePath, config);
        return config;
    }

    public static void reloadConfig() {
        for (Config config : configMap.values()) {
            config.reload();
        }
    }

}
