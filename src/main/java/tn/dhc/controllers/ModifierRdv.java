package tn.dhc.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.dhc.entities.Rdv;
import tn.dhc.services.RdvService;

import java.time.LocalDate;

public class ModifierRdv {

    @FXML
    private DatePicker modCreneauDate;

    @FXML
    private TextField modRdvMotif;

    @FXML
    private ComboBox<String> modRdvPriorite;

    @FXML
    private ComboBox<String> modRdvStatut;

    private Rdv rdv;
    private RdvService rdvService = new RdvService();

    @FXML
    public void initialize() {
        modRdvPriorite.getItems().addAll("Faible", "Moyenne", "Élevée");
        modRdvStatut.getItems().addAll("En attente", "Confirmé", "Annulé");
    }

    public void setRdv(Rdv r) {
        this.rdv = r;

        if (r != null) {
            modRdvMotif.setText(r.getMotif());
            modRdvPriorite.setValue(r.getPriorite());
            modRdvStatut.setValue(r.getStatut());
            modCreneauDate.setValue(r.getDateRdv());
        }
    }

    @FXML
    void modifierRdv(ActionEvent event) {

        try {
            if (rdv == null) {
                System.out.println("Aucun RDV !");
                return;
            }

            String motif = modRdvMotif.getText();
            String priorite = modRdvPriorite.getValue();
            String statut = modRdvStatut.getValue();
            LocalDate date = modCreneauDate.getValue();

            if (motif.isEmpty() || priorite == null || statut == null || date == null) {
                showAlert("Erreur", "Veuillez remplir tous les champs !");
                return;
            }

            rdv.setMotif(motif);
            rdv.setPriorite(priorite);
            rdv.setStatut(statut);
            rdv.setDateRdv(date);

            rdvService.modifier(rdv);

            showAlert("Succès", "RDV modifié !");
            System.out.println("✅ RDV modifié");

            try {
                Parent root = FXMLLoader.load(getClass().getResource("/Rdvs.fxml"));
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(new Scene(root));
            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void annuler(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Rdvs.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String t, String m) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(t);
        alert.setContentText(m);
        alert.show();

    }
}