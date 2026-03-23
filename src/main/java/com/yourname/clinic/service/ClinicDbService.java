package com.yourname.clinic.service;

import com.yourname.clinic.db.ConnectionManager;
import com.yourname.clinic.model.FinanceRow;
import com.yourname.clinic.model.OptionItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ClinicDbService {

    public List<OptionItem> listPatientOptions() {
        List<OptionItem> out = new ArrayList<>();
        String sql = "SELECT id,name FROM patients ORDER BY id DESC";
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                long id = rs.getLong("id");
                String name = rs.getString("name");
                out.add(new OptionItem(id, name + "(P" + id + ")"));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return out;
    }

    public List<OptionItem> listMedicineOptions() {
        List<OptionItem> out = new ArrayList<>();
        String sql = "SELECT id,name FROM medicines ORDER BY id DESC";
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                long id = rs.getLong("id");
                String name = rs.getString("name");
                out.add(new OptionItem(id, name + "(M" + id + ")"));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return out;
    }

    // Patient CRUD
    public List<String[]> listPatients() {
        List<String[]> out = new ArrayList<>();
        String sql = "SELECT id,name,gender,birth_date,phone,remark FROM patients ORDER BY id DESC";
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                out.add(new String[]{String.valueOf(rs.getLong("id")), nv(rs.getString("name")), nv(rs.getString("gender")),
                        nv(rs.getString("birth_date")), nv(rs.getString("phone")), nv(rs.getString("remark"))});
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return out;
    }

    public void insertPatient(String name, String gender, String birthDate, String phone, String remark) {
        String sql = "INSERT INTO patients(name,gender,birth_date,phone,remark) VALUES(?,?,?,?,?)";
        try (Connection c = ConnectionManager.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, name); ps.setString(2, gender); ps.setString(3, birthDate); ps.setString(4, phone); ps.setString(5, remark);
            ps.executeUpdate();
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    public void updatePatient(long id, String name, String gender, String birthDate, String phone, String remark) {
        String sql = "UPDATE patients SET name=?,gender=?,birth_date=?,phone=?,remark=? WHERE id=?";
        try (Connection c = ConnectionManager.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, name); ps.setString(2, gender); ps.setString(3, birthDate); ps.setString(4, phone); ps.setString(5, remark); ps.setLong(6, id);
            ps.executeUpdate();
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    public void deletePatient(long id) {
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM patients WHERE id=?")) {
            ps.setLong(1, id); ps.executeUpdate();
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    // Medicine CRUD
    public List<String[]> listMedicines() {
        List<String[]> out = new ArrayList<>();
        String sql = "SELECT id,name,spec,unit,stock,expire_date,remark FROM medicines ORDER BY id DESC";
        try (Connection c = ConnectionManager.getConnection(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                out.add(new String[]{String.valueOf(rs.getLong("id")), nv(rs.getString("name")), nv(rs.getString("spec")), nv(rs.getString("unit")),
                        String.valueOf(rs.getDouble("stock")), nv(rs.getString("expire_date")), nv(rs.getString("remark"))});
            }
        } catch (Exception e) { throw new RuntimeException(e); }
        return out;
    }

    public void insertMedicine(String name, String spec, String unit, String expireDate, String remark) {
        String sql = "INSERT INTO medicines(name,spec,unit,stock,expire_date,remark) VALUES(?,?,?,?,?,?)";
        try (Connection c = ConnectionManager.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, name); ps.setString(2, spec); ps.setString(3, unit); ps.setDouble(4, 0); ps.setString(5, expireDate); ps.setString(6, remark);
            ps.executeUpdate();
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    public void updateMedicine(long id, String name, String spec, String unit, String expireDate, String remark) {
        String sql = "UPDATE medicines SET name=?,spec=?,unit=?,expire_date=?,remark=? WHERE id=?";
        try (Connection c = ConnectionManager.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, name); ps.setString(2, spec); ps.setString(3, unit); ps.setString(4, expireDate); ps.setString(5, remark); ps.setLong(6, id);
            ps.executeUpdate();
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    public void adjustMedicineStock(long id, double delta) {
        String sql = "UPDATE medicines SET stock = stock + ? WHERE id=?";
        try (Connection c = ConnectionManager.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDouble(1, delta); ps.setLong(2, id); ps.executeUpdate();
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    // Visits
    public void insertVisit(String patientName, String visitDate, String complaint, String diagnosis,
                            int rxCount, double totalAmount, String prescriptionDetail) {
        String sql = "INSERT INTO visits(patient_name,visit_date,complaint,diagnosis,rx_count,total_amount,prescription_detail) VALUES(?,?,?,?,?,?,?)";
        try (Connection c = ConnectionManager.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, patientName); ps.setString(2, visitDate); ps.setString(3, complaint); ps.setString(4, diagnosis);
            ps.setInt(5, rxCount); ps.setDouble(6, totalAmount);
            ps.setString(7, prescriptionDetail);
            ps.executeUpdate();
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    public List<String[]> queryVisits(String patientLike, String dateLike, String diagnosisLike) {
        List<String[]> out = new ArrayList<>();
        String sql = "SELECT id,patient_name,visit_date,complaint,diagnosis,rx_count,total_amount,prescription_detail FROM visits " +
                "WHERE lower(patient_name) LIKE ? AND lower(visit_date) LIKE ? AND lower(diagnosis) LIKE ? ORDER BY id DESC";
        try (Connection c = ConnectionManager.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, "%" + safeLower(patientLike) + "%");
            ps.setString(2, "%" + safeLower(dateLike) + "%");
            ps.setString(3, "%" + safeLower(diagnosisLike) + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(new String[]{
                            String.valueOf(rs.getLong("id")),
                            nv(rs.getString("patient_name")),
                            nv(rs.getString("visit_date")),
                            nv(rs.getString("complaint")),
                            nv(rs.getString("diagnosis")),
                            String.valueOf(rs.getInt("rx_count")),
                            String.valueOf(rs.getDouble("total_amount")),
                            nv(rs.getString("prescription_detail"))
                    });
                }
            }
        } catch (Exception e) { throw new RuntimeException(e); }
        return out;
    }

    public double getMedicineStock(long medicineId) {
        String sql = "SELECT stock FROM medicines WHERE id=?";
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, medicineId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new RuntimeException("药品不存在: " + medicineId);
                return rs.getDouble("stock");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void consumeMedicineStock(Map<Long, Double> usageMap) {
        String querySql = "SELECT stock FROM medicines WHERE id=?";
        String updateSql = "UPDATE medicines SET stock = stock - ? WHERE id=?";
        try (Connection c = ConnectionManager.getConnection()) {
            c.setAutoCommit(false);
            try (PreparedStatement q = c.prepareStatement(querySql);
                 PreparedStatement u = c.prepareStatement(updateSql)) {
                for (Map.Entry<Long, Double> e : usageMap.entrySet()) {
                    q.setLong(1, e.getKey());
                    try (ResultSet rs = q.executeQuery()) {
                        if (!rs.next()) throw new RuntimeException("药品不存在: " + e.getKey());
                        double stock = rs.getDouble("stock");
                        if (stock < e.getValue()) {
                            throw new RuntimeException("库存不足: 药品ID=" + e.getKey() + "，当前库存=" + stock + "，需要=" + e.getValue());
                        }
                    }
                }
                for (Map.Entry<Long, Double> e : usageMap.entrySet()) {
                    u.setDouble(1, e.getValue());
                    u.setLong(2, e.getKey());
                    u.executeUpdate();
                }
                c.commit();
            } catch (Exception ex) {
                c.rollback();
                throw ex;
            } finally {
                c.setAutoCommit(true);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<FinanceRow> listFinanceRows() {
        List<FinanceRow> out = new ArrayList<>();
        String sql = "SELECT id,txn_no,type,category,amount,txn_date,pay_method,counterparty,remark FROM finance ORDER BY id DESC";
        try (Connection c = ConnectionManager.getConnection(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                out.add(new FinanceRow(
                        String.valueOf(rs.getLong("id")), rs.getString("txn_no"), rs.getString("type"), rs.getString("category"),
                        String.valueOf(rs.getDouble("amount")), rs.getString("txn_date"), rs.getString("pay_method"),
                        rs.getString("counterparty"), rs.getString("remark")
                ));
            }
        } catch (Exception e) { throw new RuntimeException(e); }
        return out;
    }

    public int countPatients() {
        String sql = "SELECT COUNT(*) AS c FROM patients";
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt("c") : 0;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<String[]> recentPatients(int limit) {
        List<String[]> out = new ArrayList<>();
        String sql = "SELECT id,name,phone,birth_date FROM patients ORDER BY id DESC LIMIT ?";
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(new String[]{
                            String.valueOf(rs.getLong("id")),
                            nv(rs.getString("name")),
                            nv(rs.getString("phone")),
                            nv(rs.getString("birth_date"))
                    });
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return out;
    }

    private String safeLower(String s) { return s == null ? "" : s.trim().toLowerCase(); }
    private String nv(String s) { return s == null ? "" : s; }
}
