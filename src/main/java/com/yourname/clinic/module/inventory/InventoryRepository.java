package com.yourname.clinic.module.inventory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class InventoryRepository {

    public double getStockQtyForUpdate(Connection conn, long medicineId) throws SQLException {
        String sql = "SELECT stock_qty FROM medicine WHERE id = ? AND is_deleted = 0";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, medicineId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("药品不存在, medicineId=" + medicineId);
                }
                return rs.getDouble("stock_qty");
            }
        }
    }

    public void decreaseStock(Connection conn, long medicineId, double qty) throws SQLException {
        String sql = "UPDATE medicine SET stock_qty = stock_qty - ?, updated_at = datetime('now') WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, qty);
            ps.setLong(2, medicineId);
            ps.executeUpdate();
        }
    }

    public void insertInventoryTxn(
            Connection conn,
            String txnNo,
            long medicineId,
            String txnType,      // OUT
            double quantity,
            Double unitPrice,
            Double amount,
            Long relatedVisitId,
            String operator,
            String remark
    ) throws SQLException {
        String sql = """
            INSERT INTO inventory_txn
            (txn_no, medicine_id, txn_type, quantity, unit_price, amount, related_visit_id, txn_time, operator, remark, created_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, datetime('now'), ?, ?, datetime('now'))
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, txnNo);
            ps.setLong(2, medicineId);
            ps.setString(3, txnType);
            ps.setDouble(4, quantity);
            if (unitPrice == null) ps.setNull(5, java.sql.Types.REAL); else ps.setDouble(5, unitPrice);
            if (amount == null) ps.setNull(6, java.sql.Types.REAL); else ps.setDouble(6, amount);
            if (relatedVisitId == null) ps.setNull(7, java.sql.Types.BIGINT); else ps.setLong(7, relatedVisitId);
            ps.setString(8, operator);
            ps.setString(9, remark);
            ps.executeUpdate();
        }
    }
}