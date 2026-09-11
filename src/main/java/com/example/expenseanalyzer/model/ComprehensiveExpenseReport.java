package com.example.expenseanalyzer.model;

import java.math.BigDecimal;
import java.util.List;

public class ComprehensiveExpenseReport {
    private OverallSummaryResponse summary;
    private List<CategoryDetailResponse> categoryBreakdown;
    private List<ExpenseItem> allEntries;

    public ComprehensiveExpenseReport() {}

    public ComprehensiveExpenseReport(OverallSummaryResponse summary,
                                      List<CategoryDetailResponse> categoryBreakdown,
                                      List<ExpenseItem> allEntries) {
        this.summary = summary;
        this.categoryBreakdown = categoryBreakdown;
        this.allEntries = allEntries;
    }

    public OverallSummaryResponse getSummary() { return summary; }
    public void setSummary(OverallSummaryResponse summary) { this.summary = summary; }

    public List<CategoryDetailResponse> getCategoryBreakdown() { return categoryBreakdown; }
    public void setCategoryBreakdown(List<CategoryDetailResponse> categoryBreakdown) { this.categoryBreakdown = categoryBreakdown; }

    public List<ExpenseItem> getAllEntries() { return allEntries; }
    public void setAllEntries(List<ExpenseItem> allEntries) { this.allEntries = allEntries; }
}