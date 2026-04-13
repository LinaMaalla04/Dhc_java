package tn.dhc.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import tn.dhc.entities.Fiche;
import tn.dhc.entities.Medicament;
import tn.dhc.entities.Ordonnance;
import tn.dhc.entities.User;
import tn.dhc.services.ServiceFiche;
import tn.dhc.services.ServiceOrdonnance;
import tn.dhc.services.UserService;
import tn.dhc.utils.OrdonnancePdfExporter;

import java.nio.file.Path;
import java.util.List;

public class Front {

    @FXML
    private FlowPane patientFichesFlow;

    @FXML
    private FlowPane patientOrdonnancesFlow;

    @FXML
    private Label reportLabel;

    private final UserService userService = new UserService();
    private final ServiceFiche ficheService = new ServiceFiche();
    private final ServiceOrdonnance ordonnanceService = new ServiceOrdonnance();

    @FXML
    public void initialize() {
        if (patientFichesFlow != null) {
            bindFlowWrap(patientFichesFlow);
        }
        if (patientOrdonnancesFlow != null) {
            bindFlowWrap(patientOrdonnancesFlow);
        }
        refreshPatientMedical();
        javafx.application.Platform.runLater(this::initAnnoncesPatient);
    }

    private static void bindFlowWrap(FlowPane flow) {
        if (flow == null) {
            return;
        }
        ScrollPane sp = null;
        for (javafx.scene.Parent walk = flow.getParent(); walk != null; walk = walk.getParent()) {
            if (walk instanceof ScrollPane scroll) {
                sp = scroll;
                break;
            }
        }
        if (sp == null) {
            return;
        }
        javafx.beans.value.ChangeListener<Number> listener = (obs, old, w) ->
                flow.setPrefWrapLength(Math.max(480, w.doubleValue() - 56));
        sp.widthProperty().addListener(listener);
        listener.changed(sp.widthProperty(), sp.getWidth(), sp.getWidth());
    }

    @FXML
    public void refreshPatientMedical() {
        if (patientFichesFlow == null || patientOrdonnancesFlow == null) {
            return;
        }
        patientFichesFlow.getChildren().clear();
        patientOrdonnancesFlow.getChildren().clear();
        User me = UserService.getCurrentUser();
        if (me == null) {
            return;
        }
        String selfLabel = me.getPrenom() + " " + me.getNom();
        for (Fiche f : ficheService.findByPatientUserId(me.getId())) {
            patientFichesFlow.getChildren().add(MedicalAdminCards.ficheCardViewOnly(f, selfLabel));
        }
        for (Ordonnance o : ordonnanceService.findByPatientUserId(me.getId())) {
            Fiche linked = ficheService.getOneById(o.getFicheId());
            String ficheSummary = formatFicheSummary(linked);
            String medSummary = ordonnanceService.getMedicamentsSummaryForOrdonnance(o.getId());
            patientOrdonnancesFlow.getChildren().add(
                    MedicalAdminCards.ordonnanceCardWithPdfExport(o, ficheSummary, medSummary,
                            () -> exportOrdonnanceToPdf(o)));
        }
    }

    private static String formatFicheSummary(Fiche f) {
        if (f == null) {
            return "Fiche liée";
        }
        String d = f.getDate() != null ? f.getDate().toString() : "";
        return (f.getLibelleMaladie() != null ? f.getLibelleMaladie() : "Fiche") + " · " + d;
    }

    private void exportOrdonnanceToPdf(Ordonnance sel) {
        User me = UserService.getCurrentUser();
        if (me == null || patientOrdonnancesFlow == null) {
            return;
        }
        Fiche fiche = ficheService.getOneById(sel.getFicheId());
        if (fiche == null) {
            alert(Alert.AlertType.ERROR, "Fiche liée introuvable.");
            return;
        }
        List<Medicament> meds = ordonnanceService.findMedicamentsByOrdonnance(sel.getId());
        User prescripteur = sel.getMedecinUserId() != null ? userService.getOneById(sel.getMedecinUserId()) : null;

        FileChooser fc = new FileChooser();
        fc.setTitle("Enregistrer l'ordonnance PDF");
        fc.setInitialFileName("ordonnance-" + sel.getId() + ".pdf");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
        Stage stage = (Stage) patientOrdonnancesFlow.getScene().getWindow();
        java.io.File file = fc.showSaveDialog(stage);
        if (file == null) {
            return;
        }
        Path path = file.toPath();
        try {
            OrdonnancePdfExporter.write(sel, fiche, me, prescripteur, meds, path);
            alert(Alert.AlertType.INFORMATION, "PDF enregistré :\n" + path.toAbsolutePath());
        } catch (Exception ex) {
            ex.printStackTrace();
            alert(Alert.AlertType.ERROR, "Impossible d'écrire le PDF : " + ex.getMessage());
        }
    }

    @FXML
    public void deconnexion(ActionEvent event) {
        new UserService().logout();
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Login.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void alert(Alert.AlertType type, String msg) {
        Alert a = new Alert(type);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  ANNONCES – PATIENT
    // ═══════════════════════════════════════════════════════════════════════════

    @javafx.fxml.FXML private javafx.scene.control.TextField annoncePatientSearch;
    @javafx.fxml.FXML private javafx.scene.control.ComboBox<String> annoncePatientSort;
    @javafx.fxml.FXML private javafx.scene.layout.VBox annoncePatientContainer;

    private final tn.dhc.services.ServiceAnnonce svcAnnonce = new tn.dhc.services.ServiceAnnonce();
    private final tn.dhc.services.ServiceCommentaire svcCommentaire = new tn.dhc.services.ServiceCommentaire();

    private void initAnnoncesPatient() {
        if (annoncePatientSort != null) {
            annoncePatientSort.setItems(javafx.collections.FXCollections.observableArrayList(
                "📅 Date (récente → ancienne)",
                "📅 Date (ancienne → récente)",
                "🔴 Urgence (haute → basse)",
                "🟢 Urgence (basse → haute)",
                "✅ Disponible en premier"
            ));
            annoncePatientSort.setOnAction(e -> filterPatientAnnonces());
        }
        if (annoncePatientSearch != null) {
            annoncePatientSearch.textProperty().addListener((obs, o, n) -> filterPatientAnnonces());
        }
        loadPatientAnnonces();
    }

    private void filterPatientAnnonces() {
        String keyword = annoncePatientSearch != null ? annoncePatientSearch.getText().trim() : "";
        String sortLabel = annoncePatientSort != null ? annoncePatientSort.getValue() : null;
        String sortKey = labelToKey(sortLabel);
        try {
            java.util.List<tn.dhc.entities.Annonce> list;
            if (!keyword.isEmpty())     list = svcAnnonce.rechercher(keyword);
            else if (sortKey != null)   list = svcAnnonce.trier(sortKey);
            else                        list = svcAnnonce.getAll();
            renderPatientCards(list);
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void loadPatientAnnonces() {
        try { renderPatientCards(svcAnnonce.getAll()); }
        catch (Exception ex) { ex.printStackTrace(); }
    }

    private String labelToKey(String label) {
        if (label == null) return null;
        if (label.contains("récente"))   return "DATE_DESC";
        if (label.contains("ancienne"))  return "DATE_ASC";
        if (label.contains("haute"))     return "URGENCE_HIGH";
        if (label.contains("basse"))     return "URGENCE_LOW";
        if (label.contains("Disponible")) return "DISPO";
        return null;
    }

    private void renderPatientCards(java.util.List<tn.dhc.entities.Annonce> annonces) {
        if (annoncePatientContainer == null) return;
        annoncePatientContainer.getChildren().clear();
        if (annonces.isEmpty()) {
            javafx.scene.control.Label empty = new javafx.scene.control.Label("Aucune annonce disponible.");
            empty.setStyle("-fx-text-fill:#95a5a6; -fx-font-size:14px; -fx-padding:30;");
            annoncePatientContainer.getChildren().add(empty);
            return;
        }
        for (tn.dhc.entities.Annonce a : annonces) {
            annoncePatientContainer.getChildren().add(buildPatientCard(a));
        }
    }

    private javafx.scene.layout.HBox buildPatientCard(tn.dhc.entities.Annonce a) {
        javafx.scene.layout.HBox card = new javafx.scene.layout.HBox(12);
        card.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color:white; -fx-background-radius:12; -fx-padding:14 18; " +
                "-fx-border-radius:12; -fx-border-width:1.5; -fx-border-color:" + urgenceBorder(a.getUrgence()) + "; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.07), 10, 0, 0, 3);");

        javafx.scene.control.Label dot = new javafx.scene.control.Label(urgenceEmoji(a.getUrgence()));
        dot.setStyle("-fx-font-size:18px;");

        javafx.scene.layout.VBox info = new javafx.scene.layout.VBox(3);
        javafx.scene.control.Label titre = new javafx.scene.control.Label(a.getTitre_annonce());
        titre.setStyle("-fx-font-size:15px; -fx-font-weight:bold; -fx-text-fill:#2c3e50;");
        javafx.scene.control.Label meta = new javafx.scene.control.Label(
            "📅 " + a.getDate_publication() + "   " + etatLbl(a.getEtat_annonce()));
        meta.setStyle("-fx-font-size:12px; -fx-text-fill:#7f8c8d;");
        info.getChildren().addAll(titre, meta);
        javafx.scene.layout.HBox.setHgrow(info, javafx.scene.layout.Priority.ALWAYS);

        javafx.scene.control.Button btnVoir = patBtn("👁 Voir", "#00b3a6", "white");
        btnVoir.setOnAction(e -> openPatientViewPopup(a));

        card.getChildren().addAll(dot, info, btnVoir);
        return card;
    }

    /** Patient view popup: shows annonce + comment area */
    private void openPatientViewPopup(tn.dhc.entities.Annonce a) {
        User me = UserService.getCurrentUser();
        if (me == null) return;

        javafx.stage.Stage popup = new javafx.stage.Stage();
        popup.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        popup.setTitle("📢 " + a.getTitre_annonce());

        javafx.scene.layout.VBox root = new javafx.scene.layout.VBox(14);
        root.setStyle("-fx-background-color:white; -fx-padding:28;");
        root.setPrefWidth(560);

        // urgence badge
        javafx.scene.control.Label urgBadge = new javafx.scene.control.Label(
            urgenceEmoji(a.getUrgence()) + "  " + a.getUrgence());
        urgBadge.setStyle("-fx-font-size:13px; -fx-font-weight:bold; -fx-text-fill:white; " +
            "-fx-background-color:" + urgenceBg(a.getUrgence()) + "; -fx-background-radius:20; -fx-padding:4 14;");

        javafx.scene.control.Label titre = new javafx.scene.control.Label(a.getTitre_annonce());
        titre.setStyle("-fx-font-size:22px; -fx-font-weight:bold; -fx-text-fill:#2c3e50;");
        titre.setWrapText(true);

        javafx.scene.control.Label dateLbl = new javafx.scene.control.Label(
            "📅 Publié le : " + a.getDate_publication() + "   " + etatLbl(a.getEtat_annonce()));
        dateLbl.setStyle("-fx-text-fill:#7f8c8d; -fx-font-size:13px;");

        javafx.scene.control.Label descTitle = new javafx.scene.control.Label("Description");
        descTitle.setStyle("-fx-font-size:14px; -fx-font-weight:bold; -fx-text-fill:#00b3a6;");

        javafx.scene.control.TextArea descArea = new javafx.scene.control.TextArea(a.getDescription());
        descArea.setEditable(false);
        descArea.setWrapText(true);
        descArea.setPrefRowCount(4);
        descArea.setStyle("-fx-background-color:#f8fffe; -fx-border-color:#e0f2f0; -fx-background-radius:8; -fx-border-radius:8;");

        // ── Comment section ────────────────────────────────────────────────────
        javafx.scene.layout.VBox commentSection = buildCommentSection(a, me, popup);

        javafx.scene.control.Button btnClose = patBtn("✖ Fermer", "#95a5a6", "white");
        btnClose.setOnAction(e -> popup.close());

        root.getChildren().addAll(urgBadge, titre, dateLbl, descTitle, descArea, commentSection, btnClose);

        javafx.scene.control.ScrollPane sp = new javafx.scene.control.ScrollPane(root);
        sp.setFitToWidth(true);
        sp.setHbarPolicy(javafx.scene.control.ScrollPane.ScrollBarPolicy.NEVER);
        javafx.scene.Scene scene = new javafx.scene.Scene(sp, 580, 600);
        popup.setScene(scene);
        popup.showAndWait();
    }

    /** Builds the comment sub-panel inside the patient view popup */
    private javafx.scene.layout.VBox buildCommentSection(
            tn.dhc.entities.Annonce a, User me, javafx.stage.Stage parentPopup) {

        javafx.scene.layout.VBox section = new javafx.scene.layout.VBox(10);
        section.setStyle("-fx-background-color:#f8fffe; -fx-background-radius:10; " +
                         "-fx-border-color:#e0f2f0; -fx-border-radius:10; -fx-padding:14;");

        javafx.scene.control.Label secTitle = new javafx.scene.control.Label("💬 Mon commentaire");
        secTitle.setStyle("-fx-font-size:14px; -fx-font-weight:bold; -fx-text-fill:#00b3a6;");

        javafx.scene.control.Label errLbl = new javafx.scene.control.Label();
        errLbl.setStyle("-fx-text-fill:#e74c3c; -fx-font-size:12px;");
        errLbl.setWrapText(true);

        // Fetch existing comment
        tn.dhc.entities.Commentaire existing;
        try { existing = svcCommentaire.getUserComment(a.getIdAnnonce(), me.getId()); }
        catch (Exception ex) { existing = null; }

        final tn.dhc.entities.Commentaire[] commentHolder = { existing };

        javafx.scene.control.TextArea taComment = new javafx.scene.control.TextArea(
            existing != null ? existing.getBody() : "");
        taComment.setPromptText("Écrire votre commentaire ici...");
        taComment.setPrefRowCount(3);
        taComment.setWrapText(true);
        taComment.setStyle("-fx-background-radius:8; -fx-border-radius:8; -fx-border-color:#bdc3c7;");
        // Disable if already commented (read-only mode, buttons allow edit)
        taComment.setEditable(existing == null);

        javafx.scene.layout.HBox btnRow = new javafx.scene.layout.HBox(10);
        btnRow.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);

        if (existing == null) {
            // No comment yet — show "Envoyer"
            javafx.scene.control.Button btnSend = patBtn("📨 Envoyer", "#00b3a6", "white");
            btnSend.setOnAction(e -> {
                String txt = taComment.getText().trim();
                if (txt.isEmpty()) { errLbl.setText("• Le commentaire ne peut pas être vide."); return; }
                if (txt.length() > 1000) { errLbl.setText("• Le commentaire est trop long (max 1000 caractères)."); return; }
                try {
                    tn.dhc.entities.Commentaire c = new tn.dhc.entities.Commentaire(txt, a.getIdAnnonce(), me.getId());
                    svcCommentaire.ajouter(c);
                    commentHolder[0] = svcCommentaire.getUserComment(a.getIdAnnonce(), me.getId());
                    taComment.setEditable(false);
                    errLbl.setText("");
                    section.getChildren().clear();
                    section.getChildren().addAll(secTitle,
                        new javafx.scene.control.Label("✅ Commentaire envoyé !"),
                        buildCommentEditDeleteRow(a, me, section, secTitle, taComment, commentHolder, errLbl));
                } catch (Exception ex) { errLbl.setText("Erreur : " + ex.getMessage()); }
            });
            btnRow.getChildren().add(btnSend);
        } else {
            // Comment exists — show edit/delete buttons
            taComment.setEditable(false);
            javafx.scene.layout.VBox editRow = buildCommentEditDeleteRow(
                a, me, section, secTitle, taComment, commentHolder, errLbl);
            section.getChildren().addAll(secTitle, taComment, editRow, errLbl);
            return section;
        }

        section.getChildren().addAll(secTitle, taComment, btnRow, errLbl);
        return section;
    }

    private javafx.scene.layout.VBox buildCommentEditDeleteRow(
            tn.dhc.entities.Annonce a, User me,
            javafx.scene.layout.VBox section,
            javafx.scene.control.Label secTitle,
            javafx.scene.control.TextArea taComment,
            tn.dhc.entities.Commentaire[] holder,
            javafx.scene.control.Label errLbl) {

        javafx.scene.layout.HBox btnRow = new javafx.scene.layout.HBox(10);
        btnRow.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);

        javafx.scene.control.Button btnEdit = patBtn("✏️ Modifier", "#3498db", "white");
        javafx.scene.control.Button btnDel  = patBtn("🗑️ Supprimer", "#e74c3c", "white");

        btnEdit.setOnAction(e -> {
            if (!taComment.isEditable()) {
                taComment.setEditable(true);
                btnEdit.setText("💾 Enregistrer");
            } else {
                // Save
                String txt = taComment.getText().trim();
                if (txt.isEmpty())        { errLbl.setText("• Le commentaire ne peut pas être vide."); return; }
                if (txt.length() > 1000)  { errLbl.setText("• Max 1000 caractères."); return; }
                try {
                    holder[0].setBody(txt);
                    svcCommentaire.modifier(holder[0]);
                    taComment.setEditable(false);
                    btnEdit.setText("✏️ Modifier");
                    errLbl.setText("✅ Commentaire mis à jour.");
                } catch (Exception ex) { errLbl.setText("Erreur : " + ex.getMessage()); }
            }
        });

        btnDel.setOnAction(e -> {
            javafx.scene.control.Alert conf = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
            conf.setHeaderText(null);
            conf.setContentText("Supprimer votre commentaire ?");
            if (conf.showAndWait().orElse(javafx.scene.control.ButtonType.CANCEL) == javafx.scene.control.ButtonType.OK) {
                try {
                    svcCommentaire.supprimer(holder[0].getId());
                    holder[0] = null;
                    taComment.clear();
                    taComment.setEditable(true);
                    section.getChildren().clear();
                    section.getChildren().addAll(secTitle, taComment,
                        buildFreshSendRow(a, me, section, secTitle, taComment, holder, errLbl), errLbl);
                } catch (Exception ex) { errLbl.setText("Erreur : " + ex.getMessage()); }
            }
        });

        btnRow.getChildren().addAll(btnEdit, btnDel);
        javafx.scene.layout.VBox wrap = new javafx.scene.layout.VBox(8, taComment, btnRow, errLbl);
        return wrap;
    }

    private javafx.scene.layout.HBox buildFreshSendRow(
            tn.dhc.entities.Annonce a, User me,
            javafx.scene.layout.VBox section,
            javafx.scene.control.Label secTitle,
            javafx.scene.control.TextArea taComment,
            tn.dhc.entities.Commentaire[] holder,
            javafx.scene.control.Label errLbl) {

        javafx.scene.layout.HBox row = new javafx.scene.layout.HBox(10);
        row.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        javafx.scene.control.Button btnSend = patBtn("📨 Envoyer", "#00b3a6", "white");
        btnSend.setOnAction(e -> {
            String txt = taComment.getText().trim();
            if (txt.isEmpty())       { errLbl.setText("• Le commentaire ne peut pas être vide."); return; }
            if (txt.length() > 1000) { errLbl.setText("• Max 1000 caractères."); return; }
            try {
                tn.dhc.entities.Commentaire c = new tn.dhc.entities.Commentaire(txt, a.getIdAnnonce(), me.getId());
                svcCommentaire.ajouter(c);
                holder[0] = svcCommentaire.getUserComment(a.getIdAnnonce(), me.getId());
                taComment.setEditable(false);
                errLbl.setText("");
                section.getChildren().clear();
                section.getChildren().addAll(secTitle,
                    buildCommentEditDeleteRow(a, me, section, secTitle, taComment, holder, errLbl));
            } catch (Exception ex) { errLbl.setText("Erreur : " + ex.getMessage()); }
        });
        row.getChildren().add(btnSend);
        return row;
    }

    // ── Small helpers ─────────────────────────────────────────────────────────

    private static javafx.scene.control.Button patBtn(String text, String bg, String fg) {
        javafx.scene.control.Button b = new javafx.scene.control.Button(text);
        b.setStyle("-fx-background-color:" + bg + "; -fx-text-fill:" + fg + "; " +
                   "-fx-background-radius:8; -fx-padding:7 14; -fx-font-weight:bold; -fx-cursor:hand;");
        b.setOnMouseEntered(e -> b.setOpacity(0.85));
        b.setOnMouseExited(e -> b.setOpacity(1.0));
        return b;
    }

    private static String urgenceEmoji(String u) {
        if (u == null) return "⚪";
        return switch (u.toUpperCase()) {
            case "ROUGE"  -> "🔴";
            case "ORANGE" -> "🟠";
            case "VERT"   -> "🟢";
            default       -> "⚪";
        };
    }

    private static String urgenceBorder(String u) {
        if (u == null) return "#e0e0e0";
        return switch (u.toUpperCase()) {
            case "ROUGE"  -> "#e74c3c";
            case "ORANGE" -> "#e67e22";
            case "VERT"   -> "#27ae60";
            default       -> "#bdc3c7";
        };
    }

    private static String urgenceBg(String u) {
        if (u == null) return "#95a5a6";
        return switch (u.toUpperCase()) {
            case "ROUGE"  -> "#e74c3c";
            case "ORANGE" -> "#e67e22";
            case "VERT"   -> "#27ae60";
            default       -> "#95a5a6";
        };
    }

    private static String etatLbl(String e) {
        if ("DISPONIBLE".equalsIgnoreCase(e))     return "✅ Disponible";
        if ("NON_DISPONIBLE".equalsIgnoreCase(e)) return "❌ Non disponible";
        return e != null ? e : "";
    }
}
