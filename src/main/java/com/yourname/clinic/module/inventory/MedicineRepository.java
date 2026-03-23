package com.yourname.clinic.module.inventory;

import com.yourname.clinic.db.ConnectionManager;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MedicineRepository {

    public long insert(Medicine m) {
        String sql = """
            INSERT INTO medicine
            (medicine_code, name, spec, unit, category, purchase_price, sale_price, stock_qty, min_stock_qty,
             supplier, expire_date, remark, created_at, updated_at, is_deleted)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0)
            """;
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, m.getMedicineCode());
            ps.setString(2, m.getName());
            ps.setString(3, m.getSpec());
            ps.setString(4, m.getUnit());
            ps.setString(5, m.getCategory());
            ps.setBigDecimal(6, m.getPurchasePrice());
            ps.setBigDecimal(7, m.getSalePrice());
            ps.setBigDecimal(8, m.getStockQty());
            ps.setBigDecimal(9, m.getMinStockQty());
            ps.setString(10, m.getSupplier());
            ps.setString(11, m.getExpireDate());
            ps.setString(12, m.getRemark());
            ps.setString(13, m.getCreatedAt());
            ps.setString(14, m.getUpdatedAt());

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getLong(1);
            }
            throw new SQLException("No generated key");
        } catch (SQLException e) {
            throw new RuntimeException("Insert medicine failed", e);
        }
    }

    public void update(Medicine m) {
        String sql = """
            UPDATE medicine
            SET name=?, spec=?, unit=?, category=?, purchase_price=?, sale_price=?, min_stock_qty=?,
                supplier=?, expire_date=?, remark=?, updated_at=?
            WHERE id=? AND is_deleted=0
            """;
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, m.getName());
            ps.setString(2, m.getSpec());
            ps.setString(3, m.getUnit());
            ps.setString(4, m.getCategory());
            ps.setBigDecimal(5, m.getPurchasePrice());
            ps.setBigDecimal(6, m.getSalePrice());
            ps.setBigDecimal(7, m.getMinStockQty());
            ps.setString(8, m.getSupplier());
            ps.setString(9, m.getExpireDate());
            ps.setString(10, m.getRemark());
            ps.setString(11, m.getUpdatedAt());
            ps.setLong(12, m.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Update medicine failed", e);
        }
    }

    public List<Medicine> search(String keyword) {
        String sql = """
            SELECT id, medicine_code, name, spec, unit, category, purchase_price, sale_price, stock_qty,
                   min_stock_qty, supplier, expire_date, remark, created_at, updated_at
            FROM medicine
            WHERE is_deleted=0 AND (name LIKE ? OR medicine_code LIKE ?)
            ORDER BY id DESC
            """;
        String k = "%" + (keyword == null ? "" : keyword.trim()) + "%";
        List<Medicine> list = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, k);
            ps.setString(2, k);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Medicine m = new Medicine();
                    m.setId(rs.getLong("id"));
                    m.setMedicineCode(rs.getString("medicine_code"));
                    m.setName(rs.getString("name"));
                    m.setSpec(rs.getString("spec"));
                    m.setUnit(rs.getString("unit"));
                    m.setCategory(rs.getString("category"));
                    m.setPurchasePrice(rs.getBigDecimal("purchase_price"));
                    m.setSalePrice(rs.getBigDecimal("sale_price"));
                    m.setStockQty(rs.getBigDecimal("stock_qty"));
                    m.setMinStockQty(rs.getBigDecimal("min_stock_qty"));
                    m.setSupplier(rs.getString("supplier"));
                    m.setExpireDate(rs.getString("expire_date"));
                    m.setRemark(rs.getString("remark"));
                    m.setCreatedAt(rs.getString("created_at"));
                    m.setUpdatedAt(rs.getString("updated_at"));
                    list.add(m);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Search medicine failed", e);
        }
        return list;
    }

    public void softDelete(long id, String updatedAt) {
        String sql = "UPDATE medicine SET is_deleted=1, updated_at=? WHERE id=?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, updatedAt);
            ps.setLong(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Delete medicine failed", e);
        }
    }

    /** 入库：加库存 + 库存流水（同一事务） */
    public void stockIn(long medicineId, BigDecimal qty, BigDecimal unitPrice, String operator, String remark, String txnNo) {
        String updateSql = """
            UPDATE medicine
            SET stock_qty = stock_qty + ?, purchase_price=?, updated_at=datetime('now')
            WHERE id=? AND is_deleted=0
            """;
        String insertTxnSql = """
            INSERT INTO inventory_txn
            (txn_no, medicine_id, txn_type, quantity, unit_price, amount, related_visit_id, txn_time, operator, remark, created_at)
            VALUES (?, ?, 'IN', ?, ?, ?, NULL, datetime('now'), ?, ?, datetime('now'))
            """;

        try (Connection conn = ConnectionManager.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps1 = conn.prepareStatement(updateSql);
                 PreparedStatement ps2 = conn.prepareStatement(insertTxnSql)) {

                ps1.setBigDecimal(1, qty);
                ps1.setBigDecimal(2, unitPrice);
                ps1.setLong(3, medicineId);
                ps1.executeUpdate();

                BigDecimal amount = qty.multiply(unitPrice);

                ps2.setString(1, txnNo);
                ps2.setLong(2, medicineId);
                ps2.setBigDecimal(3, qty);
                ps2.setBigDecimal(4, unitPrice);
                ps2.setBigDecimal(5, amount);
                ps2.setString(6, operator);
                ps2.setString(7, remark);
                ps2.executeUpdate();

                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw new RuntimeException("Stock in failed", e);
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("DB error", e);
        }
    }
}