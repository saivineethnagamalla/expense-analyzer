package com.example.expenseanalyzer.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.expenseanalyzer.model.CategoryDetailResponse;
import com.example.expenseanalyzer.model.CategorySummary;
import com.example.expenseanalyzer.model.CategorySummaryOnlyResponse;
import com.example.expenseanalyzer.model.ComprehensiveExpenseReport;
import com.example.expenseanalyzer.model.ExpenseItem;
import com.example.expenseanalyzer.model.OverallSummaryResponse;
import com.example.expenseanalyzer.model.StatementSummaryResponse;

@Service
public class StatementParserService {

    private final CategoryConfigService categoryConfigService;

    // Matches dates: D/M, DD/MM, D/M/YY, D/M/YYYY using / or -
    // group(1) = day, group(2) = month, group(3) = optional year
    private static final Pattern DATE_HEADER_PATTERN = 
            Pattern.compile("^\\s*(\\d{1,2})[/-](\\d{1,2})(?:[/-](\\d{2}|\\d{4}))?\\s*$");

    private static final Pattern INLINE_DATE_PATTERN = 
            Pattern.compile("^(.*?)\\s+(\\d{1,2}[/-]\\d{1,2}(?:[/-](?:\\d{2}|\\d{4}))?)$");

    private static final DateTimeFormatter DISPLAY_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public StatementParserService(CategoryConfigService categoryConfigService) {
        this.categoryConfigService = categoryConfigService;
    }

    // =========================================================================
    // Text / File Extraction Utilities
    // =========================================================================

    public String extractRawText(MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename() != null ? file.getOriginalFilename().toLowerCase() : "";
        if (fileName.endsWith(".pdf")) {
            try (PDDocument doc = Loader.loadPDF(file.getBytes())) {
                return new PDFTextStripper().getText(doc);
            }
        }
        return new String(file.getBytes());
    }

    public List<ExpenseItem> extractEntries(MultipartFile file, String rawText) throws IOException {
        String textToProcess = rawText;
        if (file != null && !file.isEmpty()) {
            textToProcess = extractRawText(file);
        }
        return parseToEntries(textToProcess != null ? textToProcess : "");
    }

    // =========================================================================
    // Core Parsing Logic
    // =========================================================================

    public List<ExpenseItem> parseToEntries(String rawText) {
        List<ExpenseItem> items = new ArrayList<>();
        String currentDate = null;

        try (BufferedReader reader = new BufferedReader(new StringReader(rawText))) {
            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.replace('\u00A0', ' ')
                           .replace('—', '-')
                           .replace('–', '-')
                           .replace('−', '-')
                           .trim();

                if (line.isEmpty()) continue;

                // Handle inline run-on dates like "Curd - 10 1/6"
                Matcher inlineDateMatcher = INLINE_DATE_PATTERN.matcher(line);
                String nextDate = null;
                if (inlineDateMatcher.matches() && inlineDateMatcher.group(1).contains("-")) {
                    line = inlineDateMatcher.group(1).trim();
                    nextDate = normalizeAndValidateDate(inlineDateMatcher.group(2).trim(), lineNumber);
                }

                // Check if the current line is a date header (e.g. 1/9, 2-9, 1/9/2026)
                Matcher dateMatcher = DATE_HEADER_PATTERN.matcher(line);
                if (dateMatcher.matches()) {
                    currentDate = normalizeAndValidateDate(line, lineNumber);
                    continue;
                }

                if (line.contains("-")) {
                    if (currentDate == null) {
                        throw new IllegalArgumentException(
                            "Line " + lineNumber + ": Expense entry '" + line + 
                            "' is missing a preceding date/month header (e.g. '1/9')."
                        );
                    }
                    parseDelimitedLine(line, currentDate, items);
                } else {
                    // Line neither matches a valid date header nor contains an expense delimiter
                    throw new IllegalArgumentException(
                        "Line " + lineNumber + ": Unrecognized format or missing date header -> '" + line + "'"
                    );
                }

                if (nextDate != null) {
                    currentDate = nextDate;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error reading content", e);
        }

        return items;
    }

    private String normalizeAndValidateDate(String rawDate, int lineNumber) {
        Matcher matcher = DATE_HEADER_PATTERN.matcher(rawDate.trim());
        if (!matcher.matches()) {
            throw new IllegalArgumentException(
                "Line " + lineNumber + ": Invalid date format '" + rawDate + "'. Expected format: D/M or D/M/YYYY."
            );
        }

        int day = Integer.parseInt(matcher.group(1));
        int month = Integer.parseInt(matcher.group(2));
        int currentYear = LocalDate.now().getYear();
        int year;

        if (matcher.group(3) != null) {
            String rawYear = matcher.group(3);
            year = (rawYear.length() == 2) ? 2000 + Integer.parseInt(rawYear) : Integer.parseInt(rawYear);
        } else {
            year = currentYear;
        }

        try {
            LocalDate parsedDate = LocalDate.of(year, month, day);
            return parsedDate.format(DISPLAY_DATE_FORMATTER);
        } catch (DateTimeException e) {
            throw new IllegalArgumentException(
                "Line " + lineNumber + ": Invalid calendar date '" + rawDate + "'. " + e.getMessage()
            );
        }
    }

    private void parseDelimitedLine(String line, String currentDate, List<ExpenseItem> items) {
        int firstHyphen = line.indexOf('-');
        if (firstHyphen == -1) return;

        String desc = line.substring(0, firstHyphen).trim();
        String remainder = line.substring(firstHyphen + 1)
                               .replace("*", "")
                               .replace("/-", "")
                               .replace(",", "")
                               .trim();

        BigDecimal amount;
        if (remainder.contains("-") && !remainder.startsWith("-")) {
            int lastHyphen = remainder.lastIndexOf('-');
            String finalPart = remainder.substring(lastHyphen + 1).trim();
            BigDecimal parsedFinal = parseExpressionOrNumber(finalPart);
            if (parsedFinal.compareTo(BigDecimal.ZERO) != 0 || finalPart.equals("0")) {
                amount = parsedFinal;
            } else {
                amount = parseExpressionOrNumber(remainder);
            }
        } else {
            amount = parseExpressionOrNumber(remainder);
        }

        if (amount.compareTo(BigDecimal.ZERO) == 0) {
            items.add(new ExpenseItem(currentDate, desc + " (Refunded / Cancelled)", BigDecimal.ZERO, "Refunded / Cancelled"));
        } else if (amount.compareTo(BigDecimal.ZERO) > 0) {
            String category = categoryConfigService.categorize(desc);
            items.add(new ExpenseItem(currentDate, desc, amount, category));
        }
    }

    private BigDecimal parseExpressionOrNumber(String text) {
        try {
            if (text.contains("+")) {
                String[] parts = text.split("\\+");
                BigDecimal sum = BigDecimal.ZERO;
                for (String part : parts) {
                    String clean = part.replaceAll("[^0-9.]", "").trim();
                    if (!clean.isEmpty()) {
                        sum = sum.add(new BigDecimal(clean));
                    }
                }
                return sum;
            } else {
                String clean = text.replaceAll("[^0-9.]", "").trim();
                return clean.isEmpty() ? BigDecimal.ZERO : new BigDecimal(clean);
            }
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    // =========================================================================
    // Aggregators & Reports
    // =========================================================================

    public StatementSummaryResponse processFile(MultipartFile file) throws IOException {
        String rawText = extractRawText(file);
        return parseStatementText(rawText);
    }

    public StatementSummaryResponse parseStatementText(String rawText) {
        List<ExpenseItem> items = parseToEntries(rawText);
        return aggregateFullResults(items);
    }

    private StatementSummaryResponse aggregateFullResults(List<ExpenseItem> items) {
        BigDecimal grandTotal = BigDecimal.ZERO;
        BigDecimal totalExpenses = BigDecimal.ZERO;
        BigDecimal totalInvestments = BigDecimal.ZERO;

        Map<String, BigDecimal> categorySums = new HashMap<>();
        Map<String, Integer> categoryCounts = new HashMap<>();

        for (ExpenseItem item : items) {
            BigDecimal amt = item.getAmount();
            grandTotal = grandTotal.add(amt);

            if (categoryConfigService.isInvestment(item.getCategory())) {
                totalInvestments = totalInvestments.add(amt);
            } else {
                totalExpenses = totalExpenses.add(amt);
            }

            categorySums.merge(item.getCategory(), amt, BigDecimal::add);
            categoryCounts.merge(item.getCategory(), 1, Integer::sum);
        }

        List<CategorySummary> breakdowns = categorySums.entrySet().stream()
                .map(e -> new CategorySummary(e.getKey(), e.getValue().setScale(2, RoundingMode.HALF_UP), categoryCounts.get(e.getKey())))
                .sorted((a, b) -> b.getTotalAmount().compareTo(a.getTotalAmount()))
                .collect(Collectors.toList());

        return new StatementSummaryResponse(
                grandTotal.setScale(2, RoundingMode.HALF_UP),
                totalExpenses.setScale(2, RoundingMode.HALF_UP),
                totalInvestments.setScale(2, RoundingMode.HALF_UP),
                breakdowns,
                items
        );
    }

    public OverallSummaryResponse toOverallSummary(List<ExpenseItem> items) {
        BigDecimal grandTotal = BigDecimal.ZERO;
        BigDecimal totalExpenses = BigDecimal.ZERO;
        BigDecimal totalInvestments = BigDecimal.ZERO;

        for (ExpenseItem item : items) {
            BigDecimal amt = item.getAmount();
            grandTotal = grandTotal.add(amt);
            if (categoryConfigService.isInvestment(item.getCategory())) {
                totalInvestments = totalInvestments.add(amt);
            } else {
                totalExpenses = totalExpenses.add(amt);
            }
        }

        return new OverallSummaryResponse(
                grandTotal.setScale(2, RoundingMode.HALF_UP),
                totalExpenses.setScale(2, RoundingMode.HALF_UP),
                totalInvestments.setScale(2, RoundingMode.HALF_UP),
                items.size()
        );
    }

    public List<CategoryDetailResponse> toCategoryDetails(List<ExpenseItem> items) {
        Map<String, List<ExpenseItem>> grouped = items.stream()
                .collect(Collectors.groupingBy(ExpenseItem::getCategory));

        return grouped.entrySet().stream()
                .map(entry -> {
                    String category = entry.getKey();
                    List<ExpenseItem> catItems = entry.getValue();

                    BigDecimal total = catItems.stream()
                            .map(ExpenseItem::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add)
                            .setScale(2, RoundingMode.HALF_UP);

                    List<CategoryDetailResponse.TransactionRef> txs = catItems.stream()
                            .map(i -> new CategoryDetailResponse.TransactionRef(i.getDate(), i.getDescription(), i.getAmount()))
                            .collect(Collectors.toList());

                    return new CategoryDetailResponse(category, total, catItems.size(), txs);
                })
                .sorted((a, b) -> b.getTotalAmount().compareTo(a.getTotalAmount()))
                .collect(Collectors.toList());
    }

    public List<CategorySummaryOnlyResponse> getCategoryTotalsSummary(List<ExpenseItem> items) {
        Map<String, BigDecimal> categorySums = new HashMap<>();
        Map<String, Integer> categoryCounts = new HashMap<>();

        for (ExpenseItem item : items) {
            categorySums.merge(item.getCategory(), item.getAmount(), BigDecimal::add);
            categoryCounts.merge(item.getCategory(), 1, Integer::sum);
        }

        return categorySums.entrySet().stream()
                .map(entry -> new CategorySummaryOnlyResponse(
                        entry.getKey(),
                        entry.getValue().setScale(2, RoundingMode.HALF_UP),
                        categoryCounts.get(entry.getKey())
                ))
                .sorted((a, b) -> b.getTotalAmount().compareTo(a.getTotalAmount()))
                .collect(Collectors.toList());
    }

    public ComprehensiveExpenseReport buildComprehensiveReport(List<ExpenseItem> items) {
        OverallSummaryResponse summary = toOverallSummary(items);
        List<CategoryDetailResponse> categories = toCategoryDetails(items);
        return new ComprehensiveExpenseReport(summary, categories, items);
    }
}