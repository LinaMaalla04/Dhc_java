package tn.dhc.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import tn.dhc.entities.Creneau;
import tn.dhc.services.CreneauService;

import java.time.LocalDate;
import java.time.LocalTime;

public class AjouterCreneau {

    @FXML
    private Button addCreneau;

    @FXML
    private Button annulerCreneau;

    @FXML
    private DatePicker addCreneauDate;

    @FXML
    private ComboBox<String> addCreneauDebut;

    @FXML
    private ComboBox<String> addCreneauFin;

    private final CreneauService creneauService = new CreneauService();

    // INIT COMBOBOX
    @FXML
    public void initialize() {

        for (int h = 8; h <= 18; h++) {
            addCreneauDebut.getItems().add(String.format("%02d:00", h));
            addCreneauDebut.getItems().add(String.format("%02d:30", h));

            addCreneauFin.getItems().add(String.format("%02d:00", h));
            addCreneauFin.getItems().add(String.format("%02d:30", h));
        }

        addCreneauDebut.setValue("08:00");
        addCreneauFin.setValue("08:30");
    }

    // AJOUT BDD
    @FXML
    void ajouterCreneau(ActionEvent event) {

        try {
            LocalDate date = addCreneauDate.getValue();
            String debutStr = addCreneauDebut.getValue();
            String finStr = addCreneauFin.getValue();

            if (date == null || debutStr == null || finStr == null) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez remplir tous les champs !");
                return;
            }

            LocalTime debut = LocalTime.parse(debutStr);
            LocalTime fin = LocalTime.parse(finStr);

            if (fin.isBefore(debut) || fin.equals(debut)) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Heure de fin invalide !");
                return;
            }

            int userId = 1;

            Creneau c = new Creneau(
                    date,
                    debut,
                    fin,
                    "Dispo",
                    userId
            );

            creneauService.add(c);

            showAlert(Alert.AlertType.INFORMATION, "Succès", "Créneau ajouté avec succès !");

            annuler(event);

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    // RESET
    @FXML
    void annuler(ActionEvent event) {
        addCreneauDate.setValue(null);
        addCreneauDebut.setValue(null);
        addCreneauFin.setValue(null);
    }

    // ALERT
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}