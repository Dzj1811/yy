package com.yourname.clinic.module.inventory;

import java.math.BigDecimal;

public class Medicine {
    private Long id;
    private String medicineCode;
    private String name;
    private String spec;
    private String unit;
    private String category;
    private BigDecimal purchasePrice;
    private BigDecimal salePrice;
    private BigDecimal stockQty;
    private BigDecimal minStockQty;
    private String supplier;
    private String expireDate;
    private String remark;
    private String createdAt;
    private String updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getMedicineCode() { return medicineCode; }
    public void setMedicineCode(String medicineCode) { this.medicineCode = medicineCode; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSpec() { return spec; }
    public void setSpec(String spec) { this.spec = spec; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public BigDecimal getPurchasePrice() { return purchasePrice; }
    public void setPurchasePrice(BigDecimal purchasePrice) { this.purchasePrice = purchasePrice; }

    public BigDecimal getSalePrice() { return salePrice; }
    public void setSalePrice(BigDecimal salePrice) { this.salePrice = salePrice; }

    public BigDecimal getStockQty() { return stockQty; }
    public void setStockQty(BigDecimal stockQty) { this.stockQty = stockQty; }

    public BigDecimal getMinStockQty() { return minStockQty; }
    public void setMinStockQty(BigDecimal minStockQty) { this.minStockQty = minStockQty; }

    public String getSupplier() { return supplier; }
    public void setSupplier(String supplier) { this.supplier = supplier; }

    public String getExpireDate() { return expireDate; }
    public void setExpireDate(String expireDate) { this.expireDate = expireDate; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}