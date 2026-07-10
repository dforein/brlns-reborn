package org.brlnsreb.core.player.data.database;

import org.brlnsreb.BrlnsReb;
import org.brlnsreb.core.ConfigManager;
import org.brlnsreb.utils.database.DBResults;
import org.brlnsreb.utils.database.SQLConsumer;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import org.powernukkitx.utils.Config;
import org.powernukkitx.utils.TextFormat;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;

public class DatabaseManager {
    
    private static HikariDataSource dataSource;
    private static boolean enabled;
    private final BrlnsReb plugin;

    private String accountsTable = """
        CREATE TABLE IF NOT EXISTS accounts (
            name VARCHAR(26) PRIMARY KEY,
            password_hash VARCHAR(60) NOT NULL,
            exp INT DEFAULT 0,
            coins INT DEFAULT 0,
            friend_alerts BOOLEAN DEFAULT TRUE,
            friend_notify BOOLEAN DEFAULT TRUE
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
    """;

    private String playersTable = """
        CREATE TABLE IF NOT EXISTS players (
            uuid VARCHAR(36) PRIMARY KEY,
            name VARCHAR(26) NOT NULL,
            last_login TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            FOREIGN KEY (name) REFERENCES accounts(name) ON DELETE CASCADE
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
    """;

    private String statsTable = """
        CREATE TABLE IF NOT EXISTS stats (
            player_name VARCHAR(26) NOT NULL,
            minigame_id TINYINT UNSIGNED NOT NULL,
            stat_type TINYINT UNSIGNED NOT NULL,
            value INT NOT NULL,
            PRIMARY KEY (player_name, minigame_id, stat_type),
            FOREIGN KEY (player_name) REFERENCES accounts(name) ON DELETE CASCADE
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
    """;

    //yes, i'll use double lines to save friends' associations, cus they are easier to retrieve and elaborate
    private String friendsTable = """
        CREATE TABLE IF NOT EXISTS friends (
            player_name VARCHAR(26) NOT NULL,
            friend_name VARCHAR(26) NOT NULL,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            PRIMARY KEY (player_name, friend_name),
            FOREIGN KEY (player_name) REFERENCES accounts(name) ON DELETE CASCADE,
            FOREIGN KEY (friend_name) REFERENCES accounts(name) ON DELETE CASCADE
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
    """;

    private String friendRequestsTable = """
        CREATE TABLE IF NOT EXISTS friend_requests (
            sender_name VARCHAR(26) NOT NULL,
            receiver_name VARCHAR(26) NOT NULL,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            PRIMARY KEY (sender_name, receiver_name),
            FOREIGN KEY (sender_name) REFERENCES accounts(name) ON DELETE CASCADE,
            FOREIGN KEY (receiver_name) REFERENCES accounts(name) ON DELETE CASCADE,
            INDEX idx_receiver (receiver_name)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
    """;


    public DatabaseManager(BrlnsReb plugin) {
        this.plugin = plugin;
        enabled = initConnectionPool();
    }

    public boolean retryInit() {
        enabled = initConnectionPool();
        return enabled;
    }


    private boolean initConnectionPool() {
        Config config = ConfigManager.getConfig("global/database.yml");

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
        try (Connection conn = getConnection();
             var stmt = conn.createStatement()) {
            
            //accounts
            stmt.execute(accountsTable);
            stmt.execute(playersTable);

            //stats
            stmt.execute(statsTable);

            //friends
            stmt.execute(friendsTable);
            stmt.execute(friendRequestsTable);


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

    public static DBResults executeSelect(Connection conn, String sql, Object... params) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }

            ResultSet rs = stmt.executeQuery();
            ResultSetMetaData meta = rs.getMetaData();
            int columnCount = meta.getColumnCount();

            List<Map<String, Object>> results = new ArrayList<>();

            while (rs.next()) {
                Map<String, Object> row = new HashMap<>(columnCount, 1.0f);
                for (int i = 1; i <= columnCount; i++) {
                    row.put(meta.getColumnName(i), rs.getObject(i));
                }
                results.add(row);
            }
            
            return new DBResults(results);
        }
    }

    public static int executeUpdate(Connection conn, String sql, Object... params) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }

            return stmt.executeUpdate();
        }
    }

    public static DBResults executeSelect(String sql, Object... params) throws SQLException {
        try (Connection conn = getConnection()) {
            return executeSelect(conn, sql, params);
        }
    }

    public static int executeUpdate(String sql, Object... params) throws SQLException {
        try (Connection conn = getConnection()) {
            return executeUpdate(conn, sql, params);
        }
    }

    public static void executeTransaction(SQLConsumer<Connection> work) throws SQLException {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                work.accept(conn);
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
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