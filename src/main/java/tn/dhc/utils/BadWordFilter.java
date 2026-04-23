package tn.dhc.utils;

import java.util.Arrays;
import java.util.List;

public class BadWordFilter {

    private static final List<String> BAD_WORDS = Arrays.asList(
        // French
        "merde", "con", "salope", "putain", "connard", "fdp", "enculé", "bite", "couille", "chienne",
        // English
        "fuck", "shit", "bitch", "asshole", "bastard", "dick", "pussy", "cunt", "motherfucker",
        // Arabic (Common Romanized and some Arabic script if supported)
        "zibi", "zab", "kahba", "manyak", "nemm", "asba", "nayek", "khra", "kalb"
    );

    public static String filter(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        String filteredText = text;
        for (String badWord : BAD_WORDS) {
            // Case-insensitive replacement with regex to match whole words
            String regex = "(?i)\\b" + badWord + "\\b";
            filteredText = filteredText.replaceAll(regex, "****");
        }
        return filteredText;
    }
}
