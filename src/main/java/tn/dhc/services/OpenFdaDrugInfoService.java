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
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OpenFdaDrugInfoService {

    public record DrugInfo(String dosage, String category, String forme) {}

    private static final String OPEN_FDA_ENDPOINT = "https://api.fda.gov/drug/label.json";
    private static final int SECTION_MAX_CHARS = 6000;

    private final HttpClient http = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    public DrugInfo fetchByMedicationName(String medicationName) throws IOException, InterruptedException {
        String med = medicationName == null ? "" : medicationName.trim();
        if (med.isBlank()) {
            throw new IllegalArgumentException("Nom du médicament vide.");
        }
        List<String> queries = buildQueries(med);
        JsonNode first = null;
        IOException lastError = null;
        for (String q : queries) {
            try {
                JsonNode maybe = queryFirstResult(q);
                if (maybe != null) {
                    first = maybe;
                    break;
                }
            } catch (IOException ex) {
                lastError = ex;
            }
        }
        if (first == null) {
            if (lastError != null) {
                // Network/API issues -> still return default values so UI continues.
                return new DrugInfo("500 mg", "Médicament", "Comprimé");
            }
            return new DrugInfo("500 mg", "Médicament", "Comprimé");
        }

        JsonNode openfda = first.path("openfda");

        String dosageRaw = firstText(first, "dosage_and_administration");
        String dosage = normalizeDosage(dosageRaw);
        if (dosage.isBlank()) {
            String route = firstText(openfda, "route");
            dosage = route.isBlank() ? "500 mg" : route;
        }

        String category = firstText(openfda, "pharm_class_epc");
        if (category.isBlank()) {
            category = firstText(openfda, "product_type");
        }
        if (category.isBlank()) {
            category = "Médicament";
        }
        category = cleanupCategory(category);

        String forme = firstText(openfda, "dosage_form");
        if (forme.isBlank()) {
            forme = firstText(openfda, "route");
        }
        if (forme.isBlank()) {
            forme = "Comprimé";
        }
        forme = normalizeForme(forme);

        return new DrugInfo(dosage, category, forme);
    }

    /**
     * Récupère depuis l'étiquetage FDA (openFDA) les sections utiles pour le patient : effets indésirables,
     * mises en garde, contre-indications, interactions (texte brut nettoyé du HTML).
     */
    public String fetchSideEffectsAndWarningsReport(String medicationName) throws IOException, InterruptedException {
        String med = medicationName == null ? "" : medicationName.trim();
        if (med.isBlank()) {
            throw new IllegalArgumentException("Nom du médicament vide.");
        }
        List<String> queries = buildQueries(med);
        JsonNode first = null;
        IOException lastError = null;
        for (String q : queries) {
            try {
                JsonNode maybe = queryFirstResult(q);
                if (maybe != null) {
                    first = maybe;
                    break;
                }
            } catch (IOException ex) {
                lastError = ex;
            }
        }
        if (first == null) {
            if (lastError != null) {
                throw lastError;
            }
            return "Aucune notice FDA trouvée pour « " + med + " ».\n"
                    + "Essayez le nom générique international (ex. paracetamol, ibuprofen) ou le nom de marque US.\n"
                    + "Source : openFDA drug/label.";
        }

        StringBuilder out = new StringBuilder();
        out.append("─── ").append(buildProductTitle(first)).append(" ───\n\n");
        out.append("(Informations issues des données publiques FDA / openFDA — usage indicatif, ne remplace pas votre médecin ou la notice officielle.)\n\n");

        appendLabelSection(out, "Mise en garde encadrée", first, "boxed_warning");
        appendLabelSection(out, "Avertissements et précautions", first, "warnings_and_cautions");
        appendLabelSection(out, "Effets indésirables (adverse reactions)", first, "adverse_reactions");
        appendLabelSection(out, "Contre-indications", first, "contraindications");
        appendLabelSection(out, "Interactions médicamenteuses", first, "drug_interactions");
        appendLabelSection(out, "Précautions d'emploi", first, "precautions");

        if (out.length() < 400) {
            out.append("\n---\nLes champs détaillés sont peu renseignés pour ce produit dans openFDA. Consultez la notice ou votre pharmacien.\n");
        }
        return out.toString().trim();
    }

    private static String buildProductTitle(JsonNode result) {
        JsonNode of = result.path("openfda");
        String brand = joinOpenfdaValues(of, "brand_name", 120);
        String gen = joinOpenfdaValues(of, "generic_name", 120);
        if (!brand.isBlank() && !gen.isBlank()) {
            return brand + " (" + gen + ")";
        }
        if (!brand.isBlank()) {
            return brand;
        }
        if (!gen.isBlank()) {
            return gen;
        }
        return "Produit (openFDA)";
    }

    private static String joinOpenfdaValues(JsonNode openfda, String field, int max) {
        JsonNode arr = openfda.path(field);
        if (!arr.isArray() || arr.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (JsonNode n : arr) {
            if (n != null && n.isTextual()) {
                if (sb.length() > 0) {
                    sb.append(" · ");
                }
                sb.append(n.asText(""));
            }
        }
        String s = stripHtml(sb.toString()).replaceAll("\\s+", " ").trim();
        return s.length() > max ? s.substring(0, max) + "…" : s;
    }

    private static void appendLabelSection(StringBuilder out, String title, JsonNode result, String fieldKey) {
        String body = joinLabelArray(result, fieldKey);
        if (body.isBlank()) {
            return;
        }
        out.append("▸ ").append(title).append("\n");
        out.append(body).append("\n\n");
    }

    private static String joinLabelArray(JsonNode result, String fieldKey) {
        JsonNode arr = result.path(fieldKey);
        if (!arr.isArray() || arr.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (JsonNode n : arr) {
            if (n != null && n.isTextual()) {
                sb.append(n.asText("")).append("\n");
            }
        }
        String cleaned = stripHtml(sb.toString()).trim();
        if (cleaned.isBlank()) {
            return "";
        }
        return cleaned.length() > SECTION_MAX_CHARS
                ? cleaned.substring(0, SECTION_MAX_CHARS) + "\n… (texte tronqué)"
                : cleaned;
    }

    private static String stripHtml(String raw) {
        if (raw == null || raw.isBlank()) {
            return "";
        }
        String s = raw
                .replaceAll("(?i)<br\\s*/?>", "\n")
                .replaceAll("(?i)</p>", "\n")
                .replaceAll("(?i)</div>", "\n")
                .replaceAll("(?i)</li>", "\n")
                .replaceAll("<[^>]+>", " ")
                .replace("&nbsp;", " ")
                .replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">");
        return s.replaceAll("[ \\t\\xA0]+", " ").replaceAll("\\n{3,}", "\n\n").trim();
    }

    /**
     * Requêtes openFDA drug/label : uniquement des champs {@code openfda.*}, termes sans guillemets.
     * Important : ne pas écrire {@code +OR+} dans la chaîne puis l'encoder avec {@link URLEncoder} —
     * les {@code +} littéraux deviennent {@code %2B} et Lucene voit des opérateurs invalides (HTTP 500
     * {@code parse_exception}). Utiliser des espaces autour de {@code OR} pour que {@code +} dans l'URL
     * représente bien des espaces une fois décodé.
     */
    private List<String> buildQueries(String med) {
        String phrase = sanitizeForOpenFdaPhrase(med);
        String folded = Normalizer.normalize(phrase, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT)
                .trim();
        String lettersOnly = folded.replaceAll("[^a-z0-9]+", " ").trim().replaceAll("\\s+", " ");
        if (lettersOnly.isBlank()) {
            lettersOnly = folded.replaceAll("[^a-z0-9]", "");
        }
        String compact = lettersOnly.replace(" ", "");
        String firstToken = lettersOnly.contains(" ") ? lettersOnly.substring(0, lettersOnly.indexOf(' ')) : lettersOnly;
        if (firstToken.length() > 64) {
            firstToken = firstToken.substring(0, 64);
        }
        if (compact.length() > 64) {
            compact = compact.substring(0, 64);
        }

        List<String> qs = new ArrayList<>();
        if (firstToken.length() >= 3) {
            qs.add("openfda.generic_name:" + firstToken + " OR openfda.brand_name:" + firstToken);
            qs.add("openfda.generic_name:" + firstToken);
            qs.add("openfda.brand_name:" + firstToken);
        }
        if (compact.length() >= 5 && !compact.equals(firstToken)) {
            qs.add("openfda.generic_name:" + compact + "* OR openfda.brand_name:" + compact + "*");
        }
        return qs;
    }

    /** Retire caractères qui cassent le parseur Lucene / les guillemets de la requête. */
    private static String sanitizeForOpenFdaPhrase(String raw) {
        if (raw == null) {
            return "";
        }
        String s = raw.trim()
                .replace('"', ' ')
                .replace('\\', ' ')
                .replace('+', ' ')
                .replace(':', ' ')
                .replace('(', ' ')
                .replace(')', ' ')
                .replace('*', ' ')
                .replace('?', ' ')
                .replace('!', ' ')
                .replace('[', ' ')
                .replace(']', ' ')
                .replace('{', ' ')
                .replace('}', ' ')
                .replace('^', ' ')
                .replace('~', ' ')
                .replace('|', ' ')
                .replace('&', ' ')
                .replace('/', ' ')
                .replace('<', ' ')
                .replace('>', ' ')
                .trim();
        s = s.replaceAll("\\s+", " ").trim();
        if (s.length() > 120) {
            s = s.substring(0, 120).trim();
        }
        return s;
    }

    private JsonNode queryFirstResult(String query) throws IOException, InterruptedException {
        String key = OpenFdaApiKey.resolve();
        String url = OPEN_FDA_ENDPOINT
                + "?search=" + URLEncoder.encode(query, StandardCharsets.UTF_8)
                + "&limit=1";
        if (!key.isBlank()) {
            url += "&api_key=" + URLEncoder.encode(key, StandardCharsets.UTF_8);
        }
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();
        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() == 404 && resp.body() != null && resp.body().contains("\"NOT_FOUND\"")) {
            return null; // no match for this query, try next
        }
        if (resp.statusCode() < 200 || resp.statusCode() >= 300) {
            throw new IOException("OpenFDA error " + resp.statusCode() + ": " + compact(resp.body()));
        }
        JsonNode root = mapper.readTree(resp.body());
        JsonNode results = root.path("results");
        if (!results.isArray() || results.isEmpty()) {
            return null;
        }
        return results.get(0);
    }

    private static String firstText(JsonNode node, String field) {
        JsonNode arr = node.path(field);
        if (arr.isArray() && !arr.isEmpty() && arr.get(0).isTextual()) {
            return arr.get(0).asText("");
        }
        return "";
    }

    private static String normalizeDosage(String dosageRaw) {
        if (dosageRaw == null || dosageRaw.isBlank()) {
            return "";
        }
        String lower = dosageRaw.toLowerCase(Locale.ROOT);
        Pattern p = Pattern.compile("(\\d+(?:[\\.,]\\d+)?)\\s?(mg|mcg|g|ml|iu)");
        Matcher m = p.matcher(lower);
        if (m.find()) {
            String n = m.group(1).replace(',', '.');
            String unit = m.group(2).toLowerCase(Locale.ROOT);
            return n + " " + unit;
        }
        String compact = lower.replaceAll("\\s+", " ").trim();
        return compact.length() > 32 ? compact.substring(0, 32) : compact;
    }

    private static String cleanupCategory(String s) {
        String x = s == null ? "" : s.replaceAll("\\[.*?\\]", "").trim();
        if (x.isBlank()) {
            return "Médicament";
        }
        return x.length() > 80 ? x.substring(0, 80) : x;
    }

    private static String normalizeForme(String s) {
        String x = s == null ? "" : s.trim();
        String l = x.toLowerCase(Locale.ROOT);
        if (l.contains("tablet")) return "Comprimé";
        if (l.contains("capsule")) return "Gélule";
        if (l.contains("injection")) return "Injection";
        if (l.contains("solution")) return "Solution";
        if (l.contains("suspension")) return "Suspension";
        if (l.contains("syrup")) return "Sirop";
        if (l.contains("inhal")) return "Inhalateur";
        return x.length() > 40 ? x.substring(0, 40) : x;
    }

    private static String compact(String body) {
        if (body == null) return "";
        return body.replaceAll("\\s+", " ").trim();
    }
}
