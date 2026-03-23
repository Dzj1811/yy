package com.yourname.clinic.model;

public record FinanceRow(String id, String txnNo, String type, String category, String amount,
                         String date, String payMethod, String counterparty, String remark) {
}
