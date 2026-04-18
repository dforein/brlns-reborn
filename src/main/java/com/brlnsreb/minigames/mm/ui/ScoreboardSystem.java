package com.brlnsreb.minigames.mm.ui;

import cn.nukkit.Player;
import cn.nukkit.scoreboard.Scoreboard;
import com.brlnsreb.minigames.mm.roles.MMRole;
import com.brlnsreb.minigames.utils.ScoreboardAbstract;

public class ScoreboardSystem extends ScoreboardAbstract {

    public void updatePregame(Player player, String timer) {
        Scoreboard sb = getScoreboard(player);
        if (sb == null) return;

        drawPregame(sb, player.getName(), timer);
    }

    public void updateInGame(Player player, String timer, int innocents, boolean sheriffAlive, MMRole role) {
        Scoreboard sb = getScoreboard(player);
        if (sb == null) return;

        drawInGame(sb, player.getName(), timer, innocents, sheriffAlive, role);
    }

    private void drawInGame(Scoreboard sb, String name, String timer, int innocents, boolean sheriffAlive, MMRole role) {
        int score = 0;
        String[] lines = {
            "&a",
            "  &l&aInnocents:",
            "   &l&a" + innocents,
            "  &l&eSheriff:",
            "   &a" + (sheriffAlive ? "alive" : "dead"),
            "  &l&dGame time:",
            "   &a" + timer,
            "  &l&6Role:",
            "   " + getRoleText(role),
            "&b"
        };

        for (String line : lines) {
            score++;
            setLine(sb, name, line, score);
        }
        
        clearUnusedLines(sb, name, score);
    }

    private String getRoleText(MMRole role) {
        switch (role) {
            case MURDERER:  return "&cmurderer";
            case SHERIFF:   return "&1sheriff";
            case INNOCENT:  return "&ainnocent";
            case SPECTATOR: return "&7spectator";
            default:        return "&cno role";
        }
    }
}