package tn.dhc.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import tn.dhc.entities.User;
import tn.dhc.services.UserService;

import java.util.Locale;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField passwordTextField;

    private final UserService userService = new UserService();

    private boolean passwordVisible;

    @FXML
    public void togglePasswordVisibility(ActionEvent event) {
        passwordVisible = !passwordVisible;
        if (passwordVisible) {
            passwordTextField.setText(passwordField.getText());
            passwordTextField.setVisible(true);
            passwordField.setVisible(false);
        } else {
            passwordField.setText(passwordTextField.getText());
            passwordField.setVisible(true);
            passwordTextField.setVisible(false);
        }
    }

    @FXML
    public void handleForgotPassword(ActionEvent event) {
<<<<<<< HEAD
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Mot de passe oublié");
        alert.setHeaderText(null);
        alert.setContentText("Contactez l'administrateur pour réinitialiser votre mot de passe.");
        alert.showAndWait();
=======
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/ForgotPassword.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            e.printStackTrace();
        }
>>>>>>> dfeb78e (mdp oublié)
    }

    @FXML
    public void handleLogin(ActionEvent event) {
        String email = usernameField.getText();
        String password = passwordVisible ? passwordTextField.getText() : passwordField.getText();
        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setContentText("Veuillez remplir l'email et le mot de passe.");
            alert.showAndWait();
            return;
        }
        if (userService.login(email.trim(), password)) {
            User u = UserService.getCurrentUser();
            loadScene(homeFxmlForUser(u), event);
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Email ou mot de passe incorrect.");
            alert.showAndWait();
        }
    }

    @FXML
    public void handleRegister(ActionEvent event) {
        loadScene("/Registration.fxml", event);
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

    /**
     * Admin → back-office ; patient → {@code Front.fxml} ; médecin → {@code DoctorFront.fxml}.
     */
    static String homeFxmlForUser(User u) {
        if (u == null || u.getRole() == null) {
            return "/Front.fxml";
        }
        String r = u.getRole().trim().toLowerCase(Locale.ROOT);
        if (r.equals("admin") || r.equals("administrateur")) {
            return "/Dashboard.fxml";
        }
        if (r.equals("medecin") || r.equals("médecin") || r.equals("doctor")) {
            return "/DoctorFront.fxml";
        }
        if (r.equals("patient")) {
            return "/Front.fxml";
        }
        if (r.contains("medecin") || r.contains("médecin") || r.contains("doctor")) {
            return "/DoctorFront.fxml";
        }
        if (r.contains("patient")) {
            return "/Front.fxml";
        }
        return "/Front.fxml";
    }
}
