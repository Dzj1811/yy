package com.yourname.clinic.module.visit;

import java.math.BigDecimal;
import java.sql.*;

public class VisitRepository {

    public long insertVisit(Connection conn, VisitRecord v) throws SQLException {
        String sql = """
            INSERT INTO visit_record
            (visit_no, patient_id, visit_date, complaint, diagnosis, treatment_plan, advice, remark, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, v.getVisitNo());
            ps.setLong(2, v.getPatientId());
            ps.setString(3, v.getVisitDate());
            ps.setString(4, v.getComplaint());
            ps.setString(5, v.getDiagnosis());
            ps.setString(6, v.getTreatmentPlan());
            ps.setString(7, v.getAdvice());
            ps.setString(8, v.getRemark());
            ps.setString(9, v.getCreatedAt());
            ps.setString(10, v.getUpdatedAt());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getLong(1);
            }
            throw new SQLException("No generated key for visit_record");
        }
    }

    public void insertPrescriptionItem(
            Connection conn,
            long visitId,
            long medicineId,
            BigDecimal quantity,
            String unit,
            BigDecimal unitPrice,
            BigDecimal amount,
            String usageNote,
            String createdAt
    ) throws SQLException {
        String sql = """
            INSERT INTO visit_prescription_item
            (visit_id, medicine_id, quantity, unit, unit_price, amount, usage_note, created_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, visitId);
            ps.setLong(2, medicineId);
            ps.setBigDecimal(3, quantity);
            ps.setString(4, unit);
            ps.setBigDecimal(5, unitPrice);
            ps.setBigDecimal(6, amount);
            ps.setString(7, usageNote);
            ps.setString(8, createdAt);
            ps.executeUpdate();
        }
    }
}