package tn.dhc.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.dhc.services.PasswordResetService;

public class ResetPasswordController {

    @FXML private Label         emailDisplayLabel;    // Affiche "Code envoyé à : xxx@xxx.com"
    @FXML private TextField     codeField;            // FIX : champ code OTP (plus de linkField)
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField     newPasswordVisible;
    @FXML private TextField     confirmPasswordVisible;
    @FXML private Button        resetButton;
    @FXML private Label         statusLabel;

    private boolean passwordVisible = false;
    private String  email;                            // injecté depuis ForgotPasswordController

    private final PasswordResetService resetService = new PasswordResetService();

    // ── Injection de l'email depuis l'écran précédent ──────────────────────
    public void setEmail(String email) {
        this.email = email;
        if (emailDisplayLabel != null) {
            emailDisplayLabel.setText("Code envoyé à : " + email);
        }
    }

    // ── Actions ────────────────────────────────────────────────────────────

    @FXML
    public void handleReset(ActionEvent event) {

        // 1. Récupérer le code OTP saisi
        String code = codeField.getText().trim();
        if (code.isBlank()) {
            showStatus("Veuillez saisir le code reçu par email.", true);
            return;
        }
        if (!code.matches("\\d{6}")) {
            showStatus("Le code doit contenir 6 chiffres.", true);
            return;
        }

        // 2. Vérifier les mots de passe
        String newPwd     = passwordVisible ? newPasswordVisible.getText()     : newPasswordField.getText();
        String confirmPwd = passwordVisible ? confirmPasswordVisible.getText() : confirmPasswordField.getText();

        if (newPwd == null || newPwd.isBlank()) {
            showStatus("Veuillez saisir un nouveau mot de passe.", true);
            return;
        }
        if (newPwd.length() < 8) {
            showStatus("Le mot de passe doit contenir au moins 8 caractères.", true);
            return;
        }
        if (!newPwd.equals(confirmPwd)) {
            showStatus("Les mots de passe ne correspondent pas.", true);
            return;
        }

        // 3. Réinitialiser via le service
        resetButton.setDisable(true);
        try {
            // FIX : API correcte → resetPassword(email, code, newPassword)
            resetService.resetPassword(email, code, newPwd);
            showStatus("Mot de passe modifié avec succès !", false);

            // Retour automatique au login après 2 secondes
            new Thread(() -> {
                try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
                javafx.application.Platform.runLater(() -> loadScene("/Login.fxml", event));
            }).start();

        } catch (IllegalArgumentException e) {
            showStatus(e.getMessage(), true);
            resetButton.setDisable(false);
        } catch (Exception e) {
            showStatus("Une erreur est survenue. Réessayez.", true);
            resetButton.setDisable(false);
            e.printStackTrace();
        }
    }

    @FXML
    public void togglePasswordVisibility(ActionEvent event) {
        passwordVisible = !passwordVisible;
        if (passwordVisible) {
            newPasswordVisible.setText(newPasswordField.getText());
            confirmPasswordVisible.setText(confirmPasswordField.getText());
            newPasswordVisible.setVisible(true);
            confirmPasswordVisible.setVisible(true);
            newPasswordField.setVisible(false);
            confirmPasswordField.setVisible(false);
        } else {
            newPasswordField.setText(newPasswordVisible.getText());
            confirmPasswordField.setText(confirmPasswordVisible.getText());
            newPasswordField.setVisible(true);
            confirmPasswordField.setVisible(true);
            newPasswordVisible.setVisible(false);
            confirmPasswordVisible.setVisible(false);
        }
    }

    @FXML
    public void handleBackToLogin(ActionEvent event) {
        loadScene("/Login.fxml", event);
    }

    // ── Utilitaires ────────────────────────────────────────────────────────

    private void showStatus(String message, boolean isError) {
        statusLabel.setText(message);
        statusLabel.setStyle(isError
                ? "-fx-text-fill: #e53935;"
                : "-fx-text-fill: #43a047;");
        statusLabel.setVisible(true);
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
}