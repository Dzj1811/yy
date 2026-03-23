package com.yourname.clinic.module.inventory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MedicineService {
    private final MedicineRepository repository = new MedicineRepository();
    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public long create(String name, String spec, String unit, String category,
                       BigDecimal purchasePrice, BigDecimal salePrice, BigDecimal minStockQty,
                       String supplier, String expireDate, String remark) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("药品名不能为空");
        if (unit == null || unit.isBlank()) throw new IllegalArgumentException("单位不能为空");

        String now = LocalDateTime.now().format(DT);

        Medicine m = new Medicine();
        m.setMedicineCode(genCode());
        m.setName(name.trim());
        m.setSpec(spec);
        m.setUnit(unit.trim());
        m.setCategory(category);
        m.setPurchasePrice(defaultZero(purchasePrice));
        m.setSalePrice(defaultZero(salePrice));
        m.setStockQty(BigDecimal.ZERO);
        m.setMinStockQty(defaultZero(minStockQty));
        m.setSupplier(supplier);
        m.setExpireDate(expireDate);
        m.setRemark(remark);
        m.setCreatedAt(now);
        m.setUpdatedAt(now);

        return repository.insert(m);
    }

    public List<Medicine> search(String keyword) {
        return repository.search(keyword);
    }

    public void delete(long id) {
        repository.softDelete(id, LocalDateTime.now().format(DT));
    }

    public void stockIn(long medicineId, BigDecimal qty, BigDecimal unitPrice, String remark) {
        if (medicineId <= 0) throw new IllegalArgumentException("medicineId 非法");
        if (qty == null || qty.signum() <= 0) throw new IllegalArgumentException("入库数量必须 > 0");
        if (unitPrice == null || unitPrice.signum() < 0) throw new IllegalArgumentException("入库单价必须 >= 0");

        repository.stockIn(
                medicineId,
                qty,
                unitPrice,
                "owner",
                remark == null ? "手工入库" : remark,
                "I" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"))
        );
    }

    private BigDecimal defaultZero(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    private String genCode() {
        return "M" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }
}