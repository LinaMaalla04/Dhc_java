package tn.dhc.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.MenuItem;
import javafx.scene.control.PasswordField;
import javafx.scene.control.SplitMenuButton;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import tn.dhc.entities.User;
import tn.dhc.services.UserService;

import java.time.LocalDateTime;
import java.util.Locale;

public class Registration {

    private static final String ROLE_PROMPT = "Choisissez un role";

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

    private final UserService userService = new UserService();

    @FXML
    public void initialize() {
        for (MenuItem item : addRoleUser.getItems()) {
            item.setOnAction(e -> addRoleUser.setText(item.getText()));
        }
    }

    @FXML
    public void goToLogin(ActionEvent event) {
        loadScene("/Login.fxml", event);
    }

    @FXML
    public void inscription(ActionEvent event) {
        try {
            if (!addMdpUser.getText().equals(addMdpCUser.getText())) {
                showAlert(Alert.AlertType.WARNING, "Erreur", "Les mots de passe ne correspondent pas !");
                return;
            }

            String roleUi = addRoleUser.getText();
            if (roleUi == null || roleUi.isBlank() || roleUi.equals(ROLE_PROMPT)) {
                showAlert(Alert.AlertType.WARNING, "Erreur", "Veuillez choisir un rôle (Patient ou Médecin).");
                return;
            }

            String nom = addNomUser.getText();
            String prenom = addPrenomUser.getText();
            String email = addEmailUser.getText();
            int tel = Integer.parseInt(addTelUser.getText());
            String mdp = addMdpUser.getText();
            String role = normalizeRoleForStorage(roleUi);
            String specialite = addSpecialiteUser.getText() != null ? addSpecialiteUser.getText().trim() : "";

            if (nom == null || nom.isBlank() || prenom == null || prenom.isBlank()
                    || email == null || email.isBlank()) {
                showAlert(Alert.AlertType.WARNING, "Erreur", "Veuillez remplir tous les champs obligatoires.");
                return;
            }

            User u = new User(
                    nom.trim(),
                    prenom.trim(),
                    email.trim(),
                    tel,
                    mdp,
                    role,
                    specialite,
                    LocalDateTime.now(),
                    0
            );

            userService.ajouter(u);

            showAlert(Alert.AlertType.INFORMATION, "Succès", "Inscription réussie ! Vous pouvez vous connecter.");
            loadScene("/Login.fxml", event);

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Numéro de téléphone invalide !");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage() != null ? e.getMessage() : "Erreur inconnue");
        }
    }

    private static String normalizeRoleForStorage(String roleFromUi) {
        String t = roleFromUi.trim().toLowerCase(Locale.ROOT);
        if (t.contains("patient")) {
            return "patient";
        }
        if (t.contains("medecin") || t.contains("médecin") || t.contains("doctor")) {
            return "medecin";
        }
        return t;
    }

    private void loadScene(String resource, ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(resource));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
