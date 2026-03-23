package com.yourname.clinic.db;

import java.sql.Connection;
import java.sql.Statement;

public final class DbInitializer {
    private DbInitializer() {}

    public static void init() {
        try (Connection c = ConnectionManager.getConnection();
             Statement s = c.createStatement()) {
            s.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS patients (
                      id INTEGER PRIMARY KEY AUTOINCREMENT,
                      name TEXT NOT NULL,
                      gender TEXT,
                      birth_date TEXT,
                      phone TEXT,
                      remark TEXT
                    )
                    """);

            s.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS medicines (
                      id INTEGER PRIMARY KEY AUTOINCREMENT,
                      name TEXT NOT NULL,
                      spec TEXT,
                      unit TEXT,
                      stock REAL NOT NULL DEFAULT 0,
                      expire_date TEXT,
                      remark TEXT
                    )
                    """);

            s.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS visits (
                      id INTEGER PRIMARY KEY AUTOINCREMENT,
                      patient_name TEXT NOT NULL,
                      visit_date TEXT NOT NULL,
                      complaint TEXT,
                      diagnosis TEXT,
                      rx_count INTEGER NOT NULL,
                      total_amount REAL NOT NULL,
                      prescription_detail TEXT
                    )
                    """);

            // 兼容已有数据库：补充新列
            try {
                s.executeUpdate("ALTER TABLE visits ADD COLUMN prescription_detail TEXT");
            } catch (Exception ignored) {
                // 已存在则忽略
            }

            s.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS finance (
                      id INTEGER PRIMARY KEY AUTOINCREMENT,
                      txn_no TEXT NOT NULL,
                      type TEXT NOT NULL,
                      category TEXT,
                      amount REAL NOT NULL,
                      txn_date TEXT NOT NULL,
                      pay_method TEXT,
                      counterparty TEXT,
                      remark TEXT
                    )
                    """);


        } catch (Exception e) {
            throw new RuntimeException("初始化数据库失败", e);
        }
    }
}
