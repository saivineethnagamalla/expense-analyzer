package com.example.expenseanalyzer.controller;

import com.example.expenseanalyzer.model.CategoryDetailResponse;
import com.example.expenseanalyzer.model.CategorySummaryOnlyResponse;
import com.example.expenseanalyzer.model.ExpenseItem;
import com.example.expenseanalyzer.model.OverallSummaryResponse;
import com.example.expenseanalyzer.service.StatementParserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/expenses/text")
@Tag(name = "1. Text Expense Controller", description = "Endpoints for parsing raw text notes and string statements")
public class TextExpenseController {

    private final StatementParserService parserService;

    public TextExpenseController(StatementParserService parserService) {
        this.parserService = parserService;
    }

    // Catches IllegalArgumentException for all endpoints in this controller
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleParsingError(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
            "status", 400,
            "error", "Invalid Input Format",
            "message", ex.getMessage()
        ));
    }

    @Operation(summary = "Get overall summary from raw text")
    @PostMapping(value = "/summary", consumes = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<OverallSummaryResponse> getSummary(@RequestBody String rawText) {
        List<ExpenseItem> items = parserService.parseToEntries(rawText);
        return ResponseEntity.ok(parserService.toOverallSummary(items));
    }

    @Operation(summary = "Get category-only totals from raw text")
    @PostMapping(value = "/category-totals", consumes = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<List<CategorySummaryOnlyResponse>> getCategoryTotals(@RequestBody String rawText) {
        List<ExpenseItem> items = parserService.parseToEntries(rawText);
        return ResponseEntity.ok(parserService.getCategoryTotalsSummary(items));
    }

    @Operation(summary = "Get all line-item entries from raw text")
    @PostMapping(value = "/entries", consumes = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<List<ExpenseItem>> getEntries(@RequestBody String rawText) {
        List<ExpenseItem> items = parserService.parseToEntries(rawText);
        return ResponseEntity.ok(items);
    }

    @Operation(summary = "Get category breakdown with itemized transactions from raw text")
    @PostMapping(value = "/categories", consumes = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<List<CategoryDetailResponse>> getCategoryDetails(@RequestBody String rawText) {
        List<ExpenseItem> items = parserService.parseToEntries(rawText);
        return ResponseEntity.ok(parserService.toCategoryDetails(items));
    }
}