package com.yourname.clinic.module.dashboard;

import java.math.BigDecimal;

public class DashboardService {
    private final DashboardRepository repo = new DashboardRepository();

    public DashboardStat load() {
        BigDecimal todayIncome = nvl(repo.todayIncome());
        BigDecimal monthIncome = nvl(repo.monthIncome());
        BigDecimal monthExpense = nvl(repo.monthExpense());
        BigDecimal monthProfit = monthIncome.subtract(monthExpense);
        int lowStockCount = repo.lowStockCount();

        return new DashboardStat(todayIncome, monthIncome, monthExpense, monthProfit, lowStockCount);
    }

    private BigDecimal nvl(BigDecimal v) { return v == null ? BigDecimal.ZERO : v; }

    public record DashboardStat(
            BigDecimal todayIncome,
            BigDecimal monthIncome,
            BigDecimal monthExpense,
            BigDecimal monthProfit,
            int lowStockCount
    ) {}
}