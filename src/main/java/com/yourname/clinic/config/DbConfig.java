package com.yourname.clinic.config;

public final class DbConfig {
    private DbConfig() {}

    // 本地数据文件（运行目录下 data/clinic.db）
    public static final String JDBC_URL = "jdbc:sqlite:data/clinic.db";
}