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
import tn.dhc.entities.Event;
import tn.dhc.services.EventService;

import java.time.LocalDate;
import java.time.LocalTime;

public class ModifierEvent {

    @FXML
    private DatePicker modEventDate;

    @FXML
    private ComboBox<LocalTime> modEventDebut;

    @FXML
    private TextField modEventDescr;

    @FXML
    private ComboBox<LocalTime> modEventFin;

    @FXML
    private TextField modEventTheme;

    @FXML
    private TextField modEventTitre;

    private Event event; 
    private EventService eventService = new EventService();

    @FXML
    public void initialize() {

        for (int i = 8; i <= 18; i++) {
            modEventDebut.getItems().add(LocalTime.of(i, 0));
            modEventFin.getItems().add(LocalTime.of(i, 0));
        }
    }

    public void setEvent(Event event) {
        this.event = event;

        if (event != null) {
            modEventTitre.setText(event.getTitreEvent());
            modEventTheme.setText(event.getThemeSante());
            modEventDescr.setText(event.getDescription());
            modEventDate.setValue(event.getDateEvent());

            modEventDebut.setValue(event.getHeureDebut());
            modEventFin.setValue(event.getHeureFin());
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

    @FXML
    void modifEvent(ActionEvent actionEvent) {

        if (event == null) {
            System.out.println("Aucun event sélectionné !");
            return;
        }

        try {
            String titre = modEventTitre.getText();
            String theme = modEventTheme.getText();
            String description = modEventDescr.getText();
            LocalDate date = modEventDate.getValue();
            LocalTime debut = modEventDebut.getValue();
            LocalTime fin = modEventFin.getValue();

            if (titre.isEmpty() || theme.isEmpty() || description.isEmpty() || date == null) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Champs vides");
                alert.setContentText("Veuillez remplir tous les champs !");
                alert.show();
                return;
            }

            event.setTitreEvent(titre);
            event.setThemeSante(theme);
            event.setDescription(description);
            event.setDateEvent(date);
            event.setHeureDebut(debut);
            event.setHeureFin(fin);

            eventService.modifier(event);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Succès");
            alert.setContentText("Event modifié avec succès !");
            alert.show();

            try {
                Parent root = FXMLLoader.load(getClass().getResource("/Dashboard.fxml"));
                Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();                stage.setScene(new Scene(root));
            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
=======
public class ModifierEvent {
}
>>>>>>> 34f983539afbf1fbe48f8fe4cc4c438bec376064
