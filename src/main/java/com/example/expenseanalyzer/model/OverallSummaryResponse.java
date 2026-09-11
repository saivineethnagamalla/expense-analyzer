package com.example.expenseanalyzer.model;

import java.math.BigDecimal;

public class OverallSummaryResponse {
    private BigDecimal grandTotal;
    private BigDecimal totalExpenses;
    private BigDecimal totalInvestments;
    private int totalTransactionCount;

    public OverallSummaryResponse(BigDecimal grandTotal, BigDecimal totalExpenses,
                                  BigDecimal totalInvestments, int totalTransactionCount) {
        this.grandTotal = grandTotal;
        this.totalExpenses = totalExpenses;
        this.totalInvestments = totalInvestments;
        this.totalTransactionCount = totalTransactionCount;
    }

    public BigDecimal getGrandTotal() { return grandTotal; }
    public BigDecimal getTotalExpenses() { return totalExpenses; }
    public BigDecimal getTotalInvestments() { return totalInvestments; }
    public int getTotalTransactionCount() { return totalTransactionCount; }
}