package com.example.expenseanalyzer.model;

import java.math.BigDecimal;

public class ExpenseItem {
    private String date;
    private String description;
    private BigDecimal amount;
    private String category;

    public ExpenseItem() {}

    public ExpenseItem(String date, String description, BigDecimal amount, String category) {
        this.date = date;
        this.description = description;
        this.amount = amount;
        this.category = category;
    }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
}