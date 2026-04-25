package tn.dhc.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import tn.dhc.entities.Lieu;
import tn.dhc.services.LieuService;

import java.util.List;
import java.util.stream.Collectors;

public class Lieux {

    @FXML
    private VBox lieuCardsContainer;

    @FXML
    private TextField RechLieuText;

    @FXML
    private TextField addLieuAdresse;

    @FXML
    private TextField addLieuCapacite;

    @FXML
    private TextField addLieuNom;

    @FXML
    private TextField addLieuVille;

    private LieuService lieuService = new LieuService();

    @FXML
    public void initialize() {
        loadLieux();
    }

    private void loadLieux() {
        renderLieuCards(lieuService.getAll());
    }

    private void renderLieuCards(List<Lieu> list) {
        if (lieuCardsContainer == null) return;
        lieuCardsContainer.getChildren().clear();
        if (list.isEmpty()) {
            javafx.scene.control.Label empty = new javafx.scene.control.Label("Aucun lieu trouvé.");
            empty.setStyle("-fx-text-fill:#999; -fx-font-size:14px;");
            lieuCardsContainer.getChildren().add(empty);
            return;
        }
        for (Lieu l : list) lieuCardsContainer.getChildren().add(buildLieuCard(l));
    }

    private HBox buildLieuCard(Lieu l) {
        HBox card = new HBox(16);
        card.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        card.setPadding(new javafx.geometry.Insets(14, 18, 14, 18));
        boolean dispo = l.isDisponible();
        String bg     = dispo ? "#e8f5e9" : "#fce4e4";
        String border = dispo ? "#2ecc71" : "#e74c3c";
        card.setStyle("-fx-background-color:" + bg + "; -fx-border-color:" + border +
                "; -fx-border-radius:10; -fx-background-radius:10;" +
                " -fx-effect:dropshadow(gaussian,rgba(0,0,0,0.06),6,0,0,2);");

        javafx.scene.control.Label icon = new javafx.scene.control.Label("🏢");
        icon.setStyle("-fx-font-size:26px;");

        VBox info = new VBox(3);
        HBox.setHgrow(info, javafx.scene.layout.Priority.ALWAYS);

        javafx.scene.control.Label nom = new javafx.scene.control.Label(l.getNomLieu());
        nom.setStyle("-fx-font-size:15px; -fx-font-weight:bold; -fx-text-fill:#2c3e50;");

        javafx.scene.control.Label adresse = new javafx.scene.control.Label(
                "📍 " + l.getAdresse() + "  •  🏙️ " + l.getVille());
        adresse.setStyle("-fx-font-size:12px; -fx-text-fill:#7f8c8d;");

        String capStr = l.getCapaciteMax() != null ? "👥 Capacité : " + l.getCapaciteMax() : "👥 Capacité : —";
        String dispStr = dispo ? "✅ Disponible" : "❌ Indisponible";
        javafx.scene.control.Label meta = new javafx.scene.control.Label(capStr + "   " + dispStr);
        meta.setStyle("-fx-font-size:12px; -fx-text-fill:" + (dispo ? "#27ae60" : "#e74c3c") + "; -fx-font-weight:bold;");

        info.getChildren().addAll(nom, adresse, meta);

        javafx.scene.control.Button btnEdit = new javafx.scene.control.Button("🖊 Modifier");
        btnEdit.setStyle("-fx-background-color:#00b3a6; -fx-text-fill:white; -fx-background-radius:7;" +
                " -fx-font-size:12px; -fx-padding:7 14; -fx-cursor:hand;");
        btnEdit.setOnAction(ev -> editLieuCard(l));

        javafx.scene.control.Button btnDel = new javafx.scene.control.Button("❌ Supprimer");
        btnDel.setStyle("-fx-background-color:#e74c3c; -fx-text-fill:white; -fx-background-radius:7;" +
                " -fx-font-size:12px; -fx-padding:7 14; -fx-cursor:hand;");
        btnDel.setOnAction(ev -> {
            lieuService.supprimer(l);
            loadLieux();
        });

        card.getChildren().addAll(icon, info, btnEdit, btnDel);
        return card;
    }

    private void editLieuCard(Lieu l) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/ModifierLieu.fxml"));
            javafx.scene.Parent root = loader.load();
            ModifierLieu controller = loader.getController();
            controller.setLieu(l);
            Stage stage = (Stage) lieuCardsContainer.getScene().getWindow();
            stage.setScene(new javafx.scene.Scene(root));
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    void RechLieu(ActionEvent event) {
        String search = RechLieuText.getText().toLowerCase();

        List<Lieu> filtered = lieuService.getAll().stream()
                .filter(l -> l.getNomLieu().toLowerCase().contains(search)
                        || l.getVille().toLowerCase().contains(search))
                .collect(Collectors.toList());

        renderLieuCards(filtered);
    }

    @FXML
    void ajouterEvent(ActionEvent event) {
        try {
            String nom = addLieuNom.getText();
            String adresse = addLieuAdresse.getText();
            String ville = addLieuVille.getText();

            Integer capacite = null;
            if (!addLieuCapacite.getText().isEmpty()) {
                capacite = Integer.parseInt(addLieuCapacite.getText());
            }

            Lieu l = new Lieu(0, nom, adresse, ville, capacite, true);
            lieuService.ajouter(l);

            loadLieux();
            annuler(null);

        } catch (Exception e) {
            System.out.println("Erreur ajout : " + e.getMessage());
        }
    }

    @FXML
    void annuler(ActionEvent event) {
        addLieuNom.clear();
        addLieuAdresse.clear();
        addLieuVille.clear();
        addLieuCapacite.clear();
    }

    @FXML
    void deleteLieu(ActionEvent event) { /* handled via card buttons */ }

    @FXML
    void editLieu(ActionEvent event) { /* handled via card buttons */ }

    @FXML
    void refreshLieux(ActionEvent event) {
        loadLieux();
    }
}