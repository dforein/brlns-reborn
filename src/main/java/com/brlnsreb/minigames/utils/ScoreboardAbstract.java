package com.brlnsreb.minigames.utils;

import cn.nukkit.Player;
import cn.nukkit.scoreboard.Scoreboard;
import cn.nukkit.scoreboard.data.DisplaySlot;
import cn.nukkit.scoreboard.scorer.FakeScorer;
import cn.nukkit.utils.TextFormat;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public abstract class ScoreboardAbstract {
    
    protected static final String OBJECTIVE_NAME = "scoreboard";
    protected static final String DISPLAY_TITLE = "";

    protected final Map<UUID, Scoreboard> playerBoards = new HashMap<>();
    protected final Map<UUID, Map<Integer, FakeScorer>> activeScorers = new HashMap<>();

    //implement the updateLogic in a separate public update method(s)
    //also implement drawInGame and others in the update method(s)

    protected Scoreboard getScoreboard(Player player) {
        if (player == null || !player.isOnline()) return null;

        Scoreboard sb = playerBoards.computeIfAbsent(player.getUniqueId(), k -> {
            Scoreboard newSb = new Scoreboard(OBJECTIVE_NAME, DISPLAY_TITLE);
            newSb.addViewer(player, DisplaySlot.SIDEBAR);
            return newSb;
        });

        if (!sb.containViewer(player, DisplaySlot.SIDEBAR)) {
            sb.addViewer(player, DisplaySlot.SIDEBAR);
        }

        return sb;
    }

    protected void drawPregame(Scoreboard sb, UUID playerId, String timer) {
        //lower score = higher position, NOT the reverse
        int score = 0;
        String[] lines = {
            "&a",
            "  &l&dGame time:",
            "   &a" + timer,
            "&b"
        };

        for (String line : lines) {
            score++;
            setLine(sb, playerId, line, score);
        }

        clearUnusedLines(sb, playerId, score);      //it's needed, to remove the older lower lines
    }

    protected void setLine(Scoreboard sb, UUID playerId, String text, int score) {

        String colorText = TextFormat.colorize(text);
        Map<Integer, FakeScorer> playerMap = activeScorers.computeIfAbsent(playerId, k -> new HashMap<>());
        
        FakeScorer oldScorer = playerMap.get(score);
        
        if (oldScorer != null && oldScorer.getName().equals(colorText)) {
            return;
        }

        FakeScorer newScorer = new FakeScorer(colorText);
        sb.addLine(newScorer, score);
        playerMap.put(score, newScorer);

        if (oldScorer != null) {
            sb.removeLine(oldScorer);
        }

    }

    protected void clearUnusedLines(Scoreboard sb, UUID playerId, int maxScore) {
        Map<Integer, FakeScorer> playerMap = activeScorers.get(playerId);
        if (playerMap == null) return;

        playerMap.entrySet().removeIf(entry -> {
            if (entry.getKey() > maxScore) {
                sb.removeLine(entry.getValue());
                return true;
            }
            return false;
        });
    }

    public void remove(Player player) {
        UUID playerId = player.getUniqueId();
        Scoreboard sb = playerBoards.remove(playerId);
        if (sb != null && player.isOnline()) {
            player.removeScoreboard(sb);
            sb.removeViewer(player, DisplaySlot.SIDEBAR);
        }
        activeScorers.remove(playerId);
    }

    public Set<UUID> getActivePlayerNames() {
        return playerBoards.keySet();
    }

}