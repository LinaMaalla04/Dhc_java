package tn.dhc.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.dhc.entities.Rdv;
import tn.dhc.services.RdvService;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class Rdvs {

    @FXML
    private VBox rdvCardsContainer;

    @FXML
    private TextField RechRdvText;

    @FXML
    private DatePicker addCreneauDate;

    @FXML
    private TextField addRdvMotif;

    @FXML
    private ComboBox<String> addRdvPriorite;

    @FXML
    private ComboBox<String> addRdvStatut;

    private RdvService rdvService = new RdvService();

    @FXML
    public void initialize() {
        addRdvPriorite.setItems(FXCollections.observableArrayList("Faible", "Moyenne", "Élevée"));
        addRdvStatut.setItems(FXCollections.observableArrayList("En attente", "Confirmé", "Annulé"));

        loadRdv();
    }

    private void loadRdv() {
        renderRdvCards(rdvService.getAll());
    }

    private void renderRdvCards(List<Rdv> list) {
        if (rdvCardsContainer == null) return;
        rdvCardsContainer.getChildren().clear();
        if (list.isEmpty()) {
            Label empty = new Label("Aucun rendez-vous trouvé.");
            empty.setStyle("-fx-text-fill:#999; -fx-font-size:14px;");
            rdvCardsContainer.getChildren().add(empty);
            return;
        }
        for (Rdv r : list) rdvCardsContainer.getChildren().add(buildRdvCard(r));
    }

    private HBox buildRdvCard(Rdv r) {
        HBox card = new HBox(16);
        card.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        card.setPadding(new javafx.geometry.Insets(14, 18, 14, 18));

        // Color by statut
        String statut = r.getStatut() != null ? r.getStatut().toLowerCase() : "";
        String bg, border;
        if (statut.contains("confirm")) { bg = "#e8f5e9"; border = "#2ecc71"; }
        else if (statut.contains("annul"))  { bg = "#fce4e4"; border = "#e74c3c"; }
        else { bg = "#fff8e1"; border = "#f39c12"; }

        card.setStyle("-fx-background-color:" + bg + "; -fx-border-color:" + border +
                "; -fx-border-radius:10; -fx-background-radius:10;" +
                " -fx-effect:dropshadow(gaussian,rgba(0,0,0,0.06),6,0,0,2);");

        // Priority icon
        String prioEmoji = "🟡";
        if (r.getPriorite() != null) {
            String p = r.getPriorite().toLowerCase();
            if (p.contains("urgent") || p.contains("élevée") || p.contains("elevee")) prioEmoji = "🔴";
            else if (p.contains("faible")) prioEmoji = "🟢";
        }
        Label icon = new Label(prioEmoji);
        icon.setStyle("-fx-font-size:26px;");

        VBox info = new VBox(3);
        HBox.setHgrow(info, javafx.scene.layout.Priority.ALWAYS);

        Label motif = new Label("📋 " + (r.getMotif() != null ? r.getMotif() : "—"));
        motif.setStyle("-fx-font-size:15px; -fx-font-weight:bold; -fx-text-fill:#2c3e50;");

        Label date = new Label("📅 " + (r.getDateRdv() != null ? r.getDateRdv() : "—") +
                "   👤 Patient #" + r.getUserId() + "   📎 Créneau #" + r.getCreneauId());
        date.setStyle("-fx-font-size:12px; -fx-text-fill:#7f8c8d;");

        String statutLabel = statut.contains("confirm") ? "✅ Confirmé"
                : statut.contains("annul")   ? "❌ Annulé"
                : "⏳ En attente";
        Label statutLbl = new Label(statutLabel + "   •   Priorité : " +
                (r.getPriorite() != null ? r.getPriorite() : "—"));
        statutLbl.setStyle("-fx-font-size:12px; -fx-font-weight:bold; -fx-text-fill:" + border + ";");

        info.getChildren().addAll(motif, date, statutLbl);

        Button btnEdit = new Button("🖊 Modifier");
        btnEdit.setStyle("-fx-background-color:#00b3a6; -fx-text-fill:white; -fx-background-radius:7;" +
                " -fx-font-size:12px; -fx-padding:7 14; -fx-cursor:hand;");
        btnEdit.setOnAction(ev -> editRdvCard(r));

        Button btnDel = new Button("❌ Supprimer");
        btnDel.setStyle("-fx-background-color:#e74c3c; -fx-text-fill:white; -fx-background-radius:7;" +
                " -fx-font-size:12px; -fx-padding:7 14; -fx-cursor:hand;");
        btnDel.setOnAction(ev -> {
            rdvService.delete(r.getId());
            loadRdv();
        });

        card.getChildren().addAll(icon, info, btnEdit, btnDel);
        return card;
    }

    private void editRdvCard(Rdv r) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/ModifierRdv.fxml"));
            javafx.scene.Parent root = loader.load();
            ModifierRdv controller = loader.getController();
            controller.setRdv(r);
            Stage stage = (Stage) rdvCardsContainer.getScene().getWindow();
            stage.setScene(new javafx.scene.Scene(root));
        } catch (Exception e) { e.printStackTrace(); }
    }


    @FXML
    void RechRdv(ActionEvent event) {
        String search = RechRdvText.getText().toLowerCase();

        List<Rdv> filtered = rdvService.getAll().stream()
                .filter(r -> r.getMotif().toLowerCase().contains(search)
                        || r.getPriorite().toLowerCase().contains(search)
                        || r.getStatut().toLowerCase().contains(search))
                .collect(Collectors.toList());

        renderRdvCards(filtered);
    }

    @FXML
    void ajouterRdv(ActionEvent event) {
        try {
            String motif = addRdvMotif.getText();
            String priorite = addRdvPriorite.getValue();
            String statut = addRdvStatut.getValue();
            LocalDate date = addCreneauDate.getValue();

            int creneauId = 1;
            int userId = 1;

            Rdv r = new Rdv(0, motif, priorite, statut, date, creneauId, userId);
            rdvService.add(r);

            loadRdv();
            annuler(null);

        } catch (Exception e) {
            System.out.println("Erreur ajout RDV: " + e.getMessage());
        }
    }


    @FXML
    void annuler(ActionEvent event) {
        addRdvMotif.clear();
        addRdvPriorite.setValue(null);
        addRdvStatut.setValue(null);
        addCreneauDate.setValue(null);
    }

    @FXML
    void deleteRdv(ActionEvent event) { /* handled via card buttons */ }

    @FXML
    void editRdv(ActionEvent event) { /* handled via card buttons */ }
    @FXML
    void refreshRdv(ActionEvent event) {
        loadRdv();
    }
}