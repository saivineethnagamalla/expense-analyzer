package com.example.expenseanalyzer.model;

import java.math.BigDecimal;

public class CategorySummary {
    private String category;
    private BigDecimal totalAmount;
    private int transactionCount;

    public CategorySummary(String category, BigDecimal totalAmount, int transactionCount) {
        this.category = category;
        this.totalAmount = totalAmount;
        this.transactionCount = transactionCount;
    }

    public String getCategory() { return category; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public int getTransactionCount() { return transactionCount; }
}