package com.example.expenseanalyzer.controller;

import com.example.expenseanalyzer.model.ComprehensiveExpenseReport;
import com.example.expenseanalyzer.model.ExpenseItem;
import com.example.expenseanalyzer.service.StatementParserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/expenses/details")
@Tag(name = "3. Full Details Controller", description = "Endpoints returning full, comprehensive expense reports combining summaries, categories, and item lists")
public class ExpenseDetailController {

    private final StatementParserService parserService;

    public ExpenseDetailController(StatementParserService parserService) {
        this.parserService = parserService;
    }

    @Operation(summary = "Full comprehensive report from raw text", description = "Combines overall summary, category breakdown, and raw items in one payload.")
    @PostMapping(value = "/text", consumes = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<ComprehensiveExpenseReport> getFullDetailsFromText(@RequestBody String rawText) {
        List<ExpenseItem> items = parserService.parseToEntries(rawText);
        return ResponseEntity.ok(parserService.buildComprehensiveReport(items));
    }

    @Operation(summary = "Full comprehensive report from file (.pdf or .txt)", description = "Upload file to receive overall summary, category breakdown, and raw items in one payload.")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ComprehensiveExpenseReport> getFullDetailsFromFile(@RequestPart("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        List<ExpenseItem> items = parserService.extractEntries(file, null);
        return ResponseEntity.ok(parserService.buildComprehensiveReport(items));
    }
}