package com.yourname.clinic.module.finance;

import com.yourname.clinic.db.ConnectionManager;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FinanceManageRepository {

    public List<CategoryItem> listCategories(String categoryType) {
        String sql = """
            SELECT id, category_type, category_name
            FROM finance_category
            WHERE category_type = ?
            ORDER BY is_default DESC, id ASC
            """;
        List<CategoryItem> list = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, categoryType);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new CategoryItem(
                            rs.getLong("id"),
                            rs.getString("category_type"),
                            rs.getString("category_name")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("listCategories failed", e);
        }
        return list;
    }

    public void addTxn(String txnNo, String txnType, long categoryId, BigDecimal amount,
                       String paymentMethod, String counterparty, String remark, Timestamp txnDate) {
        String sql = """
            INSERT INTO finance_txn
            (txn_no, txn_type, category_id, amount, txn_date, related_visit_id, payment_method, counterparty, remark, created_at)
            VALUES (?, ?, ?, ?, ?, NULL, ?, ?, ?, datetime('now'))
            """;
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, txnNo);
            ps.setString(2, txnType);
            ps.setLong(3, categoryId);
            ps.setBigDecimal(4, amount);
            ps.setString(5, txnDate.toString()); // SQLite 可存文本
            ps.setString(6, paymentMethod);
            ps.setString(7, counterparty);
            ps.setString(8, remark);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("addTxn failed", e);
        }
    }

    public List<FinanceRowDTO> queryTxn(String type, String fromDate, String toDate) {
        StringBuilder sql = new StringBuilder("""
            SELECT t.id, t.txn_no, t.txn_type, c.category_name, t.amount, t.txn_date,
                   t.payment_method, t.counterparty, t.remark
            FROM finance_txn t
            JOIN finance_category c ON t.category_id = c.id
            WHERE 1=1
            """);
        List<Object> params = new ArrayList<>();

        if (type != null && !type.isBlank() && !"ALL".equals(type)) {
            sql.append(" AND t.txn_type = ? ");
            params.add(type);
        }
        if (fromDate != null && !fromDate.isBlank()) {
            sql.append(" AND date(t.txn_date) >= date(?) ");
            params.add(fromDate);
        }
        if (toDate != null && !toDate.isBlank()) {
            sql.append(" AND date(t.txn_date) <= date(?) ");
            params.add(toDate);
        }
        sql.append(" ORDER BY t.id DESC ");

        List<FinanceRowDTO> list = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new FinanceRowDTO(
                            rs.getLong("id"),
                            rs.getString("txn_no"),
                            rs.getString("txn_type"),
                            rs.getString("category_name"),
                            rs.getBigDecimal("amount"),
                            rs.getString("txn_date"),
                            rs.getString("payment_method"),
                            rs.getString("counterparty"),
                            rs.getString("remark")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("queryTxn failed", e);
        }
        return list;
    }

    // DTO
    public record CategoryItem(Long id, String type, String name) {}
    public record FinanceRowDTO(
            Long id, String txnNo, String txnType, String categoryName, BigDecimal amount,
            String txnDate, String paymentMethod, String counterparty, String remark
    ) {}
}