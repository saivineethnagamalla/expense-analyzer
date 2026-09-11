package com.example.expenseanalyzer.model;

import java.math.BigDecimal;
import java.util.List;

public class CategoryDetailResponse {
    private String category;
    private BigDecimal totalAmount;
    private int transactionCount;
    private List<TransactionRef> transactions;

    public CategoryDetailResponse(String category, BigDecimal totalAmount, 
                                  int transactionCount, List<TransactionRef> transactions) {
        this.category = category;
        this.totalAmount = totalAmount;
        this.transactionCount = transactionCount;
        this.transactions = transactions;
    }

    public String getCategory() { return category; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public int getTransactionCount() { return transactionCount; }
    public List<TransactionRef> getTransactions() { return transactions; }

    public static class TransactionRef {
        private String date;
        private String description;
        private BigDecimal amount;

        public TransactionRef(String date, String description, BigDecimal amount) {
            this.date = date;
            this.description = description;
            this.amount = amount;
        }

        public String getDate() { return date; }
        public String getDescription() { return description; }
        public BigDecimal getAmount() { return amount; }
    }
}