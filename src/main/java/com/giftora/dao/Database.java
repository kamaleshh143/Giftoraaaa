package com.giftora.dao;

import com.giftora.config.AppConfig;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Manages the HikariCP connection pool over the H2 file-based database.
 */
public final class Database {

    private static final Logger log = LoggerFactory.getLogger(Database.class);
    private static volatile Database instance;

    private final HikariDataSource dataSource;

    private Database() {
        Path dir = AppConfig.getDataDirectory();
        try {
            java.nio.file.Files.createDirectories(dir);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to create database directory: " + dir, ex);
        }
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(AppConfig.getJdbcUrl());
        config.setUsername(AppConfig.getDbUser());
        config.setPassword(AppConfig.getDbPassword());
        config.setDriverClassName("org.h2.Driver");
        config.setMaximumPoolSize(AppConfig.getInt("GIFTORA_DB_POOL_SIZE", 5));
        config.setMinimumIdle(1);
        config.setPoolName("giftora-pool");
        config.setConnectionTimeout(AppConfig.getInt("GIFTORA_DB_TIMEOUT_MS", 10000));
        config.addDataSourceProperty("cachePrepStmts", "true");
        this.dataSource = new HikariDataSource(config);
        log.info("Database connection pool initialized");
    }

    public static synchronized Database getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Database();
        }
        return INSTANCE;
    }

    private static Database INSTANCE;

    public java.sql.Connection getConnection() throws java.sql.SQLException {
        return dataSource.getConnection();
    }

    public boolean isHealthy() {
        try (java.sql.Connection connection = getConnection();
             java.sql.Statement statement = connection.createStatement()) {
            statement.execute("SELECT 1");
            return true;
        } catch (java.sql.SQLException ex) {
            return false;
        }
    }

    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}
