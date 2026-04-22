package tn.dhc.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

public class CohereEventDescriptionService {

    private static final String API_KEY = "ohOFqRtH0C38Up2Lep7zJkFQ1L6X5kc5DOaGJHgP";
    private static final String API_URL = "https://api.cohere.com/v2/chat";
    private static final String API_URL_V1 = "https://api.cohere.com/v1/generate";
    private static final String MODEL = "command-r-plus";
    private static final String MODEL_V1 = "command";
    private static final int MAX_EVENT_DESCRIPTION = 240;

    private final HttpClient http = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    public String generateDescriptionFromTitle(String title) {
        String cleanedTitle = title == null ? "" : title.trim();
        if (cleanedTitle.isBlank()) {
            return "";
        }
        try {
            String v2 = generateViaV2Chat(cleanedTitle);
            if (v2 != null && !v2.isBlank()) {
                return clamp(v2);
            }
        } catch (Exception ignored) {
        }
        try {
            String v1 = generateViaV1(cleanedTitle);
            if (v1 != null && !v1.isBlank()) {
                return clamp(v1);
            }
        } catch (Exception ignored) {
        }
        return clamp(localFallback(cleanedTitle));
    }

    private String generateViaV2Chat(String cleanedTitle) throws IOException, InterruptedException {

        String prompt = """
                Tu es assistant d'un systeme medical.
                Genere une description d'evenement concise en francais (2 a 4 phrases), professionnelle et claire.
                Titre: "%s"
                La description doit etre directement utilisable dans un formulaire d'evenement.
                """.formatted(cleanedTitle);

        Map<String, Object> payload = Map.of(
                "model", MODEL,
                "temperature", 0.5,
                "messages", new Object[]{
                        Map.of("role", "user", "content", prompt)
                }
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Authorization", "Bearer " + API_KEY)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(payload)))
                .build();

        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("Cohere HTTP " + response.statusCode() + " -> " + response.body());
        }

        JsonNode root = mapper.readTree(response.body());
        JsonNode content = root.path("message").path("content");
        if (content.isArray() && !content.isEmpty()) {
            JsonNode first = content.get(0);
            String txt = first.path("text").asText("");
            if (!txt.isBlank()) {
                return txt.trim();
            }
        }
        return root.path("text").asText("").trim();
    }

    private String generateViaV1(String cleanedTitle) throws IOException, InterruptedException {
        String prompt = """
                Redige une description d'evenement medical en francais (2 a 4 phrases), claire et professionnelle.
                Titre: "%s"
                Description:
                """.formatted(cleanedTitle);
        Map<String, Object> payload = Map.of(
                "model", MODEL_V1,
                "prompt", prompt,
                "max_tokens", 180,
                "temperature", 0.6
        );
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL_V1))
                .header("Authorization", "Bearer " + API_KEY)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(payload)))
                .build();
        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("Cohere v1 HTTP " + response.statusCode() + " -> " + response.body());
        }
        JsonNode root = mapper.readTree(response.body());
        JsonNode generations = root.path("generations");
        if (generations.isArray() && !generations.isEmpty()) {
            return generations.get(0).path("text").asText("").trim();
        }
        return "";
    }

    private String localFallback(String title) {
        return "Rejoignez-nous pour \"" + title + "\" afin de sensibiliser le public et partager des conseils pratiques avec des professionnels de sante. "
                + "Cet evenement propose des echanges utiles, des recommandations de prevention et un accompagnement adapte aux participants. "
                + "Nous vous invitons a participer nombreux pour renforcer la culture de prevention et de bien-etre.";
    }

    private static String clamp(String text) {
        if (text == null) {
            return "";
        }
        String t = text.trim().replace('\n', ' ').replace('\r', ' ');
        if (t.length() <= MAX_EVENT_DESCRIPTION) {
            return t;
        }
        return t.substring(0, MAX_EVENT_DESCRIPTION - 3).trim() + "...";
    }
}
