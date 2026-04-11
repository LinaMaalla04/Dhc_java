package tn.dhc.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import javafx.stage.Stage;
import tn.dhc.entities.User;
import tn.dhc.services.UserService;
import tn.dhc.entities.Creneau;
import tn.dhc.services.CreneauService;
import tn.dhc.entities.Event;
import tn.dhc.services.EventService;

public class Dashboard {

    @FXML
    private ListView<User> AffUsers;

    @FXML
    private ListView<Creneau> AffCreneaux;

    @FXML
    private ListView<Event> AffEvents;

    @FXML
    private TextField RechCreneauText;

    @FXML
    private TextField RechEventText;

    @FXML
    private TextField RechUserText;

    @FXML
    private Label adminNameLabel;

    @FXML
    private Label reportLabel;

    private final UserService userService = new UserService();
    private final CreneauService creneauService = new CreneauService();
    private final EventService eventService = new EventService();

    @FXML
    public void initialize() {
        loadUsers();
        loadCreneaux();
        loadEvents();

    }


    private void loadUsers() {
        AffUsers.getItems().clear();
        AffUsers.getItems().addAll(userService.getAll());
    }

    private void loadCreneaux() {
        AffCreneaux.getItems().clear();
        AffCreneaux.getItems().addAll(creneauService.getAll());
    }

    private void loadEvents() {
        AffEvents.getItems().clear();
        AffEvents.getItems().addAll(eventService.getAll());
    }
    @FXML
    void refreshUsers(ActionEvent event) {
        loadUsers();
    }

    @FXML
    void deleteUser(ActionEvent event) {

        User selected = AffUsers.getSelectionModel().getSelectedItem();

        if (selected != null) {
            userService.supprimer(selected);
            loadUsers();
        } else {
            System.out.println("Aucun user sélectionne");
        }
    }

    @FXML void RechCreneau(ActionEvent event) {}
    @FXML void RechEvent(ActionEvent event) {}
    @FXML void RechUser(ActionEvent event) {}

    @FXML void addCreneau(ActionEvent event) {}
    @FXML void addEvent(ActionEvent event) {}

    @FXML void deconnexion(ActionEvent event) {}

    @FXML
    void deleteCreneau(ActionEvent event) {

        Creneau selected = AffCreneaux.getSelectionModel().getSelectedItem();

        if (selected != null) {
            creneauService.delete(selected.getId());
            loadCreneaux();
        } else {
            System.out.println("Aucun creneau sélectionne");
        }
    }
    @FXML void deleteEvent(ActionEvent event) {
        Event selected = AffEvents.getSelectionModel().getSelectedItem();

        if (selected != null) {
            eventService.supprimer(selected);
            loadEvents();
        } else {
            System.out.println("Aucun evenement sélectionne");
        }
    }

    @FXML void editCreneau(ActionEvent event) {}
    @FXML void editEvent(ActionEvent event) {}
    @FXML
    void editUser(ActionEvent event) {
        System.out.println("CLICK MODIFIER USER");
        try {
            User selected = AffUsers.getSelectionModel().getSelectedItem();

            if (selected == null) {
                System.out.println("Aucun user sélectionné");
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierUser.fxml"));
            Parent root = loader.load();

            // 🔥 récupérer le controller
            ModifierUser controller = loader.getController();

            // 🔥 envoyer le user sélectionné
            controller.setUser(selected);

            // 🔁 changer de scène
            Stage stage = (Stage) AffUsers.getScene().getWindow();
            stage.setScene(new Scene(root));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML void goToLieux(ActionEvent event) {}
    @FXML void goToRdv(ActionEvent event) {}

    @FXML
    void refreshCreneaux(ActionEvent event) {
        loadCreneaux();
    }

    @FXML void refreshEvent(ActionEvent event) {
        loadEvents();
    }
}