package com.example.expenseanalyzer.model;

import java.math.BigDecimal;
import java.util.List;

public class StatementSummaryResponse {
    private BigDecimal grandTotal;
    private BigDecimal totalExpenses;
    private BigDecimal totalInvestments;
    private List<CategorySummary> categoryBreakdown;
    private List<ExpenseItem> allEntries;

    public StatementSummaryResponse(BigDecimal grandTotal, BigDecimal totalExpenses,
                                    BigDecimal totalInvestments, List<CategorySummary> categoryBreakdown,
                                    List<ExpenseItem> allEntries) {
        this.grandTotal = grandTotal;
        this.totalExpenses = totalExpenses;
        this.totalInvestments = totalInvestments;
        this.categoryBreakdown = categoryBreakdown;
        this.allEntries = allEntries;
    }

    public BigDecimal getGrandTotal() { return grandTotal; }
    public BigDecimal getTotalExpenses() { return totalExpenses; }
    public BigDecimal getTotalInvestments() { return totalInvestments; }
    public List<CategorySummary> getCategoryBreakdown() { return categoryBreakdown; }
    public List<ExpenseItem> getAllEntries() { return allEntries; }
}