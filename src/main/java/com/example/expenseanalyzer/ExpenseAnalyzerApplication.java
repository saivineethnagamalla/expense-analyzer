package com.example.expenseanalyzer;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(
    info = @Info(
        title = "Local Expense & Statement Analyzer API",
        version = "1.0",
        description = "Offline processing engine for monthly expenses, investments, and daily logs."
    )
)
public class ExpenseAnalyzerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExpenseAnalyzerApplication.class, args);
    }
}