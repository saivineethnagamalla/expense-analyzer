package com.example.expenseanalyzer.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.expenseanalyzer.service.CategoryConfigService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/rules")
@Tag(name = "Category Rule Management", description = "Endpoints to view and manage active categorization rules")
public class CategoryRuleController {

    private final CategoryConfigService categoryConfigService;

    public CategoryRuleController(CategoryConfigService categoryConfigService) {
        this.categoryConfigService = categoryConfigService;
    }

    @Operation(summary = "Get all active category rules as a JSON map")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, List<String>>> getActiveRules() {
        return ResponseEntity.ok(categoryConfigService.getCurrentRules());
    }

    @Operation(summary = "Add a keyword to a category in memory")
    @PostMapping("/keyword")
    public ResponseEntity<String> addKeyword(
            @RequestParam String category,
            @RequestParam String keyword) {
        if (category == null || category.isBlank() || keyword == null || keyword.isBlank()) {
            return ResponseEntity.badRequest().body("Category and keyword must not be empty.");
        }

        categoryConfigService.addKeyword(category.trim(), keyword.trim());
        return ResponseEntity.ok("Added keyword '" + keyword.trim().toUpperCase() + "' to category '" + category.trim() + "'.");
    }
}