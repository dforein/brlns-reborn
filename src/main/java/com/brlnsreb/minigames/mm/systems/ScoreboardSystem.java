package com.brlnsreb.minigames.mm.systems;

import cn.nukkit.Player;
import cn.nukkit.network.protocol.RemoveObjectivePacket;
import cn.nukkit.network.protocol.SetDisplayObjectivePacket;
import cn.nukkit.network.protocol.SetScorePacket;
import cn.nukkit.scoreboard.data.DisplaySlot;
import cn.nukkit.scoreboard.data.SortOrder;
import cn.nukkit.scoreboard.data.ScorerType;
import cn.nukkit.utils.TextFormat;
import com.brlnsreb.minigames.mm.roles.MMRole;

import java.util.ArrayList;
import java.util.List;

// TODO: scoreboard astraction into Utils

public class ScoreboardSystem {
    
    private static final String OBJECTIVE_NAME = "mm_scoreboard";
    
    public void show(Player player, String timer, int innocents, boolean sheriffAlive, MMRole playerRole, boolean isPregame) {
        remove(player);
        
        SetDisplayObjectivePacket pk = new SetDisplayObjectivePacket();
        pk.displaySlot = DisplaySlot.SIDEBAR;
        pk.objectiveName = OBJECTIVE_NAME;
        pk.displayName = TextFormat.colorize("&l&aMurder&2Mystery");
        pk.criteriaName = "dummy";
        pk.sortOrder = SortOrder.DESCENDING;   //1
        player.dataPacket(pk);
        
        List<String> lines = new ArrayList<>();
        
        if (isPregame) {
            // pregame countdown
            lines.add(TextFormat.colorize("&a"));
            lines.add(TextFormat.colorize("  &l&dGame time:"));
            lines.add(TextFormat.colorize("   &a" + timer));
            lines.add("");
        } else {
            // in game
            lines.add(TextFormat.colorize("&a"));
            lines.add(TextFormat.colorize("  &l&aInnocents:"));
            lines.add(TextFormat.colorize("   &l&a" + innocents));
            lines.add(TextFormat.colorize("  &l&eSheriff:"));
            lines.add(TextFormat.colorize("   &a" + (sheriffAlive ? "alive" : "dead")));
            lines.add(TextFormat.colorize("  &l&dGame time:"));
            lines.add(TextFormat.colorize("   &a" + timer));
            lines.add(TextFormat.colorize("  &l&6Role:"));
            
            String roleText;
            switch (playerRole) {
                case MURDERER:
                    roleText = "   &cmurderer";
                    break;
                case SHERIFF:
                    roleText = "   &1sheriff";
                    break;
                case INNOCENT:
                    roleText = "   &ainnocent";
                    break;
                case SPECTATOR:
                    roleText = "   &7spectator";
                    break;
                default:
                    roleText = "   &cno role";
            }
            lines.add(TextFormat.colorize(roleText));
            lines.add("");
        }
        
        SetScorePacket scorePacket = new SetScorePacket();
        scorePacket.action = SetScorePacket.Action.SET;
        
        List<SetScorePacket.ScoreInfo> entries = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            SetScorePacket.ScoreInfo info = new SetScorePacket.ScoreInfo(
                i,                      // scoreboardId (long)
                OBJECTIVE_NAME,         // objectiveId
                lines.size() - i,       // score
                lines.get(i)            // name
            );
            info.type = ScorerType.FAKE;
            entries.add(info);
        }
        
        scorePacket.infos = entries;
        player.dataPacket(scorePacket);
    }
    
    public void remove(Player player) {
        RemoveObjectivePacket pk = new RemoveObjectivePacket();
        pk.objectiveName = OBJECTIVE_NAME;
        player.dataPacket(pk);
    }
}