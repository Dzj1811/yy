package com.yourname.clinic.db;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class ConnectionManager {
    private static final Path DB_PATH = Path.of(
            System.getProperty("user.home"),
            ".clinic-manager",
            "clinic.db"
    );
    private static final String URL = "jdbc:sqlite:" + DB_PATH;

    private ConnectionManager() {}

    public static Connection getConnection() throws SQLException {
        try {
            Files.createDirectories(DB_PATH.getParent());
        } catch (Exception e) {
            throw new SQLException("创建数据库目录失败: " + DB_PATH.getParent(), e);
        }
        return DriverManager.getConnection(URL);
    }

    public static Path getDbPath() {
        return DB_PATH;
    }
}
