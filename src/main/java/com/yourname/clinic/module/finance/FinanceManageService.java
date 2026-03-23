package com.yourname.clinic.module.finance;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class FinanceManageService {
    private final FinanceManageRepository repo = new FinanceManageRepository();

    public List<FinanceManageRepository.CategoryItem> listIncomeCategories() {
        return repo.listCategories("INCOME");
    }

    public List<FinanceManageRepository.CategoryItem> listExpenseCategories() {
        return repo.listCategories("EXPENSE");
    }

    public void addIncome(long categoryId, BigDecimal amount, String paymentMethod, String counterparty, String remark) {
        validate(categoryId, amount);
        repo.addTxn(genNo(), "INCOME", categoryId, amount,
                paymentMethod == null || paymentMethod.isBlank() ? "现金" : paymentMethod,
                counterparty, remark, Timestamp.valueOf(LocalDateTime.now()));
    }

    public void addExpense(long categoryId, BigDecimal amount, String paymentMethod, String counterparty, String remark) {
        validate(categoryId, amount);
        repo.addTxn(genNo(), "EXPENSE", categoryId, amount,
                paymentMethod == null || paymentMethod.isBlank() ? "现金" : paymentMethod,
                counterparty, remark, Timestamp.valueOf(LocalDateTime.now()));
    }

    public List<FinanceManageRepository.FinanceRowDTO> query(String type, String fromDate, String toDate) {
        return repo.queryTxn(type, fromDate, toDate);
    }

    private void validate(long categoryId, BigDecimal amount) {
        if (categoryId <= 0) throw new IllegalArgumentException("分类ID非法");
        if (amount == null || amount.signum() <= 0) throw new IllegalArgumentException("金额必须大于0");
    }

    private String genNo() {
        return "F" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
    }
}