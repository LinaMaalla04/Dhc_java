package tn.dhc.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.Locale;

public class HuggingFaceIllnessService {

    private static final String API_KEY = "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";
    private static final List<String> MODEL_URLS = List.of(
            "https://router.huggingface.co/hf-inference/models/facebook/bart-large-mnli",
            "https://api-inference.huggingface.co/models/facebook/bart-large-mnli",
            "https://api-inference.huggingface.co/pipeline/zero-shot-classification/facebook/bart-large-mnli"
    );

    private static final List<String> CANDIDATE_ILLNESSES = List.of(
            "Grippe",
            "Rhume",
            "Bronchite",
            "Angine",
            "Gastro-entérite",
            "Migraine",
            "Allergie saisonnière",
            "Infection urinaire",
            "Hypertension",
            "Diabète",
            "Asthme",
            "COVID-19"
    );

    private final HttpClient http = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    public String predictIllness(String description) throws IOException, InterruptedException {
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Description vide.");
        }
        Map<String, Object> payload = Map.of(
                "inputs", description,
                "parameters", Map.of(
                        "candidate_labels", CANDIDATE_ILLNESSES
                )
        );
        String body = mapper.writeValueAsString(payload);
        IOException lastError = null;
        JsonNode root = null;
        for (String url : MODEL_URLS) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + API_KEY)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            int code = response.statusCode();
            if (code >= 200 && code < 300) {
                root = mapper.readTree(response.body());
                break;
            }
            String compact = response.body() == null ? "" : response.body().replaceAll("\\s+", " ").trim();
            lastError = new IOException("HF API error " + code + " on " + url + ": " + compact);
        }
        if (root == null) {
            return fallbackPrediction(description);
        }
        JsonNode labels = root.path("labels");
        if (!labels.isArray() || labels.isEmpty()) {
            return fallbackPrediction(description);
        }
        String predicted = labels.get(0).asText("");
        if (predicted == null || predicted.isBlank()) {
            return fallbackPrediction(description);
        }
        return predicted;
    }

    private static String fallbackPrediction(String description) {
        String s = description == null ? "" : description.toLowerCase(Locale.ROOT);
        if ((s.contains("fever") || s.contains("fièvre")) && (s.contains("cough") || s.contains("toux"))) {
            return "Grippe";
        }
        if (s.contains("headache") || s.contains("migraine") || s.contains("mal de tête")) {
            return "Migraine";
        }
        if (s.contains("allergy") || s.contains("allerg")) {
            return "Allergie saisonnière";
        }
        if (s.contains("asthma") || s.contains("asthme") || s.contains("wheez")) {
            return "Asthme";
        }
        if (s.contains("urine") || s.contains("urinary") || s.contains("brûlure")) {
            return "Infection urinaire";
        }
        if (s.contains("diarr") || s.contains("vomit") || s.contains("naus")) {
            return "Gastro-entérite";
        }
        if (s.contains("throat") || s.contains("gorge") || s.contains("angine")) {
            return "Angine";
        }
        if (s.contains("cold") || s.contains("rhume")) {
            return "Rhume";
        }
        return "Grippe";
    }
}
