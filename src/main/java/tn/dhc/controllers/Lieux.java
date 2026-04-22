package tn.dhc.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import tn.dhc.entities.Lieu;
import tn.dhc.services.LieuService;

import java.util.List;
import java.util.stream.Collectors;

public class Lieux {

    @FXML
    private ListView<Lieu> AffLieux;

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
    private ObservableList<Lieu> lieuxList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        loadLieux();
    }

    private void loadLieux() {
        List<Lieu> lieux = lieuService.getAll();
        lieuxList.setAll(lieux);
        AffLieux.setItems(lieuxList);
    }

    @FXML
    void RechLieu(ActionEvent event) {
        String search = RechLieuText.getText().toLowerCase();

        List<Lieu> filtered = lieuService.getAll().stream()
                .filter(l -> l.getNomLieu().toLowerCase().contains(search)
                        || l.getVille().toLowerCase().contains(search))
                .collect(Collectors.toList());

        AffLieux.setItems(FXCollections.observableArrayList(filtered));
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
    void deleteLieu(ActionEvent event) {
        Lieu selected = AffLieux.getSelectionModel().getSelectedItem();

        if (selected != null) {
            lieuService.supprimer(selected);
            loadLieux();
        }
    }

    @FXML
    void editLieu(ActionEvent event) {

        Lieu selected = AffLieux.getSelectionModel().getSelectedItem();

        if (selected == null) {
            System.out.println("Aucun lieu sélectionné");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierLieu.fxml"));
            Parent root = loader.load();

            ModifierLieu controller = loader.getController();

            controller.setLieu(selected);

            Stage stage = (Stage) AffLieux.getScene().getWindow();
            stage.setScene(new Scene(root));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void refreshLieux(ActionEvent event) {
        loadLieux();
    }
}