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
<<<<<<< HEAD
            if (!t.matches("^\\d{8}$")) {
                issues.add(new FieldIssue("telephone",
                        "Téléphone : saisissez exactement 8 chiffres."));
=======
            if (!t.matches("^[0-9+().\\s-]{6,25}$")) {
                issues.add(new FieldIssue("telephone",
                        "Téléphone : utilisez 6 à 25 caractères (chiffres, +, espaces, parenthèses, point ou tiret)."));
>>>>>>> 34f983539afbf1fbe48f8fe4cc4c438bec376064
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
<<<<<<< HEAD
        if (s < 1 || s > 9_999_999) {
            issues.add(new FieldIssue("stock", "Le stock doit être compris entre 1 et 9 999 999."));
=======
        if (s < 0 || s > 9_999_999) {
            issues.add(new FieldIssue("stock", "Le stock doit être compris entre 0 et 9 999 999."));
>>>>>>> 34f983539afbf1fbe48f8fe4cc4c438bec376064
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

    public static List<FieldIssue> collectFicheIssues(String poids, String taille, String glycemie,
                                                      String libelle, String gravite, String recommandation,
                                                      String allergie, String maladieChronique, String tension,
                                                      String grpSanguin, LocalDate dateFiche) {
        List<FieldIssue> issues = new ArrayList<>();
        if (libelle == null || libelle.isBlank()) {
            issues.add(new FieldIssue("libelle", "Le libellé de la maladie est obligatoire."));
        } else {
            if (libelle.trim().length() < 2) {
                issues.add(new FieldIssue("libelle", "Le libellé doit contenir au moins 2 caractères."));
            }
            if (libelle.length() > 200) {
                issues.add(new FieldIssue("libelle", "Le libellé ne doit pas dépasser 200 caractères."));
            }
        }
        if (!isValidGravite(gravite)) {
            issues.add(new FieldIssue("gravite", "La gravité doit être Faible, Modérée ou Élevée."));
        }
        if (recommandation != null && recommandation.length() > 2000) {
            issues.add(new FieldIssue("recommandation", "Les recommandations ne doivent pas dépasser 2000 caractères."));
        }
        if (allergie != null && allergie.length() > 500) {
            issues.add(new FieldIssue("allergie", "Le champ allergies ne doit pas dépasser 500 caractères."));
        }
        if (maladieChronique != null && maladieChronique.length() > 500) {
            issues.add(new FieldIssue("chronique", "La maladie chronique ne doit pas dépasser 500 caractères."));
        }
        if (tension != null && !tension.isBlank()) {
            String t = tension.trim();
            if (t.length() > 40) {
                issues.add(new FieldIssue("tension", "La tension ne doit pas dépasser 40 caractères (ex. 120/80)."));
            } else if (t.length() < 2) {
                issues.add(new FieldIssue("tension", "La tension doit contenir au moins 2 caractères ou rester vide."));
            }
        }
        if (grpSanguin != null && !grpSanguin.isBlank()) {
            String g = grpSanguin.trim();
            if (g.length() > 12) {
                issues.add(new FieldIssue("grpSanguin", "Le groupe sanguin ne doit pas dépasser 12 caractères."));
            }
        }

        Double po = parseOptionalDouble(poids, "poids", "Le poids est obligatoire.", "Poids : nombre invalide.", issues);
        Double ta = parseOptionalDouble(taille, "taille", "La taille est obligatoire.", "Taille : nombre invalide.", issues);
        Double gl = parseOptionalDouble(glycemie, "glycemie", "La glycémie est obligatoire.", "Glycémie : nombre invalide.", issues);

        if (po != null && (po < 2 || po > 400)) {
            issues.add(new FieldIssue("poids", "Le poids doit être compris entre 2 et 400 kg."));
        }
        if (ta != null && (ta < 40 || ta > 260)) {
            issues.add(new FieldIssue("taille", "La taille doit être comprise entre 40 et 260 cm."));
        }
        if (gl != null && (gl < 0.2 || gl > 50)) {
            issues.add(new FieldIssue("glycemie", "La glycémie doit être comprise entre 0,2 et 50 (valeur clinique usuelle)."));
        }
        if (dateFiche != null) {
            LocalDate min = LocalDate.of(1920, 1, 1);
            LocalDate max = LocalDate.now().plusYears(1);
            if (dateFiche.isBefore(min)) {
                issues.add(new FieldIssue("date", "La date de la fiche ne peut pas être antérieure à 1920."));
            }
            if (dateFiche.isAfter(max)) {
                issues.add(new FieldIssue("date", "La date de la fiche ne peut pas dépasser un an dans le futur."));
            }
        }
        return issues;
    }

    private static Double parseOptionalDouble(String raw, String fieldId, String blankMsg, String badMsg,
                                              List<FieldIssue> issues) {
        if (raw == null || raw.trim().isBlank()) {
            issues.add(new FieldIssue(fieldId, blankMsg));
            return null;
        }
        try {
            return Double.parseDouble(raw.trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            issues.add(new FieldIssue(fieldId, badMsg));
            return null;
        }
    }

    public static Optional<String> validateFiche(String poids, String taille, String glycemie,
                                                 String libelle, String gravite, String recommandation,
                                                 String allergie, String maladieChronique, String tension,
                                                 String grpSanguin, LocalDate dateFiche) {
        List<FieldIssue> list = collectFicheIssues(poids, taille, glycemie, libelle, gravite, recommandation,
                allergie, maladieChronique, tension, grpSanguin, dateFiche);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0).message());
    }

    public static List<FieldIssue> collectOrdonnanceIssues(String frequence, String dureeJours, String posologie,
                                                           LocalDate dateOrdonnance) {
        List<FieldIssue> issues = new ArrayList<>();
        if (frequence != null && frequence.length() > 200) {
            issues.add(new FieldIssue("frequence", "La fréquence ne doit pas dépasser 200 caractères."));
        } else if (frequence != null && !frequence.isBlank() && frequence.trim().length() < 2) {
            issues.add(new FieldIssue("frequence", "La fréquence doit contenir au moins 2 caractères ou rester vide."));
        }
        if (posologie == null || posologie.isBlank()) {
            issues.add(new FieldIssue("posologie", "La posologie est obligatoire."));
        } else {
            String p = posologie.trim();
            if (p.length() < 5) {
                issues.add(new FieldIssue("posologie", "La posologie doit contenir au moins 5 caractères."));
            }
            if (p.length() > 4000) {
                issues.add(new FieldIssue("posologie", "La posologie ne doit pas dépasser 4000 caractères."));
            }
        }
        if (dureeJours == null || dureeJours.trim().isBlank()) {
            issues.add(new FieldIssue("duree", "La durée (jours) est obligatoire."));
        } else {
            try {
                int j = Integer.parseInt(dureeJours.trim());
                if (j < 1 || j > 3650) {
                    issues.add(new FieldIssue("duree", "La durée du traitement doit être entre 1 et 3650 jours."));
                }
            } catch (NumberFormatException e) {
                issues.add(new FieldIssue("duree", "La durée doit être un nombre entier (jours)."));
            }
        }
        if (dateOrdonnance != null) {
            LocalDate min = LocalDate.of(1990, 1, 1);
            LocalDate max = LocalDate.now().plusYears(5);
            if (dateOrdonnance.isBefore(min)) {
                issues.add(new FieldIssue("date", "La date de l'ordonnance ne peut pas être antérieure à 1990."));
            }
            if (dateOrdonnance.isAfter(max)) {
                issues.add(new FieldIssue("date", "La date de l'ordonnance ne peut pas dépasser 5 ans dans le futur."));
            }
        }
        return issues;
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
