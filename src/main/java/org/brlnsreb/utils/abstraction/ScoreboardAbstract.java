package org.brlnsreb.utils.abstraction;

import cn.nukkit.scoreboard.Scoreboard;
import cn.nukkit.scoreboard.data.DisplaySlot;
import cn.nukkit.scoreboard.scorer.FakeScorer;
import cn.nukkit.utils.TextFormat;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.core.player.PlayerUtils;

public abstract class ScoreboardAbstract {
    
    protected static final String OBJECTIVE_NAME = "scoreboard";
    protected static final String DISPLAY_TITLE = "";

    protected static final Map<UUID, Map<Integer, FakeScorer>> activeScorers = new HashMap<>();

    protected Scoreboard getScoreboardOrCreate(CustomPlayer player) {
        if (player == null || !player.isOnline()) return null;
        if (player.scoreboard != null) return player.scoreboard;

        Scoreboard sb = new Scoreboard(OBJECTIVE_NAME, DISPLAY_TITLE);
        player.scoreboard = sb;
        if (sb != null) sb.addViewer(player, DisplaySlot.SIDEBAR);

        return sb;
    }

    public void updatePregame(Collection<CustomPlayer> players, String timer) {
        //pre-prepared scoreboard for pregame phase
        String[] lines = {
            "&a",
            "  &l&dGame time:",
            "   &a" + timer,
            "&b"
        };

        update(players, lines);
    }

    public void update(Collection<CustomPlayer> players, String[] lines) {
        //use this to create or update the scoreboard (more players)
        for (CustomPlayer p : players) {
            update(p, lines);
        }
    }

    public void update(CustomPlayer player, String[] lines) {
        //use this to create or update the scoreboard (one player)
        Scoreboard sb = getScoreboardOrCreate(player);
        if (sb == null) return;

        draw(sb, player.getUniqueId(), lines);
    }

    public void remove(CustomPlayer player) {
        //use this to remove the scoreboard, if there is one
        PlayerUtils.removeScoreboard(player);
        activeScorers.remove(player.getUniqueId());
        
        //PlayerUtils.removeScoreboard(player) can be used also outside this class, so in that case activeScorers won't be cleared, 
        //however, this class will be used in already started matches, therefore at match ending it will be deleted (-> no memory leak)
    }


    protected void draw(Scoreboard sb, UUID playerId, String[] lines) {
        int i;

        for (i = 0; i < lines.length; i++) {
            setLine(sb, playerId, lines[i], i);
        }

        clearUnusedLines(sb, playerId, i);      //it's needed, to remove the older lower lines
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

}