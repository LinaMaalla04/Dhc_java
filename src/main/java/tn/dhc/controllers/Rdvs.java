package tn.dhc.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.dhc.entities.Rdv;
import tn.dhc.services.RdvService;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class Rdvs {

    @FXML
    private ListView<Rdv> AffRdv;

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
    private ObservableList<Rdv> rdvList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        addRdvPriorite.setItems(FXCollections.observableArrayList("Faible", "Moyenne", "Élevée"));
        addRdvStatut.setItems(FXCollections.observableArrayList("En attente", "Confirmé", "Annulé"));

        loadRdv();
    }

    private void loadRdv() {
        List<Rdv> list = rdvService.getAll();
        rdvList.setAll(list);
        AffRdv.setItems(rdvList);

    }


    @FXML
    void RechRdv(ActionEvent event) {
        String search = RechRdvText.getText().toLowerCase();

        List<Rdv> filtered = rdvService.getAll().stream()
                .filter(r -> r.getMotif().toLowerCase().contains(search)
                        || r.getPriorite().toLowerCase().contains(search)
                        || r.getStatut().toLowerCase().contains(search))
                .collect(Collectors.toList());

        AffRdv.setItems(FXCollections.observableArrayList(filtered));
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
    void deleteRdv(ActionEvent event) {
        Rdv selected = AffRdv.getSelectionModel().getSelectedItem();

        if (selected != null) {
            rdvService.delete(selected.getId());
            loadRdv();
        }
    }

    @FXML
    void editRdv(ActionEvent event) {

        Rdv selected = AffRdv.getSelectionModel().getSelectedItem();

        if (selected == null) {
            System.out.println("Aucun RDV sélectionné");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierRdv.fxml"));
            Parent root = loader.load();

            ModifierRdv controller = loader.getController();
            controller.setRdv(selected);

            Stage stage = (Stage) AffRdv.getScene().getWindow();
            stage.setScene(new Scene(root));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML
    void refreshRdv(ActionEvent event) {
        loadRdv();
    }
}