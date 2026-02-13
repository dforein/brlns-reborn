package com.brlnsreb.minigames.mm.systems;

import cn.nukkit.Player;
import cn.nukkit.scoreboard.Scoreboard;
import cn.nukkit.scoreboard.data.DisplaySlot;
import cn.nukkit.scoreboard.scorer.FakeScorer;
import cn.nukkit.utils.TextFormat;
import com.brlnsreb.minigames.mm.roles.MMRole;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

// TODO: scoreboard astraction into Utils

public class ScoreboardSystem {
    
    private static final String OBJECTIVE_NAME = "mm_scoreboard";
    private static final String DISPLAY_TITLE = TextFormat.colorize("&l&aMurder&2Mystery");

    private final Map<String, Scoreboard> playerBoards = new HashMap<>();

    public void update(Player player, String timer, int innocents, boolean sheriffAlive, MMRole role, boolean isPregame) {
        if (player == null || !player.isOnline()) return;

        Scoreboard sb = playerBoards.computeIfAbsent(player.getName(), k -> {
            Scoreboard newSb = new Scoreboard(OBJECTIVE_NAME, DISPLAY_TITLE);
            newSb.addViewer(player, DisplaySlot.SIDEBAR);
            player.setNameTagVisible(false);
            return newSb;
        });
        
        if (sb == null) return;

        if (!sb.containViewer(player, DisplaySlot.SIDEBAR)) {
            sb.addViewer(player, DisplaySlot.SIDEBAR);
        }

        sb.removeAllLine(true);
        
        if (isPregame) {
            drawPregame(sb, timer);
        } else {
            drawInGame(sb, timer, innocents, sheriffAlive, role);
        }
    }

    private void drawPregame(Scoreboard sb, String timer) {
        addLine(sb, "&a", 10);
        addLine(sb, "  &l&dGame time:", 9);
        addLine(sb, "   &a" + timer, 8);
        addLine(sb, "&b", 7);
    }

    private void drawInGame(Scoreboard sb, String timer, int innocents, boolean sheriffAlive, MMRole role) {
        addLine(sb, "&a", 15);

        addLine(sb, "  &l&aInnocents:", 14);
        addLine(sb, "   &l&a" + innocents, 13);
        
        addLine(sb, "  &l&eSheriff:", 12);
        addLine(sb, "   &a" + (sheriffAlive ? "alive" : "dead"), 11);
        
        addLine(sb, "  &l&dGame time:", 10);
        addLine(sb, "   &a" + timer, 9);
        
        addLine(sb, "  &l&6Role:", 8);
        addLine(sb, "   " + getRoleText(role), 7);

        addLine(sb, "&b", 6);
    }

    private void addLine(Scoreboard sb, String text, int score) {
        sb.addLine(new FakeScorer(TextFormat.colorize(text)), score);
    }

    public void remove(Player player) {
        String name = player.getName();
        Scoreboard sb = playerBoards.remove(name);
        if (sb != null && player.isOnline()) {
            sb.removeViewer(player, DisplaySlot.SIDEBAR);
        }
    }

    public Set<String> getActivePlayerNames() {
        return playerBoards.keySet();
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