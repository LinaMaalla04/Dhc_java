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
import tn.dhc.entities.Lieu;
import tn.dhc.services.LieuService;
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
    private final LieuService lieuService = new LieuService();

    @FXML
    private ComboBox<Lieu> modEventLieu;

    @FXML
    public void initialize() {
        for (int i = 8; i <= 18; i++) {
            modEventDebut.getItems().add(LocalTime.of(i, 0));
            modEventFin.getItems().add(LocalTime.of(i, 0));
        }

        // Charger les lieux avec affichage nom+adresse+ville
        modEventLieu.getItems().addAll(lieuService.getAll());
        modEventLieu.setCellFactory(lv -> new javafx.scene.control.ListCell<Lieu>() {
            @Override protected void updateItem(Lieu l, boolean empty) {
                super.updateItem(l, empty);
                setText(empty || l == null ? null
                        : l.getNomLieu() + " — " + l.getAdresse() + ", " + l.getVille());
            }
        });
        modEventLieu.setButtonCell(new javafx.scene.control.ListCell<Lieu>() {
            @Override protected void updateItem(Lieu l, boolean empty) {
                super.updateItem(l, empty);
                setText(empty || l == null ? null
                        : l.getNomLieu() + " — " + l.getAdresse() + ", " + l.getVille());
            }
        });
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

            // Pré-sélectionner le lieu actuel de l'événement
            if (event.getEventLieuId() > 0) {
                modEventLieu.getItems().stream()
                        .filter(l -> l.getId() == event.getEventLieuId())
                        .findFirst()
                        .ifPresent(modEventLieu::setValue);
            }
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

            Lieu lieuSelectionne = modEventLieu.getValue();
            if (lieuSelectionne == null) {
                Alert alertLieu = new Alert(Alert.AlertType.WARNING);
                alertLieu.setTitle("Lieu manquant");
                alertLieu.setContentText("Veuillez choisir un lieu !");
                alertLieu.show();
                return;
            }

            event.setTitreEvent(titre);
            event.setThemeSante(theme);
            event.setDescription(description);
            event.setDateEvent(date);
            event.setHeureDebut(debut);
            event.setHeureFin(fin);
            event.setEventLieuId(lieuSelectionne.getId());

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