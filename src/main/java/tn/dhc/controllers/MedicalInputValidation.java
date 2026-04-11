package tn.dhc.controllers;

import java.text.Normalizer;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

/**
 * Contrôles de saisie pour les formulaires pharmacie / médicament / fiche / ordonnance.
 */
public final class MedicalInputValidation {

    /** Identifiant de champ pour bordures d'erreur dans l'UI (voir {@link MedicalFormDialogs}). */
    public record FieldIssue(String fieldId, String message) {
    }

    private MedicalInputValidation() {
    }

    public static List<FieldIssue> collectPharmacieIssues(String nom, String adresse, String telephone,
                                                          String responsable, String hopital) {
        List<FieldIssue> issues = new ArrayList<>();
        if (nom == null || nom.isBlank()) {
            issues.add(new FieldIssue("nom", "Le nom de la pharmacie est obligatoire."));
        } else {
            String n = nom.trim();
            if (n.length() < 2) {
                issues.add(new FieldIssue("nom", "Le nom doit contenir au moins 2 caractères."));
            }
            if (n.length() > 120) {
                issues.add(new FieldIssue("nom", "Le nom ne doit pas dépasser 120 caractères."));
            }
        }
        if (adresse == null || adresse.isBlank()) {
            issues.add(new FieldIssue("adresse", "L'adresse est obligatoire."));
        } else {
            String a = adresse.trim();
            if (a.length() < 3) {
                issues.add(new FieldIssue("adresse", "L'adresse doit contenir au moins 3 caractères."));
            }
            if (a.length() > 300) {
                issues.add(new FieldIssue("adresse", "L'adresse ne doit pas dépasser 300 caractères."));
            }
        }
        if (telephone != null && !telephone.isBlank()) {
            String t = telephone.trim();
            if (!t.matches("^[0-9+().\\s-]{6,25}$")) {
                issues.add(new FieldIssue("telephone",
                        "Téléphone : utilisez 6 à 25 caractères (chiffres, +, espaces, parenthèses, point ou tiret)."));
            }
        }
        if (responsable != null && !responsable.isBlank()) {
            String r = responsable.trim();
            if (r.length() < 2) {
                issues.add(new FieldIssue("responsable", "Le responsable doit contenir au moins 2 caractères ou rester vide."));
            } else if (r.length() > 100) {
                issues.add(new FieldIssue("responsable", "Le nom du responsable ne doit pas dépasser 100 caractères."));
            }
        }
        if (hopital != null && !hopital.isBlank()) {
            String h = hopital.trim();
            if (h.length() < 2) {
                issues.add(new FieldIssue("hopital", "Le nom d'hôpital doit contenir au moins 2 caractères ou rester vide."));
            } else if (h.length() > 120) {
                issues.add(new FieldIssue("hopital", "Le champ hôpital ne doit pas dépasser 120 caractères."));
            }
        }
        return issues;
    }

    public static Optional<String> validatePharmacie(String nom, String adresse, String telephone,
                                                     String responsable, String hopital) {
        List<FieldIssue> list = collectPharmacieIssues(nom, adresse, telephone, responsable, hopital);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0).message());
    }

    public static List<FieldIssue> collectMedicamentIssues(String nom, String categorie, String dosage, String forme,
                                                           LocalDate expiration, Integer stock) {
        List<FieldIssue> issues = new ArrayList<>();
        if (nom == null || nom.isBlank()) {
            issues.add(new FieldIssue("nom", "Le nom du médicament est obligatoire."));
        } else {
            String n = nom.trim();
            if (n.length() < 2) {
                issues.add(new FieldIssue("nom", "Le nom doit contenir au moins 2 caractères."));
            }
            if (n.length() > 150) {
                issues.add(new FieldIssue("nom", "Le nom ne doit pas dépasser 150 caractères."));
            }
        }
        if (categorie != null && !categorie.isBlank()) {
            String c = categorie.trim();
            if (c.length() < 2) {
                issues.add(new FieldIssue("categorie", "La catégorie doit contenir au moins 2 caractères ou rester vide."));
            } else if (c.length() > 100) {
                issues.add(new FieldIssue("categorie", "La catégorie ne doit pas dépasser 100 caractères."));
            }
        }
        if (dosage != null && !dosage.isBlank() && dosage.trim().length() > 100) {
            issues.add(new FieldIssue("dosage", "Le dosage ne doit pas dépasser 100 caractères."));
        }
        if (forme != null && !forme.isBlank()) {
            String f = forme.trim();
            if (f.length() < 2) {
                issues.add(new FieldIssue("forme", "La forme doit contenir au moins 2 caractères ou rester vide."));
            } else if (f.length() > 80) {
                issues.add(new FieldIssue("forme", "La forme ne doit pas dépasser 80 caractères."));
            }
        }
        if (expiration == null) {
            issues.add(new FieldIssue("expiration", "La date d'expiration est obligatoire."));
        } else {
            if (expiration.isBefore(LocalDate.of(2000, 1, 1))) {
                issues.add(new FieldIssue("expiration", "La date d'expiration est incohérente (avant 2000)."));
            }
            if (expiration.isAfter(LocalDate.now().plusYears(30))) {
                issues.add(new FieldIssue("expiration", "La date d'expiration semble trop lointaine (max. 30 ans)."));
            }
        }
        int s = stock != null ? stock : 0;
        if (s < 0 || s > 9_999_999) {
            issues.add(new FieldIssue("stock", "Le stock doit être compris entre 0 et 9 999 999."));
        }
        return issues;
    }

    public static Optional<String> validateMedicament(String nom, String categorie, String dosage, String forme,
                                                      LocalDate expiration, Integer stock) {
        List<FieldIssue> list = collectMedicamentIssues(nom, categorie, dosage, forme, expiration, stock);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0).message());
    }

    /**
     * Normalise une gravité (libre ou combo) vers le libellé canonique Faible / Modérée / Élevée, ou chaîne vide si non reconnue.
     */
    public static String canonicalGraviteLabel(String input) {
        if (input == null || input.isBlank()) {
            return "";
        }
        String n = Normalizer.normalize(input.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT);
        if (n.contains("faible")) {
            return "Faible";
        }
        if (n.contains("modere")) {
            return "Modérée";
        }
        if (n.contains("eleve")) {
            return "Élevée";
        }
        return "";
    }

    public static boolean isValidGravite(String gravite) {
        return gravite != null && !gravite.isBlank() && !canonicalGraviteLabel(gravite).isEmpty();
    }

    /** Filtre liste : valeur combo « Toutes gravités » ou un des trois niveaux. */
    public static boolean graviteFilterMatches(String storedInDb, String filterSelection) {
        if (filterSelection == null || filterSelection.isBlank() || "Toutes gravités".equals(filterSelection)) {
            return true;
        }
        String a = canonicalGraviteLabel(storedInDb);
        String b = canonicalGraviteLabel(filterSelection);
        return !a.isEmpty() && a.equals(b);
    }

    

    public static Optional<String> validateOrdonnance(String frequence, String dureeJours, String posologie,
                                                      LocalDate dateOrdonnance) {
        List<FieldIssue> list = collectOrdonnanceIssues(frequence, dureeJours, posologie, dateOrdonnance);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0).message());
    }

    /** Messages uniques pour une boîte de dialogue (ordre conservé). */
    public static String formatIssueMessages(List<FieldIssue> issues) {
        Set<String> seen = new LinkedHashSet<>();
        StringBuilder sb = new StringBuilder();
        for (FieldIssue i : issues) {
            if (seen.add(i.message())) {
                if (!sb.isEmpty()) {
                    sb.append('\n');
                }
                sb.append("• ").append(i.message());
            }
        }
        return sb.toString();
    }
}
