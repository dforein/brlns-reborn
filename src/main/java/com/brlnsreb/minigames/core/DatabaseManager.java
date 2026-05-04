package com.brlnsreb.minigames.core;

import com.brlnsreb.minigames.MinigameCore;
import com.brlnsreb.minigames.core.minigame.Minigames;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import cn.nukkit.utils.Config;
import cn.nukkit.utils.TextFormat;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseManager {
    
    private static HikariDataSource dataSource;
    private final MinigameCore plugin;
    private boolean enabled;

    public DatabaseManager(MinigameCore plugin) {
        this.plugin = plugin;
        this.enabled = initConnectionPool();
    }

    public boolean retryInit() {
        this.enabled = initConnectionPool();
        return this.enabled;
    }

    private boolean initConnectionPool() {
        Config config = new Config(new File(plugin.getDataFolder() + "database.yml"), Config.YAML);

        if (config.getBoolean("enabled")) {
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
                player_name VARCHAR(32) NOT NULL,
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

        String[] columnsPlayers = {
            "player_name VARCHAR(32) NOT NULL",
            "exp INT DEFAULT 0",
            "coins INT DEFAULT 0",
        };

        String[] columnsStats = {};
        String pvpKillsColumn = "kills INT DEFAULT 0";
        
        try (Connection conn = getConnection();
             var stmt = conn.createStatement()) {
            
            stmt.execute(playersTable);
            
            for (String column : columnsPlayers) {
                stmt.execute(alterStmtCommand.formatted("players") + column);
            }

            for (Minigames minigame : Minigames.values()) {
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
        if (dataSource == null || dataSource.isClosed()) {
            throw new SQLException("Database not initialized or already closed!");
        }
        return dataSource.getConnection();
    }
    
    public boolean isEnabled() {
        return enabled && dataSource != null && !dataSource.isClosed();
    }
    
    public void closePool() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            plugin.getLogger().info("Database connection pool closed.");
        }
    }

}
