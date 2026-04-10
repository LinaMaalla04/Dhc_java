package tn.dhc.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import tn.dhc.entities.User;
import tn.dhc.services.UserService;

import java.time.LocalDateTime;

public class Registration {

    @FXML
    private TextField addEmailUser;

    @FXML
    private PasswordField addMdpUser;

    @FXML
    private PasswordField addMdpCUser;

    @FXML
    private TextField addNomUser;

    @FXML
    private TextField addPrenomUser;

    @FXML
    private SplitMenuButton addRoleUser;

    @FXML
    private TextField addSpecialiteUser;

    @FXML
    private TextField addTelUser;

    private UserService userService = new UserService();

    @FXML
    void inscription(ActionEvent event) {

        try {
            // 🔹 Vérification mots de passe
            if (!addMdpUser.getText().equals(addMdpCUser.getText())) {
                showAlert("Erreur", "Les mots de passe ne correspondent pas !");
                return;
            }

            // 🔹 Récupération des données
            String nom = addNomUser.getText();
            String prenom = addPrenomUser.getText();
            String email = addEmailUser.getText();
            int tel = Integer.parseInt(addTelUser.getText());
            String mdp = addMdpUser.getText();
            String role = addRoleUser.getText();
            String specialite = addSpecialiteUser.getText();

            // 🔹 Création user
            User u = new User(
                    nom,
                    prenom,
                    email,
                    tel,
                    mdp,
                    role,
                    specialite,
                    LocalDateTime.now(),
                    0
            );

            userService.ajouter(u);

            showAlert("Succès", "Inscription réussie !");

        } catch (NumberFormatException e) {
            showAlert("Erreur", "Numéro de téléphone invalide !");
        } catch (Exception e) {
            showAlert("Erreur", e.getMessage());
        }
    }

    // 🔹 Méthode Alert
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}