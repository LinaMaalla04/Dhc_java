package tn.dhc.utils;

import tn.dhc.entities.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Prépare le rapport d'engagement pour affichage WebView : remplace les références d'id par prénom/nom,
 * puis convertit un sous-ensemble Markdown (titres, listes, gras) en HTML sûr.
 */
public final class EngagementReportHtmlFormatter {

    private static final Pattern PAT_ID = Pattern.compile("\\bID\\s+(\\d+)\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern PAT_UTIL = Pattern.compile("(?i)utilisateur\\s*#?\\s*(\\d+)\\b");
    private EngagementReportHtmlFormatter() {
    }

    public static String displayName(User u) {
        if (u == null) {
            return "";
        }
        String p = u.getPrenom() == null ? "" : u.getPrenom().trim();
        String n = u.getNom() == null ? "" : u.getNom().trim();
        String both = (p + " " + n).trim();
        return both.isBlank() ? ("Utilisateur #" + u.getId()) : both;
    }

    /**
     * Remplace les motifs type « ID 5 », « utilisateur 12 » par le prénom et nom.
     */
    public static String replaceUserRefsInPlainText(String text, List<User> users) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        if (users == null || users.isEmpty()) {
            return text;
        }
        Map<Integer, User> byId = new HashMap<>();
        for (User u : users) {
            byId.put(u.getId(), u);
        }
        String out = replaceWithMap(text, PAT_ID, byId);
        return replaceWithMap(out, PAT_UTIL, byId);
    }

    private static String replaceWithMap(String text, Pattern pat, Map<Integer, User> byId) {
        Matcher m = pat.matcher(text);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            int id = Integer.parseInt(m.group(1));
            User u = byId.get(id);
            String rep = u != null ? Matcher.quoteReplacement(displayName(u)) : m.group(0);
            m.appendReplacement(sb, rep);
        }
        m.appendTail(sb);
        return sb.toString();
    }

    private static boolean isBulletLine(String t) {
        if (t.startsWith("- ")) {
            return true;
        }
        return t.length() >= 2 && t.charAt(0) == '\u2022' && t.charAt(1) == ' ';
    }

    private static String bulletItemText(String t) {
        if (t.startsWith("- ")) {
            return t.substring(2).trim();
        }
        if (t.length() >= 2 && t.charAt(0) == '\u2022' && t.charAt(1) == ' ') {
            return t.substring(2).trim();
        }
        return t;
    }

    public static String toFullHtmlDocument(String rawMarkdownOrPlain, List<User> users) {
        String withNames = replaceUserRefsInPlainText(rawMarkdownOrPlain, users);
        String escaped = escapeHtml(withNames);
        String body = markdownLiteToHtml(escaped);
        return """
                <!DOCTYPE html>
                <html>
                <head>
                <meta charset="UTF-8"/>
                <style>
                  body { font-family: 'Segoe UI', system-ui, sans-serif; font-size: 14px; color: #0f172a;
                         line-height: 1.55; margin: 0; padding: 18px 20px; background: #f8fafc; }
                  h2 { font-size: 1.22rem; color: #0d6b7a; margin: 1.15em 0 0.45em; font-weight: 700; border-bottom: 1px solid #cbd5e1; padding-bottom: 0.2em; }
                  h3 { font-size: 1.08rem; color: #0f766e; margin: 1em 0 0.35em; font-weight: 650; }
                  h4 { font-size: 1rem; color: #115e59; margin: 0.9em 0 0.3em; font-weight: 600; }
                  ul { margin: 0.4em 0 0.6em; padding-left: 1.35em; }
                  li { margin: 0.35em 0; }
                  p { margin: 0.55em 0; }
                  strong { color: #0f172a; font-weight: 650; }
                  hr { border: 0; border-top: 1px solid #e2e8f0; margin: 1em 0; }
                </style>
                </head>
                <body>
                """
                + body
                + """
                </body>
                </html>
                """;
    }

    private static String escapeHtml(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private static String applyBold(String line) {
        return line.replaceAll("\\*\\*([^*]+)\\*\\*", "<strong>$1</strong>");
    }

    private static void closeUl(StringBuilder out, boolean inUl) {
        if (inUl) {
            out.append("</ul>\n");
        }
    }

    private static String markdownLiteToHtml(String escaped) {
        String[] lines = escaped.split("\n", -1);
        StringBuilder out = new StringBuilder();
        boolean inUl = false;
        for (String rawLine : lines) {
            String t = rawLine.trim();
            if (t.isEmpty()) {
                closeUl(out, inUl);
                inUl = false;
                out.append("<br/>\n");
                continue;
            }
            if (t.startsWith("#### ")) {
                closeUl(out, inUl);
                inUl = false;
                out.append("<h4>").append(applyBold(t.substring(5).trim())).append("</h4>\n");
            } else if (t.startsWith("### ")) {
                closeUl(out, inUl);
                inUl = false;
                out.append("<h3>").append(applyBold(t.substring(4).trim())).append("</h3>\n");
            } else if (t.startsWith("## ")) {
                closeUl(out, inUl);
                inUl = false;
                out.append("<h2>").append(applyBold(t.substring(3).trim())).append("</h2>\n");
            } else if (t.startsWith("# ") && !t.startsWith("##")) {
                closeUl(out, inUl);
                inUl = false;
                out.append("<h2>").append(applyBold(t.substring(2).trim())).append("</h2>\n"); // titre niveau 1 rare
            } else if (isBulletLine(t)) {
                if (!inUl) {
                    out.append("<ul>\n");
                    inUl = true;
                }
                out.append("<li>").append(applyBold(bulletItemText(t))).append("</li>\n");
            } else {
                closeUl(out, inUl);
                inUl = false;
                out.append("<p>").append(applyBold(t)).append("</p>\n");
            }
        }
        closeUl(out, inUl);
        return out.toString();
    }
}
