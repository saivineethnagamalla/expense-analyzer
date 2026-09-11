package com.example.expenseanalyzer.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CategoryConfigService {

    @Value("${expense.categories.file-path:categories.txt}")
    private String configFilePath;

    private final Map<String, List<String>> rulesMap = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private long lastModifiedTime = 0L;

    @PostConstruct
    public void init() {
        loadRulesFromFile();
    }

    /**
     * Loads or reloads the rules from disk.
     * If the file does not exist, a default template is created.
     * If parsing fails, embedded defaults are loaded into memory.
     */
    public synchronized void loadRulesFromFile() {
        File file = new File(configFilePath);
        if (!file.exists()) {
            createDefaultConfigFile(file);
        }

        try {
            String content = Files.readString(Path.of(configFilePath), StandardCharsets.UTF_8);
            parseAndPopulateRules(content);
            this.lastModifiedTime = file.lastModified();
        } catch (Exception e) {
            System.err.println("Could not read rules file from disk, falling back to embedded defaults: " + e.getMessage());
            loadEmbeddedDefaults();
        }
    }

    /**
     * Automatically checks whether the file on disk was modified.
     * Hot-reloads in-memory rules if a newer timestamp is detected.
     */
    public synchronized void checkAndReloadIfModified() {
        File file = new File(configFilePath);
        if (!file.exists()) {
            createDefaultConfigFile(file);
            return;
        }

        long currentModified = file.lastModified();
        if (currentModified > this.lastModifiedTime) {
            try {
                String content = Files.readString(Path.of(configFilePath), StandardCharsets.UTF_8);
                parseAndPopulateRules(content);
                this.lastModifiedTime = currentModified;
                System.out.println(">> categories.txt updated on disk. In-memory rules reloaded.");
            } catch (Exception e) {
                System.err.println("Failed to auto-reload modified categories.txt: " + e.getMessage());
            }
        }
    }

    /**
     * Updates the file on disk with new content and updates the in-memory cache.
     */
    public synchronized void updateRulesContent(String newContent) throws IOException {
        File file = new File(configFilePath);
        try (FileWriter writer = new FileWriter(file, StandardCharsets.UTF_8, false)) {
            writer.write(newContent);
        }
        this.lastModifiedTime = file.lastModified();
        parseAndPopulateRules(newContent);
    }

    /**
     * Parses the configuration string (supports JSON map as well as fallback plain text key-value).
     */
    private void parseAndPopulateRules(String content) {
        if (content == null || content.isBlank()) {
            loadEmbeddedDefaults();
            return;
        }

        String trimmed = content.trim();
        Map<String, List<String>> temp = new HashMap<>();

        // Try JSON parsing first
        if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
            try {
                Map<String, List<String>> jsonMap = objectMapper.readValue(
                        trimmed,
                        new TypeReference<Map<String, List<String>>>() {}
                );

                jsonMap.forEach((category, keywords) -> {
                    if (category != null && keywords != null) {
                        List<String> upperList = keywords.stream()
                                .filter(Objects::nonNull)
                                .map(String::trim)
                                .map(String::toUpperCase)
                                .filter(s -> !s.isEmpty())
                                .toList();
                        temp.put(category.trim(), upperList);
                    }
                });

                rulesMap.clear();
                rulesMap.putAll(temp);
                return;
            } catch (Exception e) {
                System.err.println("JSON parse error, attempting line-by-line fallback: " + e.getMessage());
            }
        }

        // Line-by-line fallback format: Category: KW1, KW2
        parsePlainText(trimmed, temp);
        if (!temp.isEmpty()) {
            rulesMap.clear();
            rulesMap.putAll(temp);
        } else {
            loadEmbeddedDefaults();
        }
    }

    private void parsePlainText(String content, Map<String, List<String>> targetMap) {
        String[] lines = content.split("\\r?\\n");
        for (String line : lines) {
            String trimmedLine = line.trim();
            if (trimmedLine.isEmpty() || trimmedLine.startsWith("#")) continue;

            String[] parts = trimmedLine.split(":", 2);
            if (parts.length == 2) {
                String category = parts[0].replaceAll("[\"'{}\\[\\]]", "").trim();
                String rawKeywords = parts[1].replaceAll("[\"'{}\\[\\]]", "");
                String[] splitKw = rawKeywords.split(",");

                List<String> cleanList = new ArrayList<>();
                for (String kw : splitKw) {
                    String clean = kw.trim().toUpperCase();
                    if (!clean.isEmpty()) {
                        cleanList.add(clean);
                    }
                }
                if (!cleanList.isEmpty()) {
                    targetMap.put(category, cleanList);
                }
            }
        }
    }

    /**
     * Categorizes an expense description using priority rules and keyword matching.
     */
    public String categorize(String description) {
        checkAndReloadIfModified();

        if (description == null || description.isBlank()) {
            return "Miscellaneous / Extra";
        }

        String normalized = description.toUpperCase().replaceAll("[^A-Z0-9 ]", " ").replaceAll("\\s+", " ").trim();
        String paddedDesc = " " + normalized + " ";

        // Priority 1: Inflow / Reimbursements
        if (paddedDesc.contains(" GAVE ")) {
            return "Inflow & Reimbursements";
        }

        // Priority 2: Direct Investment keywords
        if (paddedDesc.contains(" CHITTI ") || paddedDesc.contains(" CHIT ") ||
            paddedDesc.contains(" SIP ") || paddedDesc.contains(" MUTUAL FUND ")) {
            return "Investments & Savings";
        }

        // Pass 1: Strict word-boundary match (e.g., " AUTO ", " CAB ")
        for (Map.Entry<String, List<String>> entry : rulesMap.entrySet()) {
            String category = entry.getKey();
            for (String kw : entry.getValue()) {
                String cleanKw = kw.toUpperCase().trim();
                if (!cleanKw.isEmpty() && paddedDesc.contains(" " + cleanKw + " ")) {
                    return category;
                }
            }
        }

        // Pass 2: Loose substring match fallback (e.g., "BIG BASKET" inside "Big basket item")
        for (Map.Entry<String, List<String>> entry : rulesMap.entrySet()) {
            String category = entry.getKey();
            for (String kw : entry.getValue()) {
                String cleanKw = kw.toUpperCase().trim();
                if (!cleanKw.isEmpty() && normalized.contains(cleanKw)) {
                    return category;
                }
            }
        }

        return "Miscellaneous / Extra";
    }

    public boolean isInvestment(String category) {
        return "Investments & Savings".equalsIgnoreCase(category) || "Investments".equalsIgnoreCase(category);
    }

    public Map<String, List<String>> getCurrentRules() {
        checkAndReloadIfModified();
        return Collections.unmodifiableMap(rulesMap);
    }

    public String getRawConfigContent() throws IOException {
        File file = new File(configFilePath);
        if (!file.exists()) {
            createDefaultConfigFile(file);
        }
        return Files.readString(Path.of(configFilePath), StandardCharsets.UTF_8);
    }

    private void loadEmbeddedDefaults() {
        Map<String, List<String>> map = new HashMap<>();

        map.put("Food & Dining", List.of(
                "SNACKS", "LUNCH", "DINNER", "ROTI", "TEA", "JUICE", "TIFFIN", "PIZZA",
                "SUBWAY", "RESTAURANT", "FOOD", "KUNAFA", "BADAM MILK", "BUN", "CHAPATI",
                "SANTHOSH DABA", "CHILLS", "PANI PURI", "PANIPURI", "CHIPS", "SODA",
                "ZOMOTO", "ZOMATO", "KANCHI", "MNGALU", "PAN"
        ));
        map.put("Groceries & Essentials", List.of(
                "ZEPTO", "BIG BASKET", "BIGBASKET", "JIOMART", "JIO MART", "BLINKIT",
                "KPN", "DMART", "RELIANCE", "SMART", "VEGETABLE", "VEGETABLES", "VEGGIES",
                "FRUITS", "FRIUTS", "MANGOES", "EGGS", "MILK", "CURD", "DRYFRUITS",
                "COCONUT WATER", "COCONUT", "CORN", "GROCERIES", "NOW"
        ));
        map.put("Travel & Commute", List.of(
                "AUTO", "RAPIDO", "CAB", "CABS", "UBER", "BUS", "METRO", "TRAIN",
                "TICKETS", "TICKET", "PETROL", "PARKING", "AIR", "BIKE", "TRAVEL"
        ));
        map.put("Medical & Healthcare", List.of(
                "DOCTOR", "MEDICINE", "MEDICINES", "MEDICALS", "TEST", "TABLETS",
                "RAJA TEST", "APOLLO", "INSURANCE"
        ));
        map.put("Utilities & Bills", List.of(
                "AIRTEL", "POWER BILL", "ELECTRIC BILL", "WATER BILL", "WIFI",
                "MINUTES", "MINTUNES", "DUSTBIN", "DISTURB CHARGES", "WASTE BILL", "GAS"
        ));
        map.put("Household Furniture & Appliances", List.of(
                "MATTERS", "MATTRESS", "PURIFIER", "BLENDER", "FANS+CHAIR", "GEYSER", "WASHING MACHINE"
        ));
        map.put("Household Maintenance & Services", List.of(
                "DIPPERS", "DIAPERS", "DEEP CLEANING", "PLUMBER", "DOOR REPAIR",
                "SCOOTY VELDING", "BIKE REPAIR", "PHONE REPAIR", "THREAD", "THREADS",
                "PARCEL", "WATER", "WATERBOTTLE", "GLASS", "PLANT", "PACKING",
                "THINGS TO HOME", "DUSTBIN CHARGES", "URBAN"
        ));
        map.put("Government & Documentation", List.of(
                "PASSPORT PHOTO", "AADHAR", "XEROX"
        ));
        map.put("Personal Care & Grooming", List.of(
                "HAIRCUT", "HAIR CUT", "TRIMMING", "MEHENDI", "HENNA", "VILVA", "VILAVA", "CHAPPALS"
        ));
        map.put("Religious, Festive & Ceremonial", List.of(
                "DONATION", "ABHISHAKAM", "POOJA", "GANESH IDOL", "AMMAVARI KADIYALU",
                "FLOWERS", "RAKHI", "TEMPLE", "SHIRIDI", "TRIYAMBAKESHWAR", "ORPHANAGE"
        ));
        map.put("Entertainment & Outings", List.of(
                "MOVIE", "POPCORN", "GO KARTING", "STATUE OF EQUALITY", "BIRTHDAY CELEBRATION"
        ));
        map.put("Shopping & E-Commerce", List.of(
                "AMAZON", "FLIPKART", "MEESHO", "MESHO", "DECATHLON", "SHOPPING", "DOMINS"
        ));
        map.put("Gifts & Social", List.of(
                "GIFT", "MARRIAGE GIFT", "OFFICE GIFT", "GIFT GAMES"
        ));
        map.put("Taxes & Fees", List.of(
                "TAX", "GIFT CARD FEE", "TIP"
        ));
        map.put("Investments & Savings", List.of(
                "CHITTI", "GOLD CHITTI", "MUTUAL FUND", "SIP", "ZERODHA", "GROWW",
                "NPS", "PPF", "SECURITIES", "CHIT"
        ));
        map.put("Transfers & Personal", List.of(
                "SENT", "SAMPATH", "SRAVYA", "PRASHANTHI"
        ));
        map.put("Inflow & Reimbursements", List.of(
                "GAVE"
        ));
        map.put("Miscellaneous / Extra", List.of(
                "EXTRA"
        ));

        rulesMap.clear();
        rulesMap.putAll(map);
    }

    private void createDefaultConfigFile(File file) {
        loadEmbeddedDefaults();
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, rulesMap);
            this.lastModifiedTime = file.lastModified();
        } catch (IOException e) {
            System.err.println("Could not create default config file: " + e.getMessage());
        }
    }
}