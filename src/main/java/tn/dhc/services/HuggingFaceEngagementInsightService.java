package tn.dhc.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import tn.dhc.entities.User;
import tn.dhc.utils.EngagementReportHtmlFormatter;
import tn.dhc.utils.UserEngagementHeuristics;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Analyse d'engagement via Hugging Face (router OpenAI-compatible).
 * La clé est uniquement celle définie ci-dessous dans le code (aucune variable d'environnement).
 */
public class HuggingFaceEngagementInsightService {

    private static final String API_URL = "https://router.huggingface.co/v1/chat/completions";
    /**
     * Token Hugging Face (read / Inference). Remplacez ici si vous en créez un nouveau.
     * Pas de lecture HF_TOKEN / propriétés JVM : évite qu’une vieille clé système écrase celle du projet.
     */
    private static final String HUGGING_FACE_API_KEY = "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";

    /** Modèles avec routage :fastest puis variantes sans suffixe. */
    private static final String[] MODELS_TO_TRY = {
            "meta-llama/Llama-3.2-3B-Instruct:fastest",
            "meta-llama/Meta-Llama-3.1-8B-Instruct:fastest",
            "mistralai/Mistral-7B-Instruct-v0.3:fastest",
            "Qwen/Qwen2.5-7B-Instruct:fastest",
            "meta-llama/Llama-3.2-3B-Instruct",
            "mistralai/Mistral-7B-Instruct-v0.3"
    };

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();
    private final ObjectMapper mapper = new ObjectMapper();

    public String analyzeEngagementFromUsers(List<User> users) {
        if (users == null || users.isEmpty()) {
            return "Aucun utilisateur à analyser.";
        }
        String apiKey = resolveApiKeyOnlyFromCode();
        if (apiKey.isBlank()) {
            return buildAiUnavailablePreamble(true, null)
                    + buildLocalEngagementReport(users);
        }

        String deterministic = buildDeterministicSegmentationMd(users);

        String system = """
                Tu es consultant produit pour une application santé (DHC) réservée aux administrateurs.
                Tu reçois un **résumé chiffré et des définitions de segments déjà calculés** par l'application (fiables).
                INTERDICTIONS strictes :
                - Ne produis **aucune** section « Segmentation des utilisateurs », **aucune** liste d'utilisateurs par profil, **aucun** reclassement.
                - Ne contredis **jamais** les effectifs ni les règles fournis (la récence / jours depuis dernière connexion prime sur le volume de connexions).
                Tâche unique :
                - Rédige **uniquement** les deux rubriques Markdown suivantes, dans cet ordre, avec titres exacts :
                ### Prédictions
                (3 à 5 phrases courtes : churn à 30 jours et 90 jours en ordre de grandeur, priorisation des relances, cohérent avec les chiffres fournis.)
                ### Conseils concrets pour l'Équipe Admin
                (liste à puces : communication, relances, personnalisation selon rôle patient/médecin/admin, RGPD, analyse des données.)
                Style : titres ###, listes à tiret (-), **gras** pour les idées clés. Réponds en français, ton synthétique.
                """;

        final String userMsg = buildAiNarrativeContext(users)
                + """

                Consigne : produis seulement les deux sections demandées (Prédictions, puis Conseils). Rien d'autre.""";

        IOException lastIo = null;
        for (String model : MODELS_TO_TRY) {
            try {
                String raw = callChat(apiKey, model, system, userMsg);
                String ia = EngagementReportHtmlFormatter.replaceUserRefsInPlainText(raw, users);
                return deterministic + "\n\n---\n\n## Analyse qualitative (IA)\n\n" + ia;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return buildAiUnavailablePreamble(false, null) + buildLocalEngagementReport(users);
            } catch (IOException ex) {
                lastIo = ex;
            }
        }
        return buildAiUnavailablePreamble(false, lastIo)
                + buildLocalEngagementReport(users);
    }

    /** Uniquement la constante {@link #HUGGING_FACE_API_KEY} (sanitisée). */
    private static String resolveApiKeyOnlyFromCode() {
        return sanitizeKey(HUGGING_FACE_API_KEY);
    }

    private static String sanitizeKey(String key) {
        if (key == null) {
            return "";
        }
        return key.trim()
                .replace("\uFEFF", "")
                .replace("\r", "")
                .replace("\n", "");
    }

    private static String buildAiUnavailablePreamble(boolean missingKey, IOException lastError) {
        StringBuilder sb = new StringBuilder();
        sb.append("⚠️ Hugging Face (IA) : ");
        if (missingKey) {
            sb.append("clé absente dans le code (constante HUGGING_FACE_API_KEY vide).\n");
        } else {
            sb.append("appel impossible après essai de plusieurs modèles (quota, modèle, réseau ou clé refusée).\n");
        }
        sb.append("• Vérifiez le token sur https://huggingface.co/settings/tokens (droits lecture + appels Inference / Inference Providers).\n");
        sb.append("• Mettez à jour la constante HUGGING_FACE_API_KEY dans HuggingFaceEngagementInsightService.java (seule source utilisée par l’app).\n");
        if (lastError != null && lastError.getMessage() != null && !lastError.getMessage().isBlank()) {
            sb.append("\nDernière erreur technique : ").append(lastError.getMessage()).append("\n");
        }
        sb.append("\n— Rapport local (même heuristique que les graphiques « engagement ») —\n\n");
        return sb.toString();
    }

    private String callChat(String apiKey, String model, String system, String userContent) throws IOException, InterruptedException {
        ObjectNode root = mapper.createObjectNode();
        root.put("model", model);
        root.put("temperature", 0.25);
        root.put("max_tokens", 2048);
        ArrayNode messages = mapper.createArrayNode();
        ObjectNode m1 = mapper.createObjectNode();
        m1.put("role", "system");
        m1.put("content", system);
        messages.add(m1);
        ObjectNode m2 = mapper.createObjectNode();
        m2.put("role", "user");
        m2.put("content", userContent);
        messages.add(m2);
        root.set("messages", messages);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Authorization", "Bearer " + apiKey)
                .header("Accept", "application/json")
                .header("Content-Type", "application/json; charset=utf-8")
                .timeout(Duration.ofSeconds(120))
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(root)))
                .build();
        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("Hugging Face HTTP " + response.statusCode() + " : " + truncate(response.body(), 400));
        }
        JsonNode tree = mapper.readTree(response.body());
        JsonNode choices = tree.path("choices");
        if (!choices.isArray() || choices.isEmpty()) {
            throw new IOException("Hugging Face : réponse sans choices.");
        }
        String text = choices.get(0).path("message").path("content").asText("");
        if (text.isBlank()) {
            throw new IOException("Hugging Face : contenu vide.");
        }
        return text.trim();
    }

    private static String truncate(String s, int max) {
        if (s == null) {
            return "";
        }
        return s.length() <= max ? s : s.substring(0, max) + "...";
    }

    private static final int MAX_USERS_LISTED_PER_SEGMENT = 120;

    private static String segmentHeader(String segmentKey) {
        return switch (segmentKey) {
            case "Très actif" -> "### Profils Très Actifs";
            case "Actif" -> "### Profils Actifs";
            case "Modéré" -> "### Profils Modérés";
            case "À risque" -> "### Profils à Risque de Désengagement (Churn)";
            case "Inactif / churn probable" -> "### Profils Inactifs";
            default -> "### " + segmentKey;
        };
    }

    /**
     * Liste exhaustive (sauf plafond par segment) : même règles que les graphiques — la récence prime.
     */
    private static String buildDeterministicSegmentationMd(List<User> users) {
        if (users == null || users.isEmpty()) {
            return "## Segmentation des utilisateurs\n\nAucun utilisateur.\n";
        }
        List<String> order = List.of("Très actif", "Actif", "Modéré", "À risque", "Inactif / churn probable");
        Map<String, List<User>> bySeg = new LinkedHashMap<>();
        for (String s : order) {
            bySeg.put(s, new ArrayList<>());
        }
        for (User u : users) {
            String seg = UserEngagementHeuristics.segment(u);
            List<User> bucket = bySeg.get(seg);
            if (bucket == null) {
                bySeg.computeIfAbsent("Modéré", k -> new ArrayList<>()).add(u);
            } else {
                bucket.add(u);
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("## Segmentation des utilisateurs\n\n");
        sb.append("Calcul **automatique** (identique aux graphiques « engagement » du tableau de bord). ")
                .append("Un utilisateur avec beaucoup de connexions mais **sans activité depuis plus de 30 jours** est classé **à risque**, ")
                .append("pas « très actif ».\n\n");
        sb.append("**Définitions (rappel)** — Très actif : dernière connexion dans les 7 jours et au moins 5 connexions au total. ")
                .append("Actif : dernière connexion dans les 14 jours. Modéré : entre 15 et 30 jours sans connexion (sans critère « à risque »). ")
                .append("À risque : plus de 30 jours sans connexion, ou moins de 3 connexions avec plus de 14 jours sans connexion. ")
                .append("Inactif / churn probable : aucune date de dernière connexion (souvent 0 login), ou plus de 90 jours sans connexion.\n\n");
        sb.append("**Effectifs** — Très actifs : ").append(bySeg.get("Très actif").size())
                .append(", Actifs : ").append(bySeg.get("Actif").size())
                .append(", Modérés : ").append(bySeg.get("Modéré").size())
                .append(", À risque : ").append(bySeg.get("À risque").size())
                .append(", Inactifs / churn probable : ").append(bySeg.get("Inactif / churn probable").size())
                .append("\n");

        for (String segKey : order) {
            List<User> list = new ArrayList<>(bySeg.getOrDefault(segKey, List.of()));
            list.sort(UserEngagementHeuristics.compareByDisplayName());
            sb.append("\n").append(segmentHeader(segKey)).append("\n\n");
            if (list.isEmpty()) {
                sb.append("- *(aucun)*\n");
                continue;
            }
            int shown = Math.min(list.size(), MAX_USERS_LISTED_PER_SEGMENT);
            for (int i = 0; i < shown; i++) {
                User u = list.get(i);
                String role = u.getRole() == null || u.getRole().isBlank() ? "—" : u.getRole().trim();
                int n = u.getLoginCount();
                sb.append("- **").append(safe(EngagementReportHtmlFormatter.displayName(u))).append("** (")
                        .append(safe(role)).append(") : ")
                        .append(n).append(" ").append(UserEngagementHeuristics.connexionLabel(n))
                        .append(", ").append(UserEngagementHeuristics.formatRecencyPhrase(u))
                        .append(".\n");
            }
            if (list.size() > shown) {
                sb.append("- *(+ ").append(list.size() - shown).append(" autre(s) dans ce segment — affichage tronqué)*\n");
            }
        }
        return sb.toString();
    }

    private static String buildAiNarrativeContext(List<User> users) {
        Map<String, Integer> segments = new LinkedHashMap<>();
        segments.put("Très actif", 0);
        segments.put("Actif", 0);
        segments.put("Modéré", 0);
        segments.put("À risque", 0);
        segments.put("Inactif / churn probable", 0);
        for (User u : users) {
            String s = UserEngagementHeuristics.segment(u);
            segments.merge(s, 1, Integer::sum);
        }
        int total = users.size();
        StringBuilder sb = new StringBuilder();
        sb.append("Contexte factuel (données calculées par l’application — **exact**, à respecter) :\n\n");
        sb.append("- Utilisateurs au total : **").append(total).append("**\n");
        sb.append("- Très actifs : **").append(segments.get("Très actif")).append("** (dernière connexion ≤ 7 j et ≥ 5 connexions)\n");
        sb.append("- Actifs : **").append(segments.get("Actif")).append("** (dernière connexion ≤ 14 j)\n");
        sb.append("- Modérés : **").append(segments.get("Modéré")).append("** (15–30 j, règles « à risque » non remplies)\n");
        sb.append("- À risque de désengagement : **").append(segments.get("À risque")).append("** (> 30 j sans connexion, ou < 3 connexions et > 14 j)\n");
        sb.append("- Inactifs / churn probable : **").append(segments.get("Inactif / churn probable")).append("** (pas de date ou > 90 j)\n");

        List<User> riskSample = users.stream()
                .filter(u -> {
                    String g = UserEngagementHeuristics.segment(u);
                    return "À risque".equals(g) || "Inactif / churn probable".equals(g);
                })
                .sorted(Comparator.comparingLong(UserEngagementHeuristics::daysSinceLoginForSort).reversed()
                        .thenComparing(UserEngagementHeuristics.compareByDisplayName()))
                .limit(20)
                .toList();
        if (!riskSample.isEmpty()) {
            sb.append("\nÉchantillon de comptes **à risque ou inactifs** (déjà classés ainsi, pour contextualiser tes phrases) :\n");
            for (User u : riskSample) {
                sb.append("- ").append(EngagementReportHtmlFormatter.displayName(u))
                        .append(", ").append(u.getLoginCount()).append(" ")
                        .append(UserEngagementHeuristics.connexionLabel(u.getLoginCount()))
                        .append(", ").append(UserEngagementHeuristics.formatRecencyPhrase(u))
                        .append("\n");
            }
        }
        return sb.toString();
    }

    private static String buildLocalHeuristicFollowUp(List<User> users) {
        Map<String, Integer> segments = new LinkedHashMap<>();
        segments.put("Très actif", 0);
        segments.put("Actif", 0);
        segments.put("Modéré", 0);
        segments.put("À risque", 0);
        segments.put("Inactif / churn probable", 0);
        for (User u : users) {
            String s = UserEngagementHeuristics.segment(u);
            segments.merge(s, 1, Integer::sum);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("## Conseils généraux pour l’équipe admin\n");
        sb.append("• **Très actifs** : valoriser (newsletter « nouveautés »), peu de relance intrusive.\n");
        sb.append("• **Actifs / modérés** : contenus éducatifs, rappels doux (email / notification in-app) 1–2 fois / mois.\n");
        sb.append("• **À risque** : message personnalisé (bénéfice concret), offre d’aide, enquête courte « qu’est-ce qui bloque ? ».\n");
        sb.append("• **Inactif / churn probable** : campagne de réactivation limitée (3 touches max), éviter le spam ; proposer tutoriel ou RDV si pertinent au rôle.\n");
        sb.append("• Adapter le ton selon le **rôle** (patient vs médecin vs admin) et respecter le consentement / RGPD sur les relances.\n");

        List<User> riskSorted = new ArrayList<>(users);
        riskSorted.sort(Comparator.comparingLong(UserEngagementHeuristics::daysSinceLoginForSort).reversed()
                .thenComparingInt(User::getLoginCount));
        sb.append("\n## Prédictions (heuristique sur l'état actuel)\n");
        int total = users.size();
        int risq = segments.get("À risque");
        int inact = segments.get("Inactif / churn probable");
        int pctAtRisk = total > 0 ? Math.max(0, (risq + inact) * 100 / total) : 0;
        sb.append("• **À 30 jours** : sans action ciblée, environ **")
                .append(pctAtRisk)
                .append("%** du vivier actuel resterait en situation \"à risque\" ou \"inactif / churn\" (ordre de grandeur indicatif).\n");
        sb.append("• **À 90 jours** : la part **inactifs / churn probable** risque de **progresser** sans relance (sensibilité : ")
                .append(inact > 0 ? "élevée" : "faible à moyenne")
                .append(" selon l'effectif actuel de ce segment).\n");
        sb.append("• **Priorité admin** : relances personnalisées sur **À risque**, puis campagne courte sur **Inactif / churn**.\n");

        sb.append("\n## Personnes à surveiller en priorité (max 12, tri : ancienneté de connexion)\n");
        int n = 0;
        for (User u : riskSorted) {
            if (n >= 12) {
                break;
            }
            String seg = UserEngagementHeuristics.segment(u);
            if (!"Très actif".equals(seg) && !"Actif".equals(seg)) {
                sb.append("• **").append(safe(EngagementReportHtmlFormatter.displayName(u))).append("**")
                        .append(" | rôle ").append(safe(u.getRole()))
                        .append(" | logins ").append(u.getLoginCount())
                        .append(" | dern. connexion ").append(u.getLastLoginAt() != null ? u.getLastLoginAt().toLocalDate().toString() : "—")
                        .append(" | segment ").append(seg)
                        .append("\n");
                n++;
            }
        }
        if (n == 0) {
            sb.append("• (aucun profil à risque ou inactif dans la liste actuelle.)\n");
        }

        return sb.toString();
    }

    private static String buildLocalEngagementReport(List<User> users) {
        return buildDeterministicSegmentationMd(users) + "\n\n---\n\n" + buildLocalHeuristicFollowUp(users);
    }

    private static String safe(String s) {
        return s == null ? "" : s.trim();
    }
}
