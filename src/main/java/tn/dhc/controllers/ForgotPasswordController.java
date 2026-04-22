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

public class ForgotPasswordController {

    @FXML private TextField emailField;
    @FXML private Button    sendButton;
    @FXML private Label     statusLabel;

    private final PasswordResetService resetService = new PasswordResetService();

    @FXML
    public void handleSendLink(ActionEvent event) {
        String email = emailField.getText();

        if (email == null || email.isBlank()) {
            showStatus("Veuillez saisir votre adresse email.", true);
            return;
        }
        if (!email.matches("^[\\w.+-]+@[\\w-]+\\.[\\w.]+$")) {
            showStatus("Adresse email invalide.", true);
            return;
        }

        sendButton.setDisable(true);
        showStatus("Envoi en cours…", false);

        String trimmedEmail = email.trim();

        Thread worker = new Thread(() -> {
            try {
                resetService.initiate(trimmedEmail);

                Platform.runLater(() -> {
                    showStatus("Code envoyé ! Vérifiez votre boîte mail.", false);
                    sendButton.setDisable(false);
                    // FIX : on passe l'email à l'écran suivant
                    openResetScreen(event, trimmedEmail);
                });

            } catch (IllegalArgumentException e) {
                // Message générique pour ne pas révéler si l'email existe
                Platform.runLater(() -> {
                    showStatus("Si cet email existe, un code vous a été envoyé.", false);
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

    @FXML
    public void handleBackToLogin(ActionEvent event) {
        loadScene("/Login.fxml", event);
    }

    // ── Privé ──────────────────────────────────────────────────────────────

    /**
     * Ouvre ResetPassword.fxml en passant l'email via le controller.
     * L'email est nécessaire pour la validation du code OTP.
     */
    private void openResetScreen(ActionEvent event, String email) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ResetPassword.fxml"));
            Parent root = loader.load();

            // FIX : injecter l'email dans le controller de l'écran suivant
            ResetPasswordController nextController = loader.getController();
            nextController.setEmail(email);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            e.printStackTrace();
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