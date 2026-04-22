package tn.dhc.services;

/**
 * Clé API openFDA (voir open.fda.gov — authentification).
 * Surcharge : OPENFDA_API_KEY, FDA_API_KEY, DATA_GOV_API_KEY ou -Dopenfda.api.key=...
 */
public final class OpenFdaApiKey {

    /** Clé fournie (data.gov / openFDA). */
    private static final String DEFAULT_KEY = "g0NqFzV0cAFK4Y6xhhR9TGK8ebzp27GaQOv7tZgj";

    private OpenFdaApiKey() {
    }

    public static String resolve() {
        String v = firstNonBlank(
                System.getenv("OPENFDA_API_KEY"),
                System.getenv("FDA_API_KEY"),
                System.getenv("DATA_GOV_API_KEY"));
        if (v != null) {
            return sanitize(v);
        }
        String prop = System.getProperty("openfda.api.key");
        if (prop != null && !prop.isBlank()) {
            return sanitize(prop);
        }
        return sanitize(DEFAULT_KEY);
    }

    private static String firstNonBlank(String... vals) {
        if (vals == null) {
            return null;
        }
        for (String s : vals) {
            if (s != null && !s.isBlank()) {
                return s;
            }
        }
        return null;
    }

    private static String sanitize(String key) {
        return key.trim()
                .replace("\uFEFF", "")
                .replace("\r", "")
                .replace("\n", "");
    }
}
