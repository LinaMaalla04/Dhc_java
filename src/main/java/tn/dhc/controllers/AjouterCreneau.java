package tn.dhc.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
<<<<<<< HEAD
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.dhc.entities.Creneau;
import tn.dhc.services.CreneauService;

import java.io.IOException;
=======
import javafx.scene.control.*;
import tn.dhc.entities.Creneau;
import tn.dhc.services.CreneauService;

>>>>>>> 34f983539afbf1fbe48f8fe4cc4c438bec376064
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

<<<<<<< HEAD
=======
    // INIT COMBOBOX
>>>>>>> 34f983539afbf1fbe48f8fe4cc4c438bec376064
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

<<<<<<< HEAD

=======
    // AJOUT BDD
>>>>>>> 34f983539afbf1fbe48f8fe4cc4c438bec376064
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

<<<<<<< HEAD
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Dashboard.fxml"));
            Parent root = loader.load();

            Dashboard controller = loader.getController();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
=======
            annuler(event);
>>>>>>> 34f983539afbf1fbe48f8fe4cc4c438bec376064

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

<<<<<<< HEAD
=======
    // RESET
>>>>>>> 34f983539afbf1fbe48f8fe4cc4c438bec376064
    @FXML
    void annuler(ActionEvent event) {
        addCreneauDate.setValue(null);
        addCreneauDebut.setValue(null);
        addCreneauFin.setValue(null);
<<<<<<< HEAD
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Dashboard.fxml"));
        Parent root = null;
        try {
            root = loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Dashboard controller = loader.getController();

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
    }

=======
    }

    // ALERT
>>>>>>> 34f983539afbf1fbe48f8fe4cc4c438bec376064
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}