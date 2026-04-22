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

/**
 * Écran 2 — "Réinitialiser le mot de passe"
 * L'utilisateur colle le lien reçu (ou les paramètres s= et t=),
 * puis saisit son nouveau mot de passe.
 *
 * Lié à : ResetPassword.fxml
 *
 * NOTE : Dans une app desktop pure JavaFX, il n'y a pas de deep link automatique.
 * Deux stratégies possibles :
 *  A) L'utilisateur copie-colle le lien complet dans un TextField → on parse s= et t=
 *  B) Un mini serveur HTTP local (ex: com.sun.net.httpserver) écoute sur localhost:8080
 *     et ouvre cet écran en passant les paramètres. (Recommandé en production)
 *
 * Ici on implémente la stratégie A (simple, suffisant pour un projet académique).
 */
public class ResetPasswordController {

    @FXML private TextField     linkField;         // Champ pour coller le lien reçu
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField     newPasswordVisible;
    @FXML private TextField     confirmPasswordVisible;
    @FXML private Button        resetButton;
    @FXML private Label         statusLabel;

    private boolean passwordVisible = false;
    private final PasswordResetService resetService = new PasswordResetService();

    // ── Actions ────────────────────────────────────────────────────────────

    @FXML
    public void handleReset(ActionEvent event) {
        // 1. Parser le lien collé
        String link = linkField.getText().trim();
        if (link.isBlank()) {
            showStatus("Veuillez coller le lien reçu par email.", true);
            return;
        }

        String selector = extractParam(link, "s");
        String token    = extractParam(link, "t");

        if (selector == null || token == null) {
            showStatus("Lien invalide. Vérifiez que vous avez collé le lien complet.", true);
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

        // 3. Réinitialiser
        resetButton.setDisable(true);
        try {
            resetService.resetPassword(selector, token, newPwd);
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

    /**
     * Extrait un paramètre d'une URL.
     * Ex: extractParam("http://...?s=abc&t=xyz", "s") → "abc"
     */
    private String extractParam(String url, String param) {
        try {
            String search = param + "=";
            int start = url.indexOf(search);
            if (start == -1) return null;
            start += search.length();
            int end = url.indexOf("&", start);
            return end == -1 ? url.substring(start) : url.substring(start, end);
        } catch (Exception e) {
            return null;
        }
    }

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
