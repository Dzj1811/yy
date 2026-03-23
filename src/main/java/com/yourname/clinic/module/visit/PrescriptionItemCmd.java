package com.yourname.clinic.module.visit;

import java.math.BigDecimal;

public class PrescriptionItemCmd {
    private long medicineId;
    private BigDecimal quantity;
    private String unit;
    private BigDecimal unitPrice;
    private String usageNote;

    public long getMedicineId() { return medicineId; }
    public void setMedicineId(long medicineId) { this.medicineId = medicineId; }

    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    public String getUsageNote() { return usageNote; }
    public void setUsageNote(String usageNote) { this.usageNote = usageNote; }
}