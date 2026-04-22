package tn.dhc.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
<<<<<<< HEAD
<<<<<<< HEAD
=======
>>>>>>> dfeb78e (mdp oublié)
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
<<<<<<< HEAD
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.dhc.entities.Event;
import tn.dhc.services.EventService;

import java.io.IOException;
=======
=======
>>>>>>> dfeb78e (mdp oublié)
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.dhc.entities.Event;
import tn.dhc.services.EventService;

<<<<<<< HEAD
>>>>>>> d47e962 (Events CRUD)
=======
import java.io.IOException;
>>>>>>> dfeb78e (mdp oublié)
import java.time.LocalDate;
import java.time.LocalTime;

public class AjouterEvent {

    @FXML
    private DatePicker addEventDate;

    @FXML
    private ComboBox<String> addEventDebut;

    @FXML
    private ComboBox<String> addEventFin;

    @FXML
    private TextField addEventDescr;

    @FXML
    private TextField addEventTheme;

    @FXML
    private TextField addEventTitre;

    private final EventService eventService = new EventService();

    @FXML
    public void initialize() {

        for (int h = 8; h <= 18; h++) {
            addEventDebut.getItems().add(String.format("%02d:00", h));
            addEventDebut.getItems().add(String.format("%02d:30", h));

            addEventFin.getItems().add(String.format("%02d:00", h));
            addEventFin.getItems().add(String.format("%02d:30", h));
        }
//_________________


        
        addEventDebut.setValue("08:00");
        addEventFin.setValue("08:30");
    }

    @FXML
    void ajouterEvent(ActionEvent event) {

        try {
            LocalDate date = addEventDate.getValue();
            String debutStr = addEventDebut.getValue();
            String finStr = addEventFin.getValue();

            String titre = addEventTitre.getText();
            String theme = addEventTheme.getText();
            String descr = addEventDescr.getText();

            if (date == null || debutStr == null || finStr == null ||
                    titre == null || titre.isEmpty() ||
                    theme == null || theme.isEmpty() ||
                    descr == null || descr.isEmpty()) {

                showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez remplir tous les champs !");
                return;
            }

            LocalTime debut = LocalTime.parse(debutStr);
            LocalTime fin = LocalTime.parse(finStr);

            if (!fin.isAfter(debut)) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Heure invalide !");
                return;
            }

<<<<<<< HEAD
<<<<<<< HEAD

            Event e = new Event(
                    0,
=======
            // ⚠️ IMPORTANT : adapter selon ton constructeur Event
            Event e = new Event(
                    0,                 // id auto
>>>>>>> d47e962 (Events CRUD)
=======

            Event e = new Event(
                    0,
>>>>>>> dfeb78e (mdp oublié)
                    titre,
                    theme,
                    descr,
                    date,
                    debut,
                    fin,
<<<<<<< HEAD
<<<<<<< HEAD
                    null,
                    1,
                    1
            );

            eventService.ajouter(e); 

            showAlert(Alert.AlertType.INFORMATION, "Succès", "Event ajouté avec succès !");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Dashboard.fxml"));
            Parent root = loader.load();

            Dashboard controller = loader.getController();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
=======
                    null,              // nbParticipant (ou 0 si obligatoire)
                    1,                 // user_id (à remplacer par user connecté)
                    1                  // lieu_id (à remplacer)
=======
                    null,
                    1,
                    1
>>>>>>> dfeb78e (mdp oublié)
            );

            eventService.ajouter(e); 

            showAlert(Alert.AlertType.INFORMATION, "Succès", "Event ajouté avec succès !");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Dashboard.fxml"));
            Parent root = loader.load();

<<<<<<< HEAD
            annuler(null);
>>>>>>> d47e962 (Events CRUD)
=======
            Dashboard controller = loader.getController();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
>>>>>>> dfeb78e (mdp oublié)

        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Erreur", ex.getMessage());
        }
    }

    @FXML
<<<<<<< HEAD
<<<<<<< HEAD
    public void annuler(ActionEvent event) throws IOException {
=======
    public void annuler(ActionEvent event) {
>>>>>>> d47e962 (Events CRUD)
=======
    public void annuler(ActionEvent event) throws IOException {
>>>>>>> dfeb78e (mdp oublié)
        addEventDate.setValue(null);
        addEventDebut.setValue("08:00");
        addEventFin.setValue("08:30");

        addEventTitre.clear();
        addEventTheme.clear();
        addEventDescr.clear();
<<<<<<< HEAD
<<<<<<< HEAD
=======
>>>>>>> dfeb78e (mdp oublié)
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
<<<<<<< HEAD
=======
>>>>>>> d47e962 (Events CRUD)
=======
>>>>>>> dfeb78e (mdp oublié)
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}