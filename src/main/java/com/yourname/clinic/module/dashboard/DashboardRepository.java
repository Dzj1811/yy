package com.yourname.clinic.module.dashboard;

import com.yourname.clinic.db.ConnectionManager;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DashboardRepository {

    public BigDecimal todayIncome() {
        String sql = """
            SELECT COALESCE(SUM(amount), 0) AS v
            FROM finance_txn
            WHERE txn_type='INCOME' AND date(txn_date)=date('now')
            """;
        return queryDecimal(sql);
    }

    public BigDecimal monthIncome() {
        String sql = """
            SELECT COALESCE(SUM(amount), 0) AS v
            FROM finance_txn
            WHERE txn_type='INCOME'
              AND strftime('%Y-%m', txn_date)=strftime('%Y-%m','now')
            """;
        return queryDecimal(sql);
    }

    public BigDecimal monthExpense() {
        String sql = """
            SELECT COALESCE(SUM(amount), 0) AS v
            FROM finance_txn
            WHERE txn_type='EXPENSE'
              AND strftime('%Y-%m', txn_date)=strftime('%Y-%m','now')
            """;
        return queryDecimal(sql);
    }

    public int lowStockCount() {
        String sql = """
            SELECT COUNT(*) AS c
            FROM medicine
            WHERE is_deleted=0 AND stock_qty <= min_stock_qty
            """;
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt("c") : 0;
        } catch (SQLException e) {
            throw new RuntimeException("lowStockCount failed", e);
        }
    }

    private BigDecimal queryDecimal(String sql) {
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getBigDecimal("v");
            return BigDecimal.ZERO;
        } catch (SQLException e) {
            throw new RuntimeException("queryDecimal failed", e);
        }
    }
}