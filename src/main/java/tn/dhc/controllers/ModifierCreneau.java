package tn.dhc.controllers;

<<<<<<< HEAD
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.dhc.entities.Creneau;
import tn.dhc.services.CreneauService;

import java.time.LocalDate;
import java.time.LocalTime;

public class ModifierCreneau {

    @FXML
    private DatePicker modCreneauDate;

    @FXML
    private ComboBox<LocalTime> modCreneauDebut;

    @FXML
    private ComboBox<LocalTime> modCreneauFin;

    private Creneau creneau;
    private final CreneauService creneauService = new CreneauService();

    @FXML
    public void initialize() {
        for (int i = 8; i <= 18; i++) {
            modCreneauDebut.getItems().add(LocalTime.of(i, 0));
            modCreneauFin.getItems().add(LocalTime.of(i, 0));
        }
    }

    public void setCreneau(Creneau c) {
        this.creneau = c;

        if (c != null) {
            modCreneauDate.setValue(c.getDateCreneau());
            modCreneauDebut.setValue(c.getHdebut());
            modCreneauFin.setValue(c.getHfin());
        }
    }

    @FXML
    void modifierCreneau(ActionEvent event) {

        try {
            if (creneau == null) {
                System.out.println("Aucun créneau sélectionné !");
                return;
            }

            LocalDate date = modCreneauDate.getValue();
            LocalTime debut = modCreneauDebut.getValue();
            LocalTime fin = modCreneauFin.getValue();

            if (date == null || debut == null || fin == null) {
                showAlert("Erreur", "Veuillez remplir tous les champs");
                return;
            }

            if (fin.isBefore(debut)) {
                showAlert("Erreur", "Heure fin doit être après début");
                return;
            }

            creneau.setDateCreneau(date);
            creneau.setHdebut(debut);
            creneau.setHfin(fin);

            creneauService.modifier(creneau);

            showAlert("Succès", "Créneau modifié !");
            System.out.println("✅ Créneau modifié !");

            try {
                Parent root = FXMLLoader.load(getClass().getResource("/Dashboard.fxml"));
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
            Parent root = FXMLLoader.load(getClass().getResource("/Dashboard.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(msg);
        alert.show();
    }
}
=======
public class ModifierCreneau {
}
>>>>>>> 34f983539afbf1fbe48f8fe4cc4c438bec376064
