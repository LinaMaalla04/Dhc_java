package tn.dhc.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.dhc.entities.Event;
import tn.dhc.services.EventService;

import java.io.IOException;
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


            Event e = new Event(
                    0,
                    titre,
                    theme,
                    descr,
                    date,
                    debut,
                    fin,
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

        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Erreur", ex.getMessage());
        }
    }

    @FXML
    public void annuler(ActionEvent event) throws IOException {
        addEventDate.setValue(null);
        addEventDebut.setValue("08:00");
        addEventFin.setValue("08:30");

        addEventTitre.clear();
        addEventTheme.clear();
        addEventDescr.clear();
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

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}