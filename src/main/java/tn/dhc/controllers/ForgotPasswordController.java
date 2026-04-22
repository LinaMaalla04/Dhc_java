package tn.dhc.controllers;

import javafx.application.Platform;
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
 * Écran 1 — "Mot de passe oublié"
 * L'utilisateur saisit son email → reçoit un lien par mail.
 *
 * Lié à : ForgotPassword.fxml
 */
public class ForgotPasswordController {

    @FXML private TextField emailField;
    @FXML private Button    sendButton;
    @FXML private Label     statusLabel;

    private final PasswordResetService resetService = new PasswordResetService();

    /**
     * Déclenché par le bouton "Envoyer le lien".
     * L'envoi se fait dans un thread séparé pour ne pas bloquer l'UI JavaFX.
     */
    @FXML
    public void handleSendLink(ActionEvent event) {
        String email = emailField.getText();

        // Validation basique côté client
        if (email == null || email.isBlank()) {
            showStatus("Veuillez saisir votre adresse email.", true);
            return;
        }
        if (!email.matches("^[\\w.+-]+@[\\w-]+\\.[\\w.]+$")) {
            showStatus("Adresse email invalide.", true);
            return;
        }

        // Désactiver le bouton pendant l'envoi
        sendButton.setDisable(true);
        showStatus("Envoi en cours…", false);

        // Envoi asynchrone (JavaMail est bloquant)
        Thread worker = new Thread(() -> {
            try {
                resetService.initiate(email.trim());
                Platform.runLater(() -> {
                    showStatus("Email envoyé ! Vérifiez votre boîte mail.", false);
                    sendButton.setDisable(false);
                    // Ouvrir l'écran de saisie du nouveau mot de passe après 2s
                    openResetScreen(event);
                });
            } catch (IllegalArgumentException e) {
                // Email inconnu → message générique pour ne pas révéler les comptes
                Platform.runLater(() -> {
                    showStatus("Si cet email existe, un lien vous a été envoyé.", false);
                    sendButton.setDisable(false);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    showStatus("Erreur lors de l'envoi. Réessayez dans quelques instants.", true);
                    sendButton.setDisable(false);
                    e.printStackTrace();
                });
            }
        });
        worker.setDaemon(true);
        worker.start();
    }

    /** Retour à l'écran de login. */
    @FXML
    public void handleBackToLogin(ActionEvent event) {
        loadScene("/Login.fxml", event);
    }

    // ── Privé ──────────────────────────────────────────────────────────────

    private void openResetScreen(ActionEvent event) {
        // On ouvre l'écran ResetPassword où l'utilisateur colle/saisit son token.
        // Dans un vrai projet avec deep link, ce serait géré automatiquement.
        loadScene("/ResetPassword.fxml", event);
    }

    private void showStatus(String message, boolean isError) {
        statusLabel.setText(message);
        statusLabel.setStyle(isError
                ? "-fx-text-fill: #e53935;"   // rouge
                : "-fx-text-fill: #43a047;"); // vert
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
