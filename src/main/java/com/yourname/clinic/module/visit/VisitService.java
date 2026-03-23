package com.yourname.clinic.module.visit;

import com.yourname.clinic.db.ConnectionManager;
import com.yourname.clinic.module.finance.FinanceRepository;
import com.yourname.clinic.module.inventory.InventoryRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class VisitService {
    private final VisitRepository visitRepository = new VisitRepository();
    private final InventoryRepository inventoryRepository = new InventoryRepository();
    private final FinanceRepository financeRepository = new FinanceRepository();

    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String OPERATOR = "owner";

    public long createVisitWithPrescriptionAndIncome(
            long patientId,
            String complaint,
            String diagnosis,
            String treatmentPlan,
            String advice,
            String remark,
            List<PrescriptionItemCmd> items,
            String paymentMethod
    ) {
        if (patientId <= 0) throw new IllegalArgumentException("patientId 非法");
        if (items == null || items.isEmpty()) throw new IllegalArgumentException("处方项不能为空");

        LocalDateTime now = LocalDateTime.now();
        String nowStr = now.format(DT);

        // 1) 构造就诊对象
        VisitRecord visit = new VisitRecord();
        visit.setVisitNo(genNo("V", now));
        visit.setPatientId(patientId);
        visit.setVisitDate(nowStr);
        visit.setComplaint(complaint);
        visit.setDiagnosis(diagnosis);
        visit.setTreatmentPlan(treatmentPlan);
        visit.setAdvice(advice);
        visit.setRemark(remark);
        visit.setCreatedAt(nowStr);
        visit.setUpdatedAt(nowStr);

        try (Connection conn = ConnectionManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 2) 插入就诊主表
                long visitId = visitRepository.insertVisit(conn, visit);

                // 3) 处理处方 + 扣库存 + 库存流水
                BigDecimal totalAmount = BigDecimal.ZERO;

                for (PrescriptionItemCmd item : items) {
                    validateItem(item);

                    BigDecimal amount = item.getQuantity()
                            .multiply(item.getUnitPrice())
                            .setScale(2, RoundingMode.HALF_UP);
                    totalAmount = totalAmount.add(amount);

                    // 3.1 保存处方项
                    visitRepository.insertPrescriptionItem(
                            conn,
                            visitId,
                            item.getMedicineId(),
                            item.getQuantity(),
                            item.getUnit(),
                            item.getUnitPrice(),
                            amount,
                            item.getUsageNote(),
                            nowStr
                    );

                    // 3.2 库存校验 + 扣减
                    double currentStock = inventoryRepository.getStockQtyForUpdate(conn, item.getMedicineId());
                    double outQty = item.getQuantity().doubleValue();
                    if (currentStock < outQty) {
                        throw new IllegalStateException("库存不足, medicineId=" + item.getMedicineId()
                                + ", current=" + currentStock + ", need=" + outQty);
                    }

                    inventoryRepository.decreaseStock(conn, item.getMedicineId(), outQty);

                    // 3.3 库存流水
                    inventoryRepository.insertInventoryTxn(
                            conn,
                            genNo("I", now),
                            item.getMedicineId(),
                            "OUT",
                            outQty,
                            item.getUnitPrice().doubleValue(),
                            amount.doubleValue(),
                            visitId,
                            OPERATOR,
                            "就诊开药自动出库"
                    );
                }

                // 4) 收入流水（默认门诊诊疗）
                long categoryId = financeRepository.findDefaultIncomeCategoryId(conn);
                financeRepository.insertFinanceTxn(
                        conn,
                        genNo("F", now),
                        "INCOME",
                        categoryId,
                        totalAmount.doubleValue(),
                        visitId,
                        paymentMethod == null ? "现金" : paymentMethod,
                        null,
                        "就诊自动记账"
                );

                conn.commit();
                return visitId;
            } catch (Exception e) {
                conn.rollback();
                throw new RuntimeException("创建就诊失败(已回滚): " + e.getMessage(), e);
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (Exception e) {
            throw new RuntimeException("数据库操作失败", e);
        }
    }

    private void validateItem(PrescriptionItemCmd item) {
        if (item.getMedicineId() <= 0) {
            throw new IllegalArgumentException("medicineId 非法");
        }
        if (item.getQuantity() == null || item.getQuantity().signum() <= 0) {
            throw new IllegalArgumentException("quantity 必须 > 0");
        }
        if (item.getUnitPrice() == null || item.getUnitPrice().signum() < 0) {
            throw new IllegalArgumentException("unitPrice 必须 >= 0");
        }
    }

    private String genNo(String prefix, LocalDateTime now) {
        // 简化：前缀 + yyyyMMddHHmmssSSS
        return prefix + now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
    }
}