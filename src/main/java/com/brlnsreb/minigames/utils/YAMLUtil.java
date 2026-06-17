package com.brlnsreb.minigames.utils;

import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.math.Vector3;

public class YAMLUtil {

    private static final int X = 0;
    private static final int Y = 1;
    private static final int Z = 2;

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
