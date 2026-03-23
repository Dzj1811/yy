package com.yourname.clinic.module.patient;

import com.yourname.clinic.db.ConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PatientRepository {

    public long insert(Patient p) {
        String sql = """
            INSERT INTO patient (
              patient_code, name, gender, birth_date, phone, remark, created_at, updated_at, is_deleted
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, 0)
            """;
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getPatientCode());
            ps.setString(2, p.getName());
            ps.setString(3, p.getGender());
            ps.setString(4, p.getBirthDate());
            ps.setString(5, p.getPhone());
            ps.setString(6, p.getRemark());
            ps.setString(7, p.getCreatedAt());
            ps.setString(8, p.getUpdatedAt());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getLong(1);
            }
            throw new SQLException("No generated key returned.");
        } catch (SQLException e) {
            throw new RuntimeException("Insert patient failed", e);
        }
    }

    public List<Patient> search(String keyword) {
        String sql = """
            SELECT id, patient_code, name, gender, birth_date, phone, remark, created_at, updated_at
            FROM patient
            WHERE is_deleted = 0
              AND (name LIKE ? OR phone LIKE ?)
            ORDER BY id DESC
            """;
        List<Patient> list = new ArrayList<>();
        String k = "%" + (keyword == null ? "" : keyword.trim()) + "%";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, k);
            ps.setString(2, k);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Patient p = new Patient();
                    p.setId(rs.getLong("id"));
                    p.setPatientCode(rs.getString("patient_code"));
                    p.setName(rs.getString("name"));
                    p.setGender(rs.getString("gender"));
                    p.setBirthDate(rs.getString("birth_date"));
                    p.setPhone(rs.getString("phone"));
                    p.setRemark(rs.getString("remark"));
                    p.setCreatedAt(rs.getString("created_at"));
                    p.setUpdatedAt(rs.getString("updated_at"));
                    list.add(p);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Search patient failed", e);
        }
        return list;
    }

    public void softDelete(long id, String updatedAt) {
        String sql = "UPDATE patient SET is_deleted = 1, updated_at = ? WHERE id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, updatedAt);
            ps.setLong(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Soft delete patient failed", e);
        }
    }
}