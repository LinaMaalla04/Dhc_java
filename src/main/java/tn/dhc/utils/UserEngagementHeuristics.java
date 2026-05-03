package tn.dhc.utils;

import tn.dhc.entities.User;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.Locale;

/**
 * Règles d'engagement alignées sur les graphiques dashboard : la <strong>récence</strong>
 * (jours depuis la dernière connexion) prime sur le volume de connexions passées.
 */
public final class UserEngagementHeuristics {

    private UserEngagementHeuristics() {
    }

    /**
     * Segment unique par utilisateur (même logique que les camemberts « engagement »).
     */
    public static String segment(User u) {
        int c = u.getLoginCount();
        if (u.getLastLoginAt() == null) {
            return c >= 1 ? "À risque" : "Inactif / churn probable";
        }
        long days = ChronoUnit.DAYS.between(u.getLastLoginAt().toLocalDate(), LocalDate.now());
        if (days > 90) {
            return "Inactif / churn probable";
        }
        if (days > 30 || (c < 3 && days > 14)) {
            return "À risque";
        }
        if (days <= 7 && c >= 5) {
            return "Très actif";
        }
        if (days <= 14) {
            return "Actif";
        }
        return "Modéré";
    }

    public static String recencyBucket(User u) {
        if (u.getLastLoginAt() == null) {
            return "Sans date";
        }
        long days = ChronoUnit.DAYS.between(u.getLastLoginAt().toLocalDate(), LocalDate.now());
        if (days <= 7) {
            return "0-7 j";
        }
        if (days <= 30) {
            return "8-30 j";
        }
        if (days <= 60) {
            return "31-60 j";
        }
        return "61+ j";
    }

    /** Tri : utilisateurs sans date en dernier ; sinon jours depuis dernière connexion. */
    public static long daysSinceLoginForSort(User u) {
        if (u.getLastLoginAt() == null) {
            return Long.MAX_VALUE / 4;
        }
        return ChronoUnit.DAYS.between(u.getLastLoginAt().toLocalDate(), LocalDate.now());
    }

    public static Comparator<User> compareByDisplayName() {
        return Comparator.comparing(
                u -> EngagementReportHtmlFormatter.displayName(u).toLowerCase(Locale.ROOT));
    }

    public static String formatRecencyPhrase(User u) {
        if (u.getLastLoginAt() == null) {
            return "dernière connexion non disponible";
        }
        long days = ChronoUnit.DAYS.between(u.getLastLoginAt().toLocalDate(), LocalDate.now());
        if (days <= 0) {
            return "dernière connexion aujourd'hui";
        }
        if (days == 1) {
            return "dernière connexion il y a 1 jour";
        }
        return "dernière connexion il y a " + days + " jours";
    }

    public static String connexionLabel(int n) {
        return n <= 1 ? "connexion" : "connexions";
    }
}
