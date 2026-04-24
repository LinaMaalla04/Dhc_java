package tn.dhc.controllers;

<<<<<<< HEAD
import com.sun.net.httpserver.HttpServer;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import tn.dhc.services.CaptchaService;
=======
import javafx.event.ActionEvent;
>>>>>>> 34f983539afbf1fbe48f8fe4cc4c438bec376064
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
<<<<<<< HEAD
    private final CaptchaService captchaService = new CaptchaService();

    // ── reCAPTCHA v3 ──────────────────────────────────────────────────────
    @FXML private WebView captchaWebView;
    private WebEngine     webEngine;
    private String        captchaToken = "";
    private HttpServer    captchaServer;
=======
>>>>>>> 34f983539afbf1fbe48f8fe4cc4c438bec376064

    @FXML
    public void initialize() {
        for (MenuItem item : addRoleUser.getItems()) {
            item.setOnAction(e -> addRoleUser.setText(item.getText()));
        }
<<<<<<< HEAD
        initCaptcha();
    }

    private void initCaptcha() {
        webEngine = captchaWebView.getEngine();
        webEngine.setJavaScriptEnabled(true);

        // Listener : token ready → poll
        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                pollCaptchaToken(null);
            }
        });

        // Démarrer le serveur HTTP et charger l'URL APRÈS que le WebView
        // soit attaché à la scène (sceneProperty listener) pour éviter 830x0
        captchaWebView.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null && captchaServer == null) {
                Platform.runLater(() -> {
                    try {
                        // Chercher un port libre à partir de 8765
                        int port = 8765;
                        while (port < 8800) {
                            try {
                                captchaServer = HttpServer.create(
                                        new java.net.InetSocketAddress("127.0.0.1", port), 0);
                                break;
                            } catch (java.net.BindException ex) {
                                port++;
                            }
                        }
                        final int finalPort = port;
                        captchaServer.createContext("/captcha.html", exchange -> {
                            byte[] bytes = getClass()
                                    .getResourceAsStream("/captcha.html").readAllBytes();
                            exchange.getResponseHeaders()
                                    .set("Content-Type", "text/html; charset=UTF-8");
                            exchange.sendResponseHeaders(200, bytes.length);
                            try (var os = exchange.getResponseBody()) { os.write(bytes); }
                        });
                        captchaServer.start();
                        webEngine.load("http://127.0.0.1:" + finalPort + "/captcha.html");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        });
    }

    /**
     * Poll le token jusqu'à ce qu'il soit disponible, puis STOPPE.
     * Le token est valide 2 minutes — on le régénère à chaque clic S'inscrire.
     */
    private void pollCaptchaToken(Runnable onTokenReady) {
        captchaToken = "";
        final int[] attempts = {0};
        javafx.animation.Timeline[] holder = new javafx.animation.Timeline[1];
        // Délai initial 600ms pour laisser grecaptcha.execute() démarrer
        javafx.animation.Timeline timeline = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(javafx.util.Duration.millis(600), e -> {
                    attempts[0]++;
                    try {
                        String token = (String) webEngine.executeScript("getCaptchaToken()");
                        if (token != null && !token.isBlank()) {
                            captchaToken = token;
                            holder[0].stop(); // STOP dès qu'on a le token
                            System.out.println("[reCAPTCHA] Token frais récupéré ✓");
                            if (onTokenReady != null) onTokenReady.run();
                            return;
                        }
                    } catch (Exception ex) { /* JS pas encore prêt */ }
                    if (attempts[0] >= 30) { // 9 secondes max
                        holder[0].stop();
                        System.out.println("[reCAPTCHA] Timeout");
                        if (onTokenReady != null) onTokenReady.run(); // continuer même en timeout
                    }
                })
        );
        holder[0] = timeline;
        timeline.setCycleCount(javafx.animation.Animation.INDEFINITE);
        timeline.play();
=======
>>>>>>> 34f983539afbf1fbe48f8fe4cc4c438bec376064
    }

    @FXML
    public void goToLogin(ActionEvent event) {
        loadScene("/Login.fxml", event);
    }

    @FXML
    public void inscription(ActionEvent event) {
        try {
<<<<<<< HEAD
            // ── reCAPTCHA v3 : token FRAIS à chaque clic ─────────────────
            // 1. Vider le token JS pour être sûr de lire un nouveau
            webEngine.executeScript("captchaToken = '';");
            captchaToken = "";
            // 2. Demander un nouveau token
            webEngine.executeScript("executeCaptcha();");
            // 3. Poller jusqu'à ce que le nouveau token soit disponible
            pollCaptchaToken(() -> Platform.runLater(() -> continueInscription(event)));
            return;

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage() != null ? e.getMessage() : "Erreur inconnue");
        }
    }

    /**
     * Appelé après récupération du token frais.
     * Vérifie le score puis procède à l'inscription.
     */
    private void continueInscription(ActionEvent event) {
        try {
            // Vérifier le token frais auprès de Google
            if (captchaToken == null || captchaToken.isBlank()) {
                showAlert(Alert.AlertType.WARNING, "Vérification",
                        "Impossible d'obtenir la vérification. Vérifiez votre connexion.");
                return;
            }
            boolean human;
            try {
                System.out.println("[reCAPTCHA] Token envoyé (longueur=" + captchaToken.length() + ") : " + captchaToken.substring(0, Math.min(40, captchaToken.length())) + "...");
                human = captchaService.verify(captchaToken);
            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "Erreur réseau",
                        "Impossible de vérifier le captcha. Vérifiez votre connexion internet.");
                return;
            }
            if (!human) {
                showAlert(Alert.AlertType.ERROR, "Vérification échouée",
                        "Score insuffisant. Veuillez réessayer.");
                return;
            }

=======
>>>>>>> 34f983539afbf1fbe48f8fe4cc4c438bec376064
            if (!addMdpUser.getText().equals(addMdpCUser.getText())) {
                showAlert(Alert.AlertType.WARNING, "Erreur", "Les mots de passe ne correspondent pas !");
                return;
            }
<<<<<<< HEAD
=======

>>>>>>> 34f983539afbf1fbe48f8fe4cc4c438bec376064
            String roleUi = addRoleUser.getText();
            if (roleUi == null || roleUi.isBlank() || roleUi.equals(ROLE_PROMPT)) {
                showAlert(Alert.AlertType.WARNING, "Erreur", "Veuillez choisir un rôle (Patient ou Médecin).");
                return;
            }
<<<<<<< HEAD
            String nom      = addNomUser.getText();
            String prenom   = addPrenomUser.getText();
            String email    = addEmailUser.getText();
            String mdp      = addMdpUser.getText();
            String role     = normalizeRoleForStorage(roleUi);
            String specialite = addSpecialiteUser.getText() != null ? addSpecialiteUser.getText().trim() : "";
            int    tel;
            try { tel = Integer.parseInt(addTelUser.getText()); }
            catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Numéro de téléphone invalide !");
                return;
            }
            if (nom == null || nom.isBlank() || prenom == null || prenom.isBlank() || email == null || email.isBlank()) {
                showAlert(Alert.AlertType.WARNING, "Erreur", "Veuillez remplir tous les champs obligatoires.");
                return;
            }
            User u = new User(nom.trim(), prenom.trim(), email.trim(), tel, mdp, role, specialite, LocalDateTime.now(), 0);
            userService.ajouter(u);

            captchaToken = "";
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Inscription réussie ! Vous pouvez vous connecter.");
            loadScene("/Login.fxml", event);

=======

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
>>>>>>> 34f983539afbf1fbe48f8fe4cc4c438bec376064
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
<<<<<<< HEAD
            if (captchaServer != null) {
                captchaServer.stop(0);
                captchaServer = null;
                System.out.println("[Captcha] Serveur HTTP arrêté.");
            }
=======
>>>>>>> 34f983539afbf1fbe48f8fe4cc4c438bec376064
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
<<<<<<< HEAD
}
=======
}
>>>>>>> 34f983539afbf1fbe48f8fe4cc4c438bec376064
