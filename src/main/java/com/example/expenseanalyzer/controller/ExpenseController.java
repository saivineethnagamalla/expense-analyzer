//package com.example.expenseanalyzer.controller;
//
//import java.io.IOException;
//import java.util.List;
//
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestPart;
//import org.springframework.web.bind.annotation.RestController;
//import org.springframework.web.multipart.MultipartFile;
//
//import com.example.expenseanalyzer.model.CategoryDetailResponse;
//import com.example.expenseanalyzer.model.CategorySummaryOnlyResponse;
//import com.example.expenseanalyzer.model.ExpenseItem;
//import com.example.expenseanalyzer.model.OverallSummaryResponse;
//import com.example.expenseanalyzer.model.StatementSummaryResponse;
//import com.example.expenseanalyzer.service.StatementParserService;
//
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.tags.Tag;
//
//@RestController
//@RequestMapping("/api/v1/expenses")
//@Tag(name = "Expense & Investment Analyzer", description = "Endpoints for full processing, summary, raw entries, and category drill-downs")
//public class ExpenseController {
//
//	private final StatementParserService parserService;
//
//	public ExpenseController(StatementParserService parserService) {
//		this.parserService = parserService;
//	}
//
//	// =============================================================
//	// ORIGINAL FLOW (PRESERVED - DO NOT MODIFY)
//	// =============================================================
//
//	@Operation(summary = "Original: Upload PDF or text file for full statement summary")
//	@PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//	public ResponseEntity<StatementSummaryResponse> uploadFile(@RequestPart("file") MultipartFile file)
//			throws IOException {
//		if (file.isEmpty())
//			return ResponseEntity.badRequest().build();
//		return ResponseEntity.ok(parserService.processFile(file));
//	}
//
//	@Operation(summary = "Original: Parse raw text for full statement summary")
//	@PostMapping(value = "/parse-text", consumes = MediaType.TEXT_PLAIN_VALUE)
//	public ResponseEntity<StatementSummaryResponse> parseRawText(@RequestBody String rawText) {
//		if (rawText == null || rawText.trim().isEmpty())
//			return ResponseEntity.badRequest().build();
//		return ResponseEntity.ok(parserService.parseStatementText(rawText));
//	}
//
//	// =============================================================
//	// NEW SPLIT APIs (Accepts either raw text body or file upload)
//	// =============================================================
//
//	// --- API 1: ONLY SUMMARY ---
//	@Operation(summary = "1. Get Overall Summary Only", description = "Returns grand total, total expenses, total investments, and transaction count.")
//	@PostMapping(value = "/summary", consumes = MediaType.TEXT_PLAIN_VALUE)
//	public ResponseEntity<OverallSummaryResponse> getSummary(@RequestBody String rawText) throws IOException {
//		List<ExpenseItem> items = parserService.extractEntries(null, rawText);
//		return ResponseEntity.ok(parserService.toOverallSummary(items));
//	}
//
//	@Operation(summary = "1. Get Overall Summary Only (File Upload)")
//	@PostMapping(value = "/summary/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//	public ResponseEntity<OverallSummaryResponse> getSummaryFile(@RequestPart("file") MultipartFile file)
//			throws IOException {
//		List<ExpenseItem> items = parserService.extractEntries(file, null);
//		return ResponseEntity.ok(parserService.toOverallSummary(items));
//	}
//
//	// --- API 2: ALL ENTRIES ---
//	@Operation(summary = "2. Get All Entries List", description = "Returns list of items with date, description, amount, and category.")
//	@PostMapping(value = "/entries", consumes = MediaType.TEXT_PLAIN_VALUE)
//	public ResponseEntity<List<ExpenseItem>> getEntries(@RequestBody String rawText) throws IOException {
//		List<ExpenseItem> items = parserService.extractEntries(null, rawText);
//		return ResponseEntity.ok(items);
//	}
//
//	@Operation(summary = "2. Get All Entries List (File Upload)")
//	@PostMapping(value = "/entries/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//	public ResponseEntity<List<ExpenseItem>> getEntriesFile(@RequestPart("file") MultipartFile file)
//			throws IOException {
//		List<ExpenseItem> items = parserService.extractEntries(file, null);
//		return ResponseEntity.ok(items);
//	}
//
//	// --- API 3: CATEGORY INFO & DRILLDOWN ---
//	@Operation(summary = "3. Get Category Info & Transactions", description = "Returns category totals, counts, and individual transaction dates and amounts.")
//	@PostMapping(value = "/categories", consumes = MediaType.TEXT_PLAIN_VALUE)
//	public ResponseEntity<List<CategoryDetailResponse>> getCategories(@RequestBody String rawText) throws IOException {
//		List<ExpenseItem> items = parserService.extractEntries(null, rawText);
//		return ResponseEntity.ok(parserService.toCategoryDetails(items));
//	}
//
//	@Operation(summary = "3. Get Category Info & Transactions (File Upload)")
//	@PostMapping(value = "/categories/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//	public ResponseEntity<List<CategoryDetailResponse>> getCategoriesFile(@RequestPart("file") MultipartFile file)
//			throws IOException {
//		List<ExpenseItem> items = parserService.extractEntries(file, null);
//		return ResponseEntity.ok(parserService.toCategoryDetails(items));
//	}
//
//	@Operation(summary = "Category Summary Only", description = "Returns only the aggregated amount and transaction count per category, ordered from highest to lowest spend.")
//	@PostMapping(value = "/summary/categories", consumes = MediaType.TEXT_PLAIN_VALUE)
//	public ResponseEntity<List<CategorySummaryOnlyResponse>> getCategorySummaryOnly(@RequestBody String rawText)
//			throws IOException {
//		List<ExpenseItem> items = parserService.extractEntries(null, rawText);
//		return ResponseEntity.ok(parserService.getCategoryTotalsSummary(items));
//	}
//
//	@Operation(summary = "Category Summary Only (File/PDF Upload)", description = "Upload a statement or notes file (.txt or .pdf) to get aggregated category totals only.")
//	@PostMapping(value = "/summary/categories/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//	public ResponseEntity<List<CategorySummaryOnlyResponse>> getCategorySummaryOnlyFile(
//			@RequestPart("file") MultipartFile file) throws IOException {
//		List<ExpenseItem> items = parserService.extractEntries(file, null);
//		return ResponseEntity.ok(parserService.getCategoryTotalsSummary(items));
//	}
//}