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
    private final Map<String, Map<Integer, FakeScorer>> activeScorers = new HashMap<>();

    public void update(Player player, String timer, int innocents, boolean sheriffAlive, MMRole role, boolean isPregame) {
        if (player == null || !player.isOnline()) return;

        String name = player.getName();
        Scoreboard sb = playerBoards.computeIfAbsent(name, k -> {
            Scoreboard newSb = new Scoreboard(OBJECTIVE_NAME, DISPLAY_TITLE);
            newSb.addViewer(player, DisplaySlot.SIDEBAR);
            player.setNameTagVisible(false);
            return newSb;
        });

        if (!sb.containViewer(player, DisplaySlot.SIDEBAR)) {
            sb.addViewer(player, DisplaySlot.SIDEBAR);
        }

        if (isPregame) {
            drawPregame(sb, name, timer);
        } else {
            drawInGame(sb, name, timer, innocents, sheriffAlive, role);
        }
    }

    private void drawPregame(Scoreboard sb, String name, String timer) {
        //score lower = higher position, NOT the reverse
        setLine(sb, name, "&a", 1);
        setLine(sb, name, "  &l&dGame time:", 2);
        setLine(sb, name, "   &a" + timer, 3);
        setLine(sb, name, "&b", 4);
        clearUnusedLines(sb, name, 4);
    }

    private void drawInGame(Scoreboard sb, String name, String timer, int innocents, boolean sheriffAlive, MMRole role) {
        setLine(sb, name, "&a", 1);
        setLine(sb, name, "  &l&aInnocents:", 2);
        setLine(sb, name, "   &l&a" + innocents, 3);
        setLine(sb, name, "  &l&eSheriff:", 4);
        setLine(sb, name, "   &a" + (sheriffAlive ? "alive" : "dead"), 5);
        setLine(sb, name, "  &l&dGame time:", 6);
        setLine(sb, name, "   &a" + timer, 7);
        setLine(sb, name, "  &l&6Role:", 8);
        setLine(sb, name, "   " + getRoleText(role), 9);
        setLine(sb, name, "&b", 10);
        clearUnusedLines(sb, name, 10);
    }

    private void setLine(Scoreboard sb, String playerName, String text, int score) {
        String colorText = TextFormat.colorize(text);
        Map<Integer, FakeScorer> playerMap = activeScorers.computeIfAbsent(playerName, k -> new HashMap<>());
        
        FakeScorer oldScorer = playerMap.get(score);
        
        // Se il testo è lo stesso, non facciamo nulla (ottimizzazione estrema)
        if (oldScorer != null && oldScorer.getName().equals(colorText)) {
            return;
        }

        // Aggiungiamo la nuova riga
        FakeScorer newScorer = new FakeScorer(colorText);
        sb.addLine(newScorer, score);
        playerMap.put(score, newScorer);

        // Ora che la nuova è stata aggiunta, rimuoviamo quella vecchia (se esisteva)
        if (oldScorer != null) {
            sb.removeLine(oldScorer);
        }
    }

    private void clearUnusedLines(Scoreboard sb, String playerName, int maxScore) {
        Map<Integer, FakeScorer> playerMap = activeScorers.get(playerName);
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
        String name = player.getName();
        Scoreboard sb = playerBoards.remove(name);
        if (sb != null && player.isOnline()) {
            sb.removeViewer(player, DisplaySlot.SIDEBAR);
        }
        activeScorers.remove(name);
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