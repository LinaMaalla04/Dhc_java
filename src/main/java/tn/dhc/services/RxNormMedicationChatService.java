package tn.dhc.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class RxNormMedicationChatService {

    private static final String RXNORM_URL = "https://rxnav.nlm.nih.gov/REST/drugs.json?name=";

    private final HttpClient http = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    public String answerMedicationQuestion(String medicationName) {
        String query = medicationName == null ? "" : medicationName.trim();
        if (query.isBlank()) {
            return "Veuillez saisir un nom de médicament (ex: ibuprofen).";
        }

        try {
            JsonNode root = fetchRxNorm(query);
            return formatRxNormResponse(query, root);
        } catch (Exception e) {
            return "Impossible de contacter RxNorm pour le moment : " + e.getMessage();
        }
    }

    private JsonNode fetchRxNorm(String medicationName) throws IOException, InterruptedException {
        String encoded = URLEncoder.encode(medicationName, StandardCharsets.UTF_8);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(RXNORM_URL + encoded))
                .GET()
                .build();
        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("RxNorm HTTP " + response.statusCode());
        }
        return mapper.readTree(response.body());
    }

    private String formatRxNormResponse(String query, JsonNode root) {
        JsonNode groups = root.path("drugGroup").path("conceptGroup");
        if (!groups.isArray() || groups.isEmpty()) {
            return "Aucun resultat RxNorm pour \"" + query + "\".\nEssayez un autre nom (generic ou brand).";
        }

        List<String> lines = new ArrayList<>();
        int totalConcepts = 0;
        lines.add("Resultats RxNorm pour: " + query);
        lines.add("");

        for (JsonNode group : groups) {
            String tty = group.path("tty").asText("");
            JsonNode concepts = group.path("conceptProperties");
            if (!concepts.isArray() || concepts.isEmpty()) {
                continue;
            }
            totalConcepts += concepts.size();
            lines.add("Type " + tty + " (" + concepts.size() + "):");

            int shown = 0;
            for (JsonNode c : concepts) {
                if (shown >= 6) {
                    lines.add("  - ...");
                    break;
                }
                String name = c.path("name").asText("");
                String synonym = c.path("synonym").asText("");
                String rxcui = c.path("rxcui").asText("");
                if (!name.isBlank()) {
                    if (!synonym.isBlank() && !synonym.equalsIgnoreCase(name)) {
                        lines.add("  - " + name + " | Synonyme: " + synonym + " | RXCUI: " + rxcui);
                    } else {
                        lines.add("  - " + name + " | RXCUI: " + rxcui);
                    }
                    shown++;
                }
            }
            lines.add("");
        }

        if (totalConcepts == 0) {
            return "Aucun detail exploitable trouve pour \"" + query + "\".";
        }

        lines.add("Note: ces donnees viennent de RxNorm et sont informatives, pas un avis medical.");
        return String.join("\n", lines);
    }
}
