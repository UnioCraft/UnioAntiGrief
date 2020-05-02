package com.uniocraft.antigrief;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ConnectionPoolManager {

    private final Main plugin;

    private HikariDataSource griefdataSource;

    private String hostname;
    private String port;
    private String database;
    private String username;
    private String password;

    public ConnectionPoolManager(Main plugin) {
        this.plugin = plugin;
        init();
        setupPool();
    }

    private void init() {
        hostname = plugin.getConfig().getString("database.ip");
        port = plugin.getConfig().getString("database.port");
        database = plugin.getConfig().getString("database.dbname");
        username = plugin.getConfig().getString("database.user");
        password = plugin.getConfig().getString("database.pass");
    }

    private void setupPool() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(
                "jdbc:mysql://" +
                        hostname +
                        ":" +
                        port +
                        "/" +
                        database + "?autoReconnect=true"
        );
        config.setDriverClassName("com.mysql.jdbc.Driver");
        config.setUsername(username);
        config.setPassword(password);
        config.setMinimumIdle(5);
        config.setMaximumPoolSize(5);
        config.setConnectionTimeout(15000);
        config.setLeakDetectionThreshold(10000);
        config.setPoolName("UnioGriefPool");
        griefdataSource = new HikariDataSource(config);
    }


    public void close(Connection conn, PreparedStatement ps, ResultSet res) {
        if (conn != null) try {
            conn.close();
        } catch (SQLException ignored) {
        }
        if (ps != null) try {
            ps.close();
        } catch (SQLException ignored) {
        }
        if (res != null) try {
            res.close();
        } catch (SQLException ignored) {
        }
    }

    public Connection getConnection() throws SQLException {
        return griefdataSource.getConnection();
    }

    public void closePool() {
        if (griefdataSource != null && !griefdataSource.isClosed()) {
            griefdataSource.close();
        }
    }
}
