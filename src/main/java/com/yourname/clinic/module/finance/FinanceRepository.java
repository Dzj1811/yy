package com.yourname.clinic.module.finance;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class FinanceRepository {

    public long findDefaultIncomeCategoryId(Connection conn) throws SQLException {
        String sql = """
            SELECT id FROM finance_category
            WHERE category_type = 'INCOME' AND category_name = '门诊诊疗'
            LIMIT 1
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getLong("id");
        }
        throw new SQLException("未找到默认收入分类：门诊诊疗");
    }

    public void insertFinanceTxn(
            Connection conn,
            String txnNo,
            String txnType,     // INCOME
            long categoryId,
            double amount,
            Long relatedVisitId,
            String paymentMethod,
            String counterparty,
            String remark
    ) throws SQLException {
        String sql = """
            INSERT INTO finance_txn
            (txn_no, txn_type, category_id, amount, txn_date, related_visit_id, payment_method, counterparty, remark, created_at)
            VALUES (?, ?, ?, ?, datetime('now'), ?, ?, ?, ?, datetime('now'))
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, txnNo);
            ps.setString(2, txnType);
            ps.setLong(3, categoryId);
            ps.setDouble(4, amount);
            if (relatedVisitId == null) ps.setNull(5, java.sql.Types.BIGINT); else ps.setLong(5, relatedVisitId);
            ps.setString(6, paymentMethod);
            ps.setString(7, counterparty);
            ps.setString(8, remark);
            ps.executeUpdate();
        }
    }
}