package tn.dhc.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import tn.dhc.entities.Fiche;
import tn.dhc.entities.Medicament;
import tn.dhc.entities.Ordonnance;
import tn.dhc.entities.User;
import tn.dhc.utils.OrdonnancePdfExporter;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class SignatureApiService {

    public record SignatureLaunchResult(String envelopeId, String ceremonyUrl, String status) {}
    public record DeliverableStatus(String status, String deliverableUrl) {}

    private static final String API_BASE = "https://api.signatureapi.com/v1";
    private static final String APP_SIGNATURE_API_KEY = "key_test_3zHaWTmYjkeZ6iePTklxk01pIWL0JWt4JVta4gpdva4S";

    private final HttpClient http = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();
    private final String apiKey;

    public SignatureApiService() {
        this.apiKey = APP_SIGNATURE_API_KEY;
    }

    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }

    public SignatureLaunchResult launchOrdonnanceSignature(Ordonnance ordonnance, Fiche fiche, User patient,
                                                           User medecin, List<Medicament> medicaments)
            throws IOException, InterruptedException {
        ensureConfigured();
        Path tmpPdf = Files.createTempFile("ordonnance-sign-", ".pdf");
        OrdonnancePdfExporter.write(ordonnance, fiche, patient, medecin, medicaments, tmpPdf);
        String uploadUrl = createUpload(tmpPdf);
        Map<String, Object> payload = Map.of(
                "title", "Ordonnance " + ordonnance.getId(),
                "message", "Veuillez signer l'ordonnance.",
                "documents", List.of(Map.of(
                        "title", "Ordonnance médicale",
                        "url", uploadUrl,
                        "format", "pdf",
                        "places", List.of(Map.of(
                                "key", "doctor_signature",
                                "type", "signature",
                                "recipient_key", "doctor_signer"
                        )),
                        "fixed_positions", List.of(Map.of(
                                "place_key", "doctor_signature",
                                "page", 1,
                                "top", 120,
                                "left", 360
                        ))
                )),
                "recipients", List.of(Map.of(
                        "type", "signer",
                        "key", "doctor_signer",
                        "name", safeName(medecin),
                        "email", safeEmail(medecin),
                        "delivery_type", "none",
                        "ceremony", Map.of(
                                "authentication", List.of(Map.of(
                                        "type", "custom",
                                        "provider", "DHC",
                                        "data", Map.of(
                                                "ordonnance_id", String.valueOf(ordonnance.getId()),
                                                "doctor_id", medecin != null ? String.valueOf(medecin.getId()) : "0"
                                        )
                                ))
                        )
                ))
        );
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE + "/envelopes"))
                .header("Content-Type", "application/json")
                .header("X-API-Key", apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(payload)))
                .build();
        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("Signature API envelope error: " + response.statusCode() + " -> " + response.body());
        }
        JsonNode root = mapper.readTree(response.body());
        String envelopeId = root.path("id").asText(null);
        String status = root.path("status").asText(null);
        String ceremonyUrl = null;
        JsonNode recipients = root.path("recipients");
        if (recipients.isArray() && !recipients.isEmpty()) {
            ceremonyUrl = recipients.get(0).path("ceremony").path("url").asText(null);
        }
        return new SignatureLaunchResult(envelopeId, ceremonyUrl, status);
    }

    public DeliverableStatus fetchDeliverableStatus(String envelopeId) throws IOException, InterruptedException {
        ensureConfigured();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE + "/envelopes/" + envelopeId + "/deliverables"))
                .header("X-API-Key", apiKey)
                .GET()
                .build();
        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("Signature API deliverables fetch error: " + response.statusCode() + " -> " + response.body());
        }
        JsonNode root = mapper.readTree(response.body());
        JsonNode data = root.path("data");
        if (!data.isArray() || data.isEmpty()) {
            return new DeliverableStatus("pending", null);
        }
        JsonNode best = data.get(data.size() - 1);
        String status = best.path("status").asText(null);
        String url = best.path("url").asText(null);
        return new DeliverableStatus(status, url);
    }

    public void downloadSignedPdf(String signedUrl, Path outputPath) throws IOException, InterruptedException {
        byte[] bytes = downloadSignedPdfBytes(signedUrl);
        Files.write(outputPath, bytes);
    }

    public byte[] downloadSignedPdfBytes(String signedUrl) throws IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(signedUrl))
                .GET();
        if (isConfigured()) {
            builder.header("X-API-Key", apiKey);
        }
        HttpRequest request = builder.build();
        HttpResponse<byte[]> response = http.send(request, HttpResponse.BodyHandlers.ofByteArray());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("Download signed PDF failed: " + response.statusCode());
        }
        return response.body();
    }

    private String createUpload(Path pdfPath) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE + "/uploads"))
                .header("Content-Type", "application/pdf")
                .header("X-API-Key", apiKey)
                .POST(HttpRequest.BodyPublishers.ofFile(pdfPath))
                .build();
        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("Signature API upload error: " + response.statusCode() + " -> " + response.body());
        }
        JsonNode node = mapper.readTree(response.body());
        String url = node.path("url").asText(null);
        if (url == null || url.isBlank()) {
            throw new IOException("Upload URL missing in SignatureAPI response.");
        }
        return url;
    }

    private static String safeName(User u) {
        if (u == null) {
            return "Médecin";
        }
        return (u.getPrenom() != null ? u.getPrenom().trim() : "")
                + " "
                + (u.getNom() != null ? u.getNom().trim() : "");
    }

    private static String safeEmail(User u) {
        if (u == null || u.getMail() == null || u.getMail().isBlank()) {
            return "doctor@example.com";
        }
        return u.getMail().trim();
    }

    private void ensureConfigured() {
        if (!isConfigured()) {
            throw new IllegalStateException("SIGNATURE_API_KEY is not configured.");
        }
    }
}
