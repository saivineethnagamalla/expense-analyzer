package com.example.expenseanalyzer.service;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CategorizerService {

    private static final Map<String, List<String>> CATEGORY_RULES = new HashMap<>();

    static {
        CATEGORY_RULES.put("Food & Dining", List.of("SNACKS", "LUNCH", "DINNER", "ROTI", "TEA", "JUICE", "TIFFIN"));
        CATEGORY_RULES.put("Travel & Commute", List.of("AUTO", "RAPIDO", "BUS", "METRO", "TRAIN", "TICKETS"));
        CATEGORY_RULES.put("Medical & Healthcare", List.of("DOCTOR", "MEDICINE", "MEDICALS", "TEST"));
        CATEGORY_RULES.put("Groceries & Quick Commerce", List.of("ZEPTO", "KPN", "VEGETABLES", "STORE"));
        CATEGORY_RULES.put("Shopping & E-Commerce", List.of("FLIPKART", "AMAZON", "GIFT CARD"));
        CATEGORY_RULES.put("Utilities & Bills", List.of("AIRTEL", "POWER BILL", "BILL", "MINUTES"));
        CATEGORY_RULES.put("Household & Personal Care", List.of("DIAPERS", "HAIR CUT", "FLOWERS", "XEROX", "PARCEL", "WATER"));
        CATEGORY_RULES.put("Religious & Donations", List.of("DONATION", "ABHISHAKAM", "POOJA"));
        // Investment category definitions
        CATEGORY_RULES.put("Investments", List.of("MUTUAL FUND", "SIP", "ZERODHA", "GROWW", "NPS", "PPF", "SECURITIES", "CHIT"));
        // Peer / Transfers
        CATEGORY_RULES.put("Transfers & Personal", List.of("SENT", "SAMPATH"));
    }

    public String categorize(String description) {
        String upper = description.toUpperCase();

        for (Map.Entry<String, List<String>> entry : CATEGORY_RULES.entrySet()) {
            for (String keyword : entry.getValue()) {
                if (upper.contains(keyword)) {
                    return entry.getKey();
                }
            }
        }
        return "Miscellaneous / Extra";
    }

    public boolean isInvestment(String category) {
        return "Investments".equalsIgnoreCase(category);
    }
}