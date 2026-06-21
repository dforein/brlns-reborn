package org.brlnsreb.utils;

import java.util.HashMap;

import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.math.Vector3;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.TextFormat;

public class YamlUtil {

    private static final int X = 0;
    private static final int Y = 1;
    private static final int Z = 2;

    private static final HashMap<String, String> cache = new HashMap<>();

    public static void resetCache() {
        cache.clear();
    }

    public static String getStr(String path, Config config) {
        String str = cache.get(config.hashCode() + path);
        if (str == null) return str;

        str = TextFormat.colorize(config.getString(path));
        if (str != null) cache.put(config.hashCode() + path, str);
        return str;
    }

    public static Position parsePosition(String rawCoords, Level level) {
        return new Position(
            parseCoordinate(rawCoords, X),
            parseCoordinate(rawCoords, Y),
            parseCoordinate(rawCoords, Z),
            level
        );
    }

    public static Position parsePositionCentered(String rawCoords, Level level) {
        return new Position(
            parseCoordinate(rawCoords, X) + 0.5,
            parseCoordinate(rawCoords, Y),
            parseCoordinate(rawCoords, Z) + 0.5,
            level
        );
    }

    public static Vector3 parseVector3(String rawCoords) {
        return new Vector3(
            parseCoordinate(rawCoords, X),
            parseCoordinate(rawCoords, Y),
            parseCoordinate(rawCoords, Z)
        );
    }

    public static Vector3 parseVector3Centered(String rawCoords) {
        return new Vector3(
            parseCoordinate(rawCoords, X) + 0.5,
            parseCoordinate(rawCoords, Y),
            parseCoordinate(rawCoords, Z) + 0.5
        );
    }

    public static double parseCoordinate(String rawCoords, int coord) {
        return Double.parseDouble(
            rawCoords.split("\\s+") [coord]
        );
    }

    public static String checkConfigPath(String configPath) {
        if (configPath.isEmpty() || configPath.charAt(configPath.length() - 1) == '.') return configPath;
        return configPath + ".";
    }

}
