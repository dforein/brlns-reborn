package com.brlnsreb.minigames.core;

import com.brlnsreb.minigames.MinigameCore;
import com.brlnsreb.minigames.core.minigame.MinigameType;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import cn.nukkit.utils.Config;
import cn.nukkit.utils.TextFormat;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;

public class DatabaseManager {
    
    private static HikariDataSource dataSource;
    private static boolean enabled;
    private final MinigameCore plugin;

    public DatabaseManager(MinigameCore plugin) {
        this.plugin = plugin;
        enabled = initConnectionPool();
    }

    public boolean retryInit() {
        enabled = initConnectionPool();
        return enabled;
    }

    private boolean initConnectionPool() {
        Config config = new Config(new File(plugin.getDataFolder() + "database.yml"), Config.YAML);

        if (!config.getBoolean("enabled", false)) {
            plugin.getLogger().warning(TextFormat.GOLD + "Database disabled by settings.");
            return false;
        }

        String host = config.getString("database.host");
        int port = config.getInt("database.port");
        String database = config.getString("database.database");
        String username = config.getString("database.username");
        String password = config.getString("database.password");
        int poolSize = config.getInt("database.pool-size");

        String jdbcUrl = String.format(
            "jdbc:mysql://%s:%d/%s?useSSL=false&characterEncoding=UTF-8&autoReconnect=true",
            host, port, database
        );

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(jdbcUrl);
        hikariConfig.setUsername(username);
        hikariConfig.setPassword(password);

        hikariConfig.setMaximumPoolSize(poolSize);
        hikariConfig.setMinimumIdle(2);
        hikariConfig.setConnectionTimeout(30000);
        hikariConfig.setIdleTimeout(600000);
        hikariConfig.setMaxLifetime(1800000);

        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        try {
            dataSource = new HikariDataSource(hikariConfig);
            
            try (Connection testConn = dataSource.getConnection()) {
                plugin.getLogger().info(TextFormat.GREEN + "Database connected successfully to " + database);
            }
            
            createTables();
            
            return true;
            
        } catch (Exception e) {
            plugin.getLogger().error(TextFormat.RED + "Failed to connect to database: " + e.getMessage());
            return false;
        }
    }

    private void createTables() {
        String playersTable = """
            CREATE TABLE IF NOT EXISTS players (
                uuid VARCHAR(36) PRIMARY KEY,
                player_name VARCHAR(32),
                exp INT DEFAULT 0,
                coins INT DEFAULT 0,
                INDEX idx_name (player_name)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
        """;

        String statsTable = """
            CREATE TABLE IF NOT EXISTS %s (
                player_uuid VARCHAR(36) PRIMARY KEY,
                wins INT DEFAULT 0,
                INDEX idx_win (wins),
                FOREIGN KEY (player_uuid) REFERENCES players(uuid) ON DELETE CASCADE
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
        """;

        String alterStmtCommand = "ALTER TABLE %s ADD COLUMN IF NOT EXISTS";

        String[] columnsPlayers = {};   //for updates, to add new columns
        String[] columnsStats = {};
        String pvpKillsColumn = "kills INT DEFAULT 0";
        
        try (Connection conn = getConnection();
             var stmt = conn.createStatement()) {
            
            stmt.execute(playersTable);
            
            for (String column : columnsPlayers) {
                stmt.execute(alterStmtCommand.formatted("players") + column);
            }

            for (MinigameType minigame : MinigameType.values()) {
                String mgNameTag = minigame.getNameTag();
                stmt.execute(statsTable.formatted(mgNameTag));

                for (String column : columnsStats) {
                    stmt.execute(alterStmtCommand.formatted(mgNameTag) + column);
                }

                if (minigame.isPvp()) {
                    stmt.execute(alterStmtCommand.formatted(mgNameTag) + pvpKillsColumn);
                }
            }

            plugin.getLogger().info(TextFormat.GRAY + "Database tables ready");
            
        } catch (SQLException e) {
            plugin.getLogger().error("Failed to create tables: " + e.getMessage());
        }
    }
    
    public static Connection getConnection() throws SQLException {
        if (!isEnabled()) {
            throw new SQLException("Database not initialized or already closed!");
        }
        return dataSource.getConnection();
    }

    public static int executeUpdate(String sql, Object... params) {
        try (Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            
            return stmt.executeUpdate();
            
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static List<Map<String, Object>> executeSelect(String sql, Object... params) {
        try (Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            
            ResultSet rs = stmt.executeQuery();
            ResultSetMetaData meta = rs.getMetaData();
            int columnCount = meta.getColumnCount();

            List<Map<String, Object>> results = new ArrayList<>();

            while (rs.next()) {
                HashMap<String, Object> row = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    row.put(meta.getColumnName(i), rs.getObject(i));
                }
                results.add(row);
            }

            return results;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public static boolean isEnabled() {
        return enabled && dataSource != null && !dataSource.isClosed();
    }
    
    public void closePool() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            plugin.getLogger().info("Database connection pool closed.");
        }
    }

}
