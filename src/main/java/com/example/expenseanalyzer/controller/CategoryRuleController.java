package com.example.expenseanalyzer.controller;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.expenseanalyzer.service.CategoryConfigService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/rules")
@Tag(name = "Category Rule Management", description = "Endpoints to view, update, and manage the category mapping rules")
public class CategoryRuleController {

    private final CategoryConfigService categoryConfigService;

    public CategoryRuleController(CategoryConfigService categoryConfigService) {
        this.categoryConfigService = categoryConfigService;
    }

    @Operation(summary = "Get active category rules as JSON map")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, List<String>>> getActiveRules() {
        return ResponseEntity.ok(categoryConfigService.getCurrentRules());
    }

    @Operation(summary = "Get raw categories.txt file content")
    @GetMapping(value = "/raw", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> getRawConfigFile() throws IOException {
        return ResponseEntity.ok(categoryConfigService.getRawConfigContent());
    }

    @Operation(summary = "Update rules via raw text string")
    @PutMapping(value = "/raw", consumes = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> updateRulesFromText(@RequestBody String updatedRules) throws IOException {
        categoryConfigService.updateRulesContent(updatedRules);
        return ResponseEntity.ok("Category rules updated and reloaded successfully.");
    }

    @Operation(summary = "Upload a new categories.txt file")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadConfigFile(@RequestPart("file") MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body("File cannot be empty");
        }
        String content = new String(file.getBytes(), StandardCharsets.UTF_8);
        categoryConfigService.updateRulesContent(content);
        return ResponseEntity.ok("Category file uploaded and rules reloaded successfully.");
    }

    @Operation(summary = "Reload rules directly from local file")
    @PostMapping(value = "/reload")
    public ResponseEntity<String> reloadRulesFromFile() {
        categoryConfigService.loadRulesFromFile();
        return ResponseEntity.ok("Rules reloaded from disk.");
    }
}