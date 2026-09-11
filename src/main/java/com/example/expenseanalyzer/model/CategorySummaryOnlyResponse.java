package com.example.expenseanalyzer.model;

import java.math.BigDecimal;

public class CategorySummaryOnlyResponse {
    private String category;
    private BigDecimal totalAmount;
    private int transactionCount;

    public CategorySummaryOnlyResponse() {}

    public CategorySummaryOnlyResponse(String category, BigDecimal totalAmount, int transactionCount) {
        this.category = category;
        this.totalAmount = totalAmount;
        this.transactionCount = transactionCount;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public int getTransactionCount() {
        return transactionCount;
    }

    public void setTransactionCount(int transactionCount) {
        this.transactionCount = transactionCount;
    }
}