package tn.dhc.controllers;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import tn.dhc.entities.Fiche;
import tn.dhc.entities.Medicament;
import tn.dhc.entities.Ordonnance;
import tn.dhc.entities.Pharmacie;

/**
 * Card layouts for the admin clinical / pharmacy sections (no IDs shown on cards).
 */
public final class MedicalAdminCards {

    private MedicalAdminCards() {
    }

    public static VBox pharmacieCard(Pharmacie p, Runnable onEdit, Runnable onDelete) {
        Label title = titled("🏥 " + nullSafe(p.getNom()));
        Label adresse = meta("📍 " + nullSafe(p.getAdresse()));
        Label tel = meta("📞 " + nullSafe(p.getTelephone()));
        Label resp = meta("👤 " + nullSafe(p.getResponsable()));
        Label hop = meta("🏨 " + (p.getHopital() != null && !p.getHopital().isBlank() ? p.getHopital() : "—"));

        HBox actions = actionRow(
                smallButton("Modifier", "btn-secondary", onEdit),
                smallButton("Supprimer", "btn-danger", onDelete)
        );

        VBox card = wrapCard(title, adresse, tel, resp, hop, new Separator(), actions);
        card.setUserData(p);
        return card;
    }

    public static VBox medicamentCard(Medicament m, Runnable onEdit, Runnable onDelete) {
        Label title = titled("💊 " + nullSafe(m.getNomMedicament()));
        Label cat = meta("📂 " + nullSafe(m.getCategorie()));
        Label dose = meta("⚗ " + nullSafe(m.getDosage()) + " · " + nullSafe(m.getForme()));
        Label stock = meta("📦 Stock : " + m.getStock());
        String exp = m.getDateExpiration() != null ? m.getDateExpiration().toString() : "—";
        Label expL = meta("📅 Expire : " + exp);

        HBox actions = actionRow(
                smallButton("Modifier", "btn-secondary", onEdit),
                smallButton("Supprimer", "btn-danger", onDelete)
        );

        VBox card = wrapCard(title, cat, dose, stock, expL, new Separator(), actions);
        card.setUserData(m);
        return card;
    }

    
    public static VBox ordonnanceCard(Ordonnance o, String ficheSummary, String medicamentsSummary, Runnable onEdit, Runnable onDelete) {
        Label title = titled("📜 Ordonnance");
        Label fiche = meta("📎 Fiche : " + nullSafe(ficheSummary));
        Label meds = wrapMeta("💊 Médicaments : " + shorten(nullSafe(medicamentsSummary), 120));
        Label pos = wrapMeta("Posologie : " + shorten(nullSafe(o.getPosologie()), 140));
        Label freq = meta("⏱ " + nullSafe(o.getFrequence()) + " · durée " + o.getDureeTraitement() + " j");
        String d = o.getDate() != null ? o.getDate().toString() : "—";
        Label dateL = meta("📅 " + d);

        HBox actions = actionRow(
                smallButton("Modifier", "btn-secondary", onEdit),
                smallButton("Supprimer", "btn-danger", onDelete)
        );

        VBox card = wrapCard(title, fiche, meds, pos, freq, dateL, new Separator(), actions);
        card.setUserData(o);
        return card;
    }

    /** Carte fiche sans actions (patient / médecin consultation). */
    public static VBox ficheCardViewOnly(Fiche f, String patientLabel) {
        Label allergy = wrapMetaComfort("Allergies / chroniques : "
                + shorten(nullSafe(f.getAllergie()) + " · " + nullSafe(f.getMaladieChronique()), 220));
        VBox card = wrapCardComfort(titleComfort("📋 " + nullSafe(f.getLibelleMaladie())),
                metaComfort("👤 Patient : " + nullSafe(patientLabel)),
                metaComfort(String.format("⚖ %.1f kg · %.0f cm · glycémie %.1f", f.getPoids(), f.getTaille(), f.getGlycemie())),
                metaComfort("🩸 " + nullSafe(f.getGrpSanguin()) + " · tension " + nullSafe(f.getTension())),
                metaComfort("📅 " + (f.getDate() != null ? f.getDate().toString() : "—")),
                metaComfort("⚠ Gravité : " + nullSafe(f.getGravite())),
                allergy);
        card.setUserData(f);
        return card;
    }

    /** Carte ordonnance avec bouton export PDF (patient). */
    public static VBox ordonnanceCardWithPdfExport(Ordonnance o, String ficheSummary, String medicamentsSummary,
                                                   Runnable onExportPdf) {
        String d = o.getDate() != null ? o.getDate().toString() : "—";
        HBox actions = actionRowSingle(comfortButton("Exporter en PDF", "btn-primary", onExportPdf));
        VBox card = wrapCardComfort(
                titleComfort("📜 Ordonnance"),
                metaComfort("📎 Fiche : " + nullSafe(ficheSummary)),
                wrapMetaComfort("💊 Médicaments : " + shorten(nullSafe(medicamentsSummary), 200)),
                wrapMetaComfort("Posologie : " + shorten(nullSafe(o.getPosologie()), 320)),
                metaComfort("⏱ " + nullSafe(o.getFrequence()) + " · durée " + o.getDureeTraitement() + " jour(s)"),
                metaComfort("📅 " + d),
                new Separator(),
                actions);
        card.setUserData(o);
        return card;
    }

    /** Carte ordonnance lecture seule (médecin). */
    public static VBox ordonnanceCardViewOnly(Ordonnance o, String ficheSummary, String medicamentsSummary) {
        String d = o.getDate() != null ? o.getDate().toString() : "—";
        VBox card = wrapCardComfort(
                titleComfort("📜 Ordonnance"),
                metaComfort("📎 Fiche : " + nullSafe(ficheSummary)),
                wrapMetaComfort("💊 Médicaments : " + shorten(nullSafe(medicamentsSummary), 200)),
                wrapMetaComfort("Posologie : " + shorten(nullSafe(o.getPosologie()), 320)),
                metaComfort("⏱ " + nullSafe(o.getFrequence()) + " · durée " + o.getDureeTraitement() + " jour(s)"),
                metaComfort("📅 " + d));
        card.setUserData(o);
        return card;
    }

    private static HBox actionRowSingle(Button primary) {
        HBox h = new HBox(10, primary);
        h.setAlignment(Pos.CENTER_LEFT);
        h.setPadding(new Insets(8, 0, 0, 0));
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        return new HBox(10, h, spacer);
    }

    /** Cartes larges pour espaces patient / médecin. */
    private static VBox wrapCardComfort(javafx.scene.Node... nodes) {
        VBox v = new VBox(12);
        v.getChildren().addAll(nodes);
        v.setPadding(new Insets(22, 24, 22, 24));
        v.setMinWidth(400);
        v.setPrefWidth(520);
        v.setMaxWidth(780);
        v.getStyleClass().addAll("clinical-entity-card", "clinical-entity-card-comfort");
        v.setAlignment(Pos.TOP_LEFT);
        return v;
    }

    private static Label titleComfort(String text) {
        Label l = new Label(text);
        l.setWrapText(true);
        l.setFont(Font.font("Segoe UI", 20));
        l.setStyle("-fx-font-weight: bold; -fx-text-fill: #008f85;");
        return l;
    }

    private static Label metaComfort(String text) {
        Label l = new Label(text);
        l.setWrapText(true);
        l.setStyle("-fx-text-fill: #2c3e50; -fx-font-size: 15px;");
        return l;
    }

    private static Label wrapMetaComfort(String text) {
        Label l = metaComfort(text);
        l.setWrapText(true);
        l.setMaxWidth(Double.MAX_VALUE);
        return l;
    }

    private static Button comfortButton(String text, String styleClass, Runnable action) {
        Button b = new Button(text);
        b.getStyleClass().add(styleClass);
        b.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-padding: 10 22;");
        b.setMinHeight(40);
        b.setOnAction(e -> action.run());
        return b;
    }

    private static VBox wrapCard(javafx.scene.Node... nodes) {
        VBox v = new VBox(8);
        v.getChildren().addAll(nodes);
        v.setPadding(new Insets(16));
        v.setMinWidth(280);
        v.setPrefWidth(300);
        v.setMaxWidth(340);
        v.getStyleClass().add("clinical-entity-card");
        v.setAlignment(Pos.TOP_LEFT);
        return v;
    }

    private static Label titled(String text) {
        Label l = new Label(text);
        l.setWrapText(true);
        l.setFont(Font.font("Segoe UI", 16));
        l.setStyle("-fx-font-weight: bold; -fx-text-fill: #00b3a6;");
        return l;
    }

    private static Label meta(String text) {
        Label l = new Label(text);
        l.setWrapText(true);
        l.setStyle("-fx-text-fill: #34495e; -fx-font-size: 13px;");
        return l;
    }

    private static Label wrapMeta(String text) {
        Label l = meta(text);
        l.setWrapText(true);
        return l;
    }

    private static HBox actionRow(Button edit, Button del) {
        HBox h = new HBox(10, edit, del);
        h.setAlignment(Pos.CENTER_LEFT);
        h.setPadding(new Insets(8, 0, 0, 0));
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        return new HBox(10, h, spacer);
    }

    private static Button smallButton(String text, String styleClass, Runnable action) {
        Button b = new Button(text);
        b.getStyleClass().add(styleClass);
        b.setStyle("-fx-font-size: 12px; -fx-padding: 6 14;");
        b.setOnAction(e -> action.run());
        return b;
    }

    private static String nullSafe(String s) {
        return s == null ? "" : s;
    }

    private static String shorten(String s, int max) {
        if (s == null) {
            return "";
        }
        String t = s.trim();
        return t.length() <= max ? t : t.substring(0, max - 1) + "…";
    }
}
