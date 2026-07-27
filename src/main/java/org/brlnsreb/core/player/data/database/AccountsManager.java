package org.brlnsreb.core.player.data.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.brlnsreb.BrlnsReb;
import org.brlnsreb.core.player.CustomPlayer;
import org.brlnsreb.core.player.PlayerUtils;
import org.brlnsreb.core.player.data.PlayerData;
import org.brlnsreb.core.player.data.StatType;
import org.brlnsreb.utils.database.DBResults;
import org.mindrot.jbcrypt.BCrypt;

import org.powernukkitx.scheduler.ServerScheduler;

public class AccountsManager {

    private static ServerScheduler scheduler;

    public static void init() {
        scheduler = BrlnsReb.getScheduler();
    }

    public static void loadAccountDataSync(CustomPlayer player, String name) throws SQLException {
        DBResults dataResults = DatabaseManager.executeSelect(
            """
            SELECT a.coins, a.exp, a.friend_alerts, a.friend_notify,
                s.minigame_id, s.stat_type, s.value
            FROM accounts a
            LEFT JOIN stats s ON a.name = s.player_name
            WHERE a.name = ?
            """,
            name
        );

        setAccountData(player, name, dataResults);
    }

    public static Outcome checkAndLoadAccountDataSync(CustomPlayer player, String name, String password) throws SQLException {
        DBResults dataResults = DatabaseManager.executeSelect(
            """
            SELECT a.password_hash, a.coins, a.exp, a.friend_alerts, a.friend_notify,
                s.minigame_id, s.stat_type, s.value
            FROM accounts a
            LEFT JOIN stats s ON a.name = s.player_name
            WHERE a.name = ?
            """,
            name
        );

        if (dataResults.isEmpty()) return Outcome.NAME_NOT_FOUND;
        if (!BCrypt.checkpw(password, dataResults.getString("password_hash"))) {
            return Outcome.WRONG_PASSWORD;
        }

        setAccountData(player, name, dataResults);

        return Outcome.OK;
    }

    public static Outcome createNewAccount(CustomPlayer player, String name, String password) throws SQLException {
        //check name: alphanumeric + underscore, min and max lenght
        if (!name.matches("^[A-Za-z0-9_]{3,26}$")) return Outcome.INVALID_NAME;
        
        //check whether the name is already in use
        DBResults accountsResults = DatabaseManager.executeSelect(
            "SELECT * FROM accounts WHERE name = ?", 
            name
        );
        if (!accountsResults.isEmpty()) return Outcome.NAME_ALREADY_IN_USE;
        
        //start to create the account
        String pwHash = BCrypt.hashpw(password, BCrypt.gensalt(12));

        DatabaseManager.executeUpdate(
            """
            INSERT INTO accounts (name, password_hash, exp, coins)
            VALUES (?, ?, ?, ?)
            """,
            name, pwHash, 0, 0        
        );

        DatabaseManager.executeUpdate(
            """
            INSERT INTO players (uuid, name)
            VALUES (?, ?)
            """,
            player.getUniqueId().toString(), name
        );

        scheduler.scheduleTask(() -> {
            PlayerData data = player.data;

            data.name = name;
            data.setCoins(0);
            data.setExp(0);
            data.updateLevel();

            updatePlayer(player, name);
        });

        return Outcome.OK;
    }

    private static void setAccountData(CustomPlayer player, String name, DBResults dataResults) {
        scheduler.scheduleTask(() -> {
            if (!player.isOnline()) return;
            PlayerData data = player.data;

            data.name = name;
            data.setCoins(dataResults.getInt("coins"));
            data.setExp(dataResults.getInt("exp"));
            data.updateLevel();
            data.setFriendAlerts(dataResults.getBoolean("friend_alerts"));
            data.setFriendNotify(dataResults.getBoolean("friend_notify"));

            for (int i = 0; i < dataResults.results.size(); i++) {
                if (dataResults.results.get(i).get("minigame_id") == null) continue;

                data.setStat(
                    dataResults.getInt(i, "minigame_id"),
                    dataResults.getInt(i, "stat_type"),
                    dataResults.getInt(i, "value")
                );
            }

            if (player.spawned) updatePlayer(player, name);

            PlayerDataManager.onLogin(name, player.getUniqueId());
        });
    }

    public static Outcome playerLogoutSync(CustomPlayer player) {
        PlayerData data = player.data;

        Outcome outcome = savePlayerDataSync(data);
        if (outcome != Outcome.OK) return outcome;

        FriendsManager.removeOnlineFriend(data);
        data.resetData();

        scheduler.scheduleTask(BrlnsReb.instance, () -> updatePlayer(player, player.getName()));

        return outcome;
    }

    public static CompletableFuture<Outcome> savePlayerData(CustomPlayer player) {
        if (player.isOnline() && !player.canRunAsync()) {
            scheduler.scheduleDelayedTask(() -> savePlayerData(player), 20);
            return CompletableFuture.completedFuture(
                Outcome.ASYNC_TASK_ALREADY_RUNNING
            );
        }

        return CompletableFuture.supplyAsync(() -> {
            Outcome outcome = savePlayerDataSync(player.data);
            player.resetAsync();
            return outcome;
        });
    }

    public static Outcome savePlayerDataSync(PlayerData data) {
        Outcome outcome;

        outcome = saveExpCoinsSync(data);
        if (outcome != Outcome.OK) return outcome;
        outcome = saveStatsSync(data);
        return outcome;
    }

    public static CompletableFuture<Outcome> saveExpCoins(CustomPlayer player) {
        if (!player.canRunAsync()) {
            scheduler.scheduleDelayedTask(() -> savePlayerData(player), 20);
            return CompletableFuture.completedFuture(
                Outcome.ASYNC_TASK_ALREADY_RUNNING
            );
        }

        return CompletableFuture.supplyAsync(() -> {
            Outcome outcome = saveExpCoinsSync(player.data);
            player.resetAsync();
            return outcome;
        });
    }

    public static Outcome saveExpCoinsSync(PlayerData data) {
        try {
            if (!data.isLogged()) return Outcome.PLAYER_ALREADY_LOGGED_OUT;

            DatabaseManager.executeUpdate(
                """
                UPDATE accounts
                SET exp = ?, coins = ?
                WHERE name = ?
                """,
                data.getExp(), data.getCoins(), 
                data.name
            );

            return Outcome.OK;
        } catch (SQLException e) {
            return PlayerDataManager.onDBError(e);
        }
    }

    public static CompletableFuture<Outcome> saveStats(PlayerData data) {
        return CompletableFuture.supplyAsync(() -> {
            return saveStatsSync(data);
        });
    }

    public static Outcome saveStatsSync(PlayerData data) {
        try {
            if (!data.isLogged()) return Outcome.PLAYER_ALREADY_LOGGED_OUT;

            int totalRows = data.getStatsAmount();
            if (totalRows == 0) return Outcome.OK;

            StringBuilder sql = new StringBuilder("INSERT INTO stats (player_name, minigame_id, stat_type, value) VALUES ");
            for (int i = 0; i < totalRows; i++) {
                sql.append("(?, ?, ?, ?)");
                if (i < totalRows - 1) {
                    sql.append(", ");
                }
            }
            sql.append(" ON DUPLICATE KEY UPDATE value = VALUES(value);");

            try (Connection conn = DatabaseManager.getConnection();
                    PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            
                int i = 1, statTypeId;

                for (Map.Entry<Integer, int[]> entry : data.getStats().entrySet()) {
                    int minigameId = entry.getKey();
                    int[] values = entry.getValue();

                    for (statTypeId = 0; statTypeId < StatType.size; statTypeId++) {
                        int value = values[statTypeId];
                        if (value == -1) continue;

                        stmt.setString(i++, data.name);
                        stmt.setInt(i++, minigameId);
                        stmt.setInt(i++, statTypeId);
                        stmt.setInt(i++, value);
                    }
                }

                if (stmt.executeUpdate() < 1) return Outcome.DB_ERROR;
            }

            return Outcome.OK;
        } catch (SQLException e) {
            return PlayerDataManager.onDBError(e);
        }
    }

    private static void updatePlayer(CustomPlayer player, String name) {
        player.updatePresetNameTags();
        player.updateExp();
        player.setDisplayName(name);
        PlayerUtils.updateOnlinePlayer(player, false);
    }

}