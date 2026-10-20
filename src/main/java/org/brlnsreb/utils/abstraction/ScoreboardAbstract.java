package org.brlnsreb.utils.abstraction;

import org.powernukkitx.scoreboard.Scoreboard;
import org.powernukkitx.scoreboard.data.DisplaySlot;
import org.powernukkitx.scoreboard.scorer.FakeScorer;
import org.powernukkitx.utils.TextFormat;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.utils.messages.ChatMsgs;

public abstract class ScoreboardAbstract {
    
    protected String title = ChatMsgs.BROKENLENS_REBORN;

    protected static final Map<UUID, Map<Integer, FakeScorer>> activeScorers = new ConcurrentHashMap<>();
    protected static final Map<String, FakeScorer> scorerCache = new ConcurrentHashMap<>();

    protected Scoreboard getScoreboardOrCreate(CustomPlayer player) {
        if (player == null || !player.isOnline()) return null;
        if (player.hasScoreboard()) return player.getScoreboard();

        remove(player);

        Scoreboard sb = new Scoreboard(title, title);
        player.setScoreboard(sb);
        sb.addViewer(player, DisplaySlot.SIDEBAR);

        return sb;
    }

    public void updatePregame(Collection<CustomPlayer> players, String timer) {
        //pre-prepared scoreboard for pregame phase
        String[] lines = {
            "&a",
            "  &l&dGame time:",
            "   &a" + timer,
            "&b",
            "&1",
            "&2",
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

        draw(sb, player.getUUID(), lines);
    }

    public static void remove(CustomPlayer player) {
        //use this to remove the scorers associated to the player
        activeScorers.remove(player.getUUID());
    }


    protected void draw(Scoreboard sb, UUID playerId, String[] lines) {
        int i;

        for (i = 0; i < lines.length; i++) {
            setLine(sb, playerId, lines[i], i);
        }

        clearUnusedLines(sb, playerId, i);      //it's needed, to remove the older lower lines
    }

    protected void setLine(Scoreboard sb, UUID playerId, String text, int score) {
        text = TextFormat.colorize(text);

        Map<Integer, FakeScorer> playerMap = activeScorers.computeIfAbsent(playerId, k -> new HashMap<>());
        FakeScorer oldScorer = playerMap.get(score);
        
        if (oldScorer != null && oldScorer.getName().equals(text)) {
            return;
        }

        FakeScorer scorer = scorerCache.computeIfAbsent(text, FakeScorer::new);

        sb.addLine(scorer, score);
        playerMap.put(score, scorer);

        if (oldScorer != null) {
            sb.removeLine(oldScorer);
        }
    }

    protected void clearUnusedLines(Scoreboard sb, UUID playerId, int maxScore) {
        Map<Integer, FakeScorer> playerMap = activeScorers.get(playerId);
        if (playerMap == null) return;

        playerMap.entrySet().removeIf(entry -> {
            if (entry.getKey() >= maxScore) {
                sb.removeLine(entry.getValue());
                return true;
            }
            return false;
        });
    }

}