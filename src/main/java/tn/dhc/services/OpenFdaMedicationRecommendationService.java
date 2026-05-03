package tn.dhc.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import tn.dhc.entities.Medicament;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class OpenFdaMedicationRecommendationService {

    private static final String OPEN_FDA_ENDPOINT = "https://api.fda.gov/drug/label.json";

    private static final Map<String, List<String>> FALLBACK_BY_ILLNESS = Map.ofEntries(
            Map.entry("grippe", List.of("oseltamivir", "paracetamol", "ibuprofen")),
            Map.entry("rhume", List.of("paracetamol", "ibuprofen", "cetirizine")),
            Map.entry("bronchite", List.of("amoxicillin", "azithromycin", "salbutamol")),
            Map.entry("angine", List.of("amoxicillin", "paracetamol")),
            Map.entry("gastro", List.of("ondansetron", "loperamide", "oral rehydration")),
            Map.entry("migraine", List.of("sumatriptan", "ibuprofen", "paracetamol")),
            Map.entry("allergie", List.of("cetirizine", "loratadine", "fexofenadine")),
            Map.entry("infection urinaire", List.of("nitrofurantoin", "fosfomycin", "ciprofloxacin")),
            Map.entry("hypertension", List.of("amlodipine", "losartan", "lisinopril")),
            Map.entry("diabete", List.of("metformin", "insulin")),
            Map.entry("asthme", List.of("salbutamol", "budesonide", "montelukast")),
            Map.entry("covid", List.of("paracetamol", "ibuprofen")),
            Map.entry("fever", List.of("paracetamol", "ibuprofen", "aspirin")),
            Map.entry("fièvre", List.of("paracetamol", "ibuprofen", "aspirin")),
            Map.entry("fievre", List.of("paracetamol", "ibuprofen", "aspirin")),
            Map.entry("douleur", List.of("paracetamol", "ibuprofen", "diclofenac")),
            Map.entry("pain", List.of("paracetamol", "ibuprofen", "diclofenac")),
            Map.entry("toux", List.of("salbutamol", "cetirizine", "montelukast")),
            Map.entry("cough", List.of("salbutamol", "cetirizine", "montelukast"))
    );

    private final HttpClient http = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    public List<Integer> recommendMedicationIdsForIllness(String illness, List<Medicament> available) {
        if (illness == null || illness.isBlank() || available == null || available.isEmpty()) {
            return List.of();
        }
        Set<String> fdaTerms = fetchTermsFromOpenFda(illness);
        if (fdaTerms.isEmpty()) {
            fdaTerms.addAll(fallbackTerms(illness));
        }
        Map<Integer, Integer> scoreById = new HashMap<>();
        for (Medicament m : available) {
            String name = safe(m.getNomMedicament());
            String cat = safe(m.getCategorie());
            int score = 0;
            for (String term : fdaTerms) {
                if (term.length() < 4) {
                    continue;
                }
                if (name.contains(term) || term.contains(name)) {
                    score += 5;
                } else if (name.replace("-", " ").contains(term.replace("-", " "))) {
                    score += 4;
                }
                if (!cat.isBlank() && (cat.contains(term) || term.contains(cat))) {
                    score += 2;
                }
            }
            if (score > 0) {
                scoreById.put(m.getId(), score);
            }
        }

        List<Map.Entry<Integer, Integer>> ranked = new ArrayList<>(scoreById.entrySet());
        ranked.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));

        List<Integer> out = new ArrayList<>();
        for (Map.Entry<Integer, Integer> e : ranked) {
            out.add(e.getKey());
            if (out.size() >= 6) {
                break;
            }
        }
        return out;
    }

    private Set<String> fetchTermsFromOpenFda(String illness) {
        Set<String> terms = new HashSet<>();
        try {
            String token = openFdaIllnessToken(illness);
            if (token.isBlank()) {
                return terms;
            }
            String q = "(indications_and_usage:" + token + " OR purpose:" + token + ")";
            String key = OpenFdaApiKey.resolve();
            String url = OPEN_FDA_ENDPOINT + "?search=" + URLEncoder.encode(q, StandardCharsets.UTF_8) + "&limit=20";
            if (!key.isBlank()) {
                url += "&api_key=" + URLEncoder.encode(key, StandardCharsets.UTF_8);
            }
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() < 200 || resp.statusCode() >= 300) {
                return terms;
            }
            JsonNode root = mapper.readTree(resp.body());
            JsonNode results = root.path("results");
            if (!results.isArray()) {
                return terms;
            }
            for (JsonNode r : results) {
                JsonNode openfda = r.path("openfda");
                addArrayValues(openfda.path("generic_name"), terms);
                addArrayValues(openfda.path("brand_name"), terms);
                addArrayValues(openfda.path("substance_name"), terms);
                addArrayValues(r.path("active_ingredient"), terms);
            }
        } catch (IOException | InterruptedException ignored) {
        }
        return normalizeTerms(terms);
    }

    /** Premier mot alphanumérique pour Lucene — évite guillemets et {@code +OR+} (mal encodé). */
    private static String openFdaIllnessToken(String raw) {
        if (raw == null) {
            return "";
        }
        String s = Normalizer.normalize(raw.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT);
        s = s.replaceAll("[^a-z0-9]+", " ").trim().replaceAll("\\s+", " ");
        if (s.isBlank()) {
            return "";
        }
        String first = s.contains(" ") ? s.substring(0, s.indexOf(' ')) : s;
        if (first.length() < 3 || first.length() > 60) {
            return "";
        }
        return first;
    }

    private static void addArrayValues(JsonNode arr, Set<String> out) {
        if (!arr.isArray()) {
            return;
        }
        for (JsonNode n : arr) {
            if (n != null && n.isTextual()) {
                out.add(n.asText());
            }
        }
    }

    private static Set<String> normalizeTerms(Set<String> raw) {
        Set<String> out = new HashSet<>();
        for (String x : raw) {
            if (x == null) {
                continue;
            }
            String cleaned = x.toLowerCase(Locale.ROOT)
                    .replaceAll("[^a-z0-9\\-\\s]", " ")
                    .trim();
            if (cleaned.isBlank()) {
                continue;
            }
            for (String token : cleaned.split("\\s+")) {
                if (token.length() >= 4) {
                    out.add(token);
                }
            }
            if (cleaned.length() >= 4) {
                out.add(cleaned);
            }
        }
        return out;
    }

    private static Set<String> fallbackTerms(String illness) {
        String key = safe(illness);
        for (Map.Entry<String, List<String>> e : FALLBACK_BY_ILLNESS.entrySet()) {
            if (key.contains(e.getKey())) {
                return new HashSet<>(e.getValue());
            }
        }
        // Default symptom-safe fallback to avoid empty recommendations.
        if (key.contains("fever") || key.contains("fièvre") || key.contains("fievre")) {
            return new HashSet<>(List.of("paracetamol", "ibuprofen", "aspirin"));
        }
        return new HashSet<>();
    }

    private static String safe(String s) {
        return s == null ? "" : s.trim().toLowerCase(Locale.ROOT);
    }
}
