package tn.dhc.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Vérifie un token reCAPTCHA v3 auprès de l'API Google.
 *
 * Flux :
 *  1. Le WebView exécute JS → obtient un token via grecaptcha.execute()
 *  2. Java appelle CaptchaService.verify(token)
 *  3. Google répond avec un score entre 0.0 (bot) et 1.0 (humain)
 *  4. On accepte si score >= MIN_SCORE
 */
public class CaptchaService {

    private static final String SECRET_KEY  = "6LePU8UsAAAAAOQo6XMRW5cD1QGHCa1bKlHO5OZl";
    private static final String VERIFY_URL  = "https://www.google.com/recaptcha/api/siteverify";
    private static final double MIN_SCORE   = 0.5; // seuil recommandé par Google

    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Vérifie le token reCAPTCHA v3.
     *
     * @param token  token reçu depuis le WebView
     * @return true si l'utilisateur est considéré humain (score >= 0.5)
     * @throws RuntimeException si l'appel réseau échoue
     */
    public boolean verify(String token) {
        if (token == null || token.isBlank()) return false;

        try {
            // ── Requête POST vers Google ──────────────────────────────────
            URL url = new URL(VERIFY_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            String body = "secret=" + SECRET_KEY + "&response=" + token;
            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.getBytes(StandardCharsets.UTF_8));
            }

            // ── Lire la réponse JSON ──────────────────────────────────────
            // Exemple de réponse :
            // { "success": true, "score": 0.9, "action": "register", "hostname": "localhost" }
            JsonNode json = mapper.readTree(conn.getInputStream());

            boolean success = json.path("success").asBoolean(false);
            double  score   = json.path("score").asDouble(0.0);

            System.out.printf("[reCAPTCHA v3] success=%b  score=%.2f%n", success, score);
            System.out.println("Score reCAPTCHA = " + score);
            return success && score >= MIN_SCORE;

        } catch (Exception e) {
            throw new RuntimeException("Échec de la vérification reCAPTCHA : " + e.getMessage(), e);
        }
    }
}
