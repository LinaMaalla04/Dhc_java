package tn.dhc.utils;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import tn.dhc.entities.Fiche;
import tn.dhc.entities.Medicament;
import tn.dhc.entities.Ordonnance;
import tn.dhc.entities.User;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Génère une ordonnance PDF structurée (mise en page type document médical).
 */
public final class OrdonnancePdfExporter {

    private static final float MARGIN = 56;
    private static final float LINE_HEIGHT = 14;
    private static final float TITLE_SIZE = 18;
    private static final float HEAD_SIZE = 11;
    private static final float BODY_SIZE = 10;
    private static final float FOOTER_Y = 100;

    private OrdonnancePdfExporter() {
    }

    public static void write(Ordonnance ordonnance, Fiche fiche, User patient, User prescripteur,
                             List<Medicament> medicaments, Path outputPath) throws IOException {
        PDFont helv = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
        PDFont helvBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);
            float pageW = page.getMediaBox().getWidth();
            float pageH = page.getMediaBox().getHeight();
            float textW = pageW - 2 * MARGIN;
            float y = pageH - MARGIN;

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                y = drawCenteredTitle(cs, helvBold, "ORDONNANCE MÉDICALE", TITLE_SIZE, pageW, y);
                y -= 8;
                y = drawHorizontalLine(cs, MARGIN, y, pageW - MARGIN);
                y -= 18;

                y = drawBlock(cs, helv, helvBold, textW, MARGIN, y,
                        "Établissement", "DHC — Digital Health Center");
                y = drawBlock(cs, helv, helvBold, textW, MARGIN, y,
                        "Date de prescription",
                        ordonnance.getDate() != null ? ordonnance.getDate().toString() : "—");

                String patientLine = patient != null
                        ? safe(patient.getPrenom()) + " " + safe(patient.getNom()).toUpperCase(Locale.ROOT)
                        : "—";
                y = drawBlock(cs, helv, helvBold, textW, MARGIN, y, "Patient", patientLine);

                if (fiche != null) {
                    y = drawBlock(cs, helv, helvBold, textW, MARGIN, y,
                            "Motif / suivi (fiche n° " + fiche.getId() + ")",
                            safe(fiche.getLibelleMaladie()) + " — Gravité : " + safe(fiche.getGravite()));
                }

                String presc = prescripteur != null
                        ? "Dr " + safe(prescripteur.getPrenom()) + " " + safe(prescripteur.getNom()).toUpperCase(Locale.ROOT)
                        + (prescripteur.getSpecialite() != null && !prescripteur.getSpecialite().isBlank()
                        ? " — " + prescripteur.getSpecialite().trim() : "")
                        : "Document patient DHC — identité du prescripteur sur le dossier clinique.";
                y = drawBlock(cs, helv, helvBold, textW, MARGIN, y, "Prescripteur", presc);

                y -= 6;
                y = drawHorizontalLine(cs, MARGIN, y, pageW - MARGIN);
                y -= 16;

                cs.setFont(helvBold, HEAD_SIZE);
                cs.beginText();
                cs.newLineAtOffset(MARGIN, y);
                cs.showText("Prescription");
                cs.endText();
                y -= LINE_HEIGHT + 4;

                y = drawWrapped(cs, helv, BODY_SIZE, MARGIN, y, textW, safe(ordonnance.getPosologie()));
                y -= 8;

                y = drawBlock(cs, helv, helvBold, textW, MARGIN, y,
                        "Fréquence", ordonnance.getFrequence() != null && !ordonnance.getFrequence().isBlank()
                                ? ordonnance.getFrequence().trim() : "—");
                y = drawBlock(cs, helv, helvBold, textW, MARGIN, y,
                        "Durée du traitement", ordonnance.getDureeTraitement() + " jour(s)");

                y -= 10;
                cs.setFont(helvBold, HEAD_SIZE);
                cs.beginText();
                cs.newLineAtOffset(MARGIN, y);
                cs.showText("Médicament(s) prescrit(s)");
                cs.endText();
                y -= LINE_HEIGHT + 4;

                if (medicaments == null || medicaments.isEmpty()) {
                    y = drawWrapped(cs, helv, BODY_SIZE, MARGIN, y, textW, "— (aucun médicament lié en base)");
                } else {
                    int n = 1;
                    for (Medicament m : medicaments) {
                        String line = n + ". " + safe(m.getNomMedicament());
                        if (m.getDosage() != null && !m.getDosage().isBlank()) {
                            line += " — " + m.getDosage().trim();
                        }
                        if (m.getForme() != null && !m.getForme().isBlank()) {
                            line += " (" + m.getForme().trim() + ")";
                        }
                        y = drawWrapped(cs, helv, BODY_SIZE, MARGIN, y, textW, line);
                        n++;
                    }
                }
            }

            if (y < FOOTER_Y + 50) {
                PDPage page2 = new PDPage(PDRectangle.A4);
                doc.addPage(page2);
                try (PDPageContentStream cs2 = new PDPageContentStream(doc, page2)) {
                    drawSignatureFooter(cs2, helv, pageW, FOOTER_Y);
                }
            } else {
                try (PDPageContentStream cs2 = new PDPageContentStream(doc, page, AppendMode.APPEND, true, true)) {
                    drawSignatureFooter(cs2, helv, pageW, FOOTER_Y);
                }
            }

            doc.save(outputPath.toFile());
        }
    }

    private static void drawSignatureFooter(PDPageContentStream cs, PDFont helv, float pageW, float y) throws IOException {
        cs.setStrokingColor(0.35f, 0.35f, 0.35f);
        cs.setLineWidth(0.6f);
        cs.moveTo(MARGIN, y + 36);
        cs.lineTo(pageW - MARGIN, y + 36);
        cs.stroke();
        cs.setStrokingColor(0, 0, 0);
        cs.setFont(helv, 9);
        cs.beginText();
        cs.newLineAtOffset(MARGIN, y + 12);
        cs.showText("Signature et cachet du médecin");
        cs.endText();
    }

    private static float drawCenteredTitle(PDPageContentStream cs, PDFont font, String text, float size,
                                           float pageW, float y) throws IOException {
        float w = font.getStringWidth(text) / 1000 * size;
        float x = (pageW - w) / 2;
        cs.setFont(font, size);
        cs.beginText();
        cs.newLineAtOffset(x, y);
        cs.showText(text);
        cs.endText();
        return y - size - 4;
    }

    private static float drawHorizontalLine(PDPageContentStream cs, float x1, float y, float x2) throws IOException {
        cs.setStrokingColor(0f, 0.5f, 0.47f);
        cs.setLineWidth(1.2f);
        cs.moveTo(x1, y);
        cs.lineTo(x2, y);
        cs.stroke();
        cs.setStrokingColor(0, 0, 0);
        return y;
    }

    private static float drawBlock(PDPageContentStream cs, PDFont body, PDFont bold, float maxW,
                                   float margin, float y, String label, String value) throws IOException {
        cs.setFont(bold, BODY_SIZE);
        cs.beginText();
        cs.newLineAtOffset(margin, y);
        cs.showText(label + " : ");
        cs.endText();
        y -= LINE_HEIGHT;
        y = drawWrapped(cs, body, BODY_SIZE, margin + 8, y, maxW - 8, value);
        y -= 4;
        return y;
    }

    private static float drawWrapped(PDPageContentStream cs, PDFont font, float fontSize,
                                     float x, float y, float maxWidth, String text) throws IOException {
        for (String line : wrap(text, font, fontSize, maxWidth)) {
            cs.setFont(font, fontSize);
            cs.beginText();
            cs.newLineAtOffset(x, y);
            cs.showText(line);
            cs.endText();
            y -= LINE_HEIGHT;
        }
        return y;
    }

    private static List<String> wrap(String text, PDFont font, float fontSize, float maxWidth) throws IOException {
        List<String> lines = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            lines.add("");
            return lines;
        }
        String[] paragraphs = text.split("\n");
        for (String para : paragraphs) {
            if (para.isEmpty()) {
                lines.add("");
                continue;
            }
            String[] words = para.split("\\s+");
            StringBuilder current = new StringBuilder();
            for (String w : words) {
                String trial = current.isEmpty() ? w : current + " " + w;
                float tw = font.getStringWidth(trial) / 1000f * fontSize;
                if (tw > maxWidth && !current.isEmpty()) {
                    lines.add(current.toString());
                    current = new StringBuilder(w);
                } else {
                    current = new StringBuilder(trial);
                }
            }
            if (!current.isEmpty()) {
                lines.add(current.toString());
            }
        }
        return lines;
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }
}
