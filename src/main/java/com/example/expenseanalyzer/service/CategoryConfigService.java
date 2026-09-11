package com.example.expenseanalyzer.service;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CategoryConfigService {

    private final Map<String, List<String>> rulesMap = new ConcurrentHashMap<>();

    public CategoryConfigService() {
        initializeRules();
    }

    private void initializeRules() {
        Map<String, List<String>> map = new LinkedHashMap<>();

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
                "PASSPORT PHOTO", "AADHAR", "AADHAAR", "XEROX"
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

        map.put("Investments", List.of(
                "MUTUAL FUND", "SIP", "ZERODHA", "GROWW", "NPS", "PPF", "SECURITIES",
                "CHITTI", "GOLD CHITTI", "CHIT", "CRYPTO", "STOCKS", "IPO", "ELSS", "RD"
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

        rulesMap.putAll(map);
    }

    public String categorize(String description) {
        if (description == null || description.isBlank()) {
            return "Miscellaneous / Extra";
        }

        String normalized = description.toUpperCase()
                .replaceAll("[^A-Z0-9 ]", " ")
                .replaceAll("\\s+", " ")
                .trim();
        String paddedDesc = " " + normalized + " ";

        // Priority 1: Inflows / Reimbursements
        if (paddedDesc.contains(" GAVE ")) {
            return "Inflow & Reimbursements";
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
        return "Investments".equalsIgnoreCase(category) || "Investments & Savings".equalsIgnoreCase(category);
    }

    public Map<String, List<String>> getCurrentRules() {
        return Collections.unmodifiableMap(rulesMap);
    }

    public void addKeyword(String category, String keyword) {
        rulesMap.computeIfAbsent(category, k -> new ArrayList<>())
                .add(keyword.trim().toUpperCase());
    }
}