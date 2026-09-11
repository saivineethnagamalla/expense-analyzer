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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/expenses/pdf")
@Tag(name = "2. PDF Expense Controller", description = "Endpoints for parsing PDF statements via file upload")
public class PdfExpenseController {

    private final StatementParserService parserService;

    public PdfExpenseController(StatementParserService parserService) {
        this.parserService = parserService;
    }

    private void validatePdfFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be null or empty.");
        }
        String fileName = file.getOriginalFilename();
        if (fileName == null || !fileName.toLowerCase().endsWith(".pdf")) {
            throw new IllegalArgumentException("Only .pdf files are accepted by this controller.");
        }
    }

    @Operation(summary = "Get overall summary from PDF file")
    @PostMapping(value = "/summary", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<OverallSummaryResponse> getSummaryFromPdf(@RequestPart("file") MultipartFile file) throws IOException {
        validatePdfFile(file);
        List<ExpenseItem> items = parserService.extractEntries(file, null);
        return ResponseEntity.ok(parserService.toOverallSummary(items));
    }

    @Operation(summary = "Get category-only totals from PDF file")
    @PostMapping(value = "/category-totals", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<CategorySummaryOnlyResponse>> getCategoryTotalsFromPdf(@RequestPart("file") MultipartFile file) throws IOException {
        validatePdfFile(file);
        List<ExpenseItem> items = parserService.extractEntries(file, null);
        return ResponseEntity.ok(parserService.getCategoryTotalsSummary(items));
    }

    @Operation(summary = "Get all line-item entries from PDF file")
    @PostMapping(value = "/entries", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<ExpenseItem>> getEntriesFromPdf(@RequestPart("file") MultipartFile file) throws IOException {
        validatePdfFile(file);
        List<ExpenseItem> items = parserService.extractEntries(file, null);
        return ResponseEntity.ok(items);
    }

    @Operation(summary = "Get category breakdown with itemized transactions from PDF file")
    @PostMapping(value = "/categories", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<CategoryDetailResponse>> getCategoryDetailsFromPdf(@RequestPart("file") MultipartFile file) throws IOException {
        validatePdfFile(file);
        List<ExpenseItem> items = parserService.extractEntries(file, null);
        return ResponseEntity.ok(parserService.toCategoryDetails(items));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleInvalidFile(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
}