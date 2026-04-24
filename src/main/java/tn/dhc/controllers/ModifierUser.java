package tn.dhc.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
<<<<<<< HEAD
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
import javafx.scene.control.SplitMenuButton;
import javafx.scene.control.MenuItem;
=======
import javafx.scene.control.PasswordField;
import javafx.scene.control.SplitMenuButton;
import javafx.scene.control.TextField;
import tn.dhc.entities.User;
import tn.dhc.services.UserService;
>>>>>>> 34f983539afbf1fbe48f8fe4cc4c438bec376064

public class ModifierUser {

    @FXML
    private TextField modEmailUser;

<<<<<<< HEAD
=======

>>>>>>> 34f983539afbf1fbe48f8fe4cc4c438bec376064
    @FXML
    private TextField modNomUser;

    @FXML
    private TextField modPrenomUser;

    @FXML
    private SplitMenuButton modRoleUser;

    @FXML
    private TextField modSpecialiteUser;

    @FXML
    private TextField modTelUser;

    @FXML
    private PasswordField modMdpUser;

    @FXML
    private PasswordField modMdpCUser;

    private User user;
<<<<<<< HEAD
    private final UserService userService = new UserService();

    @FXML
    public void initialize() {

        for (MenuItem item : modRoleUser.getItems()) {
            item.setOnAction(e -> {
                modRoleUser.setText(item.getText());
            });
        }
    }
=======

    private UserService userService = new UserService();
>>>>>>> 34f983539afbf1fbe48f8fe4cc4c438bec376064

    public void setUser(User user) {
        this.user = user;

        modNomUser.setText(user.getNom());
        modPrenomUser.setText(user.getPrenom());
        modEmailUser.setText(user.getMail());
        modTelUser.setText(String.valueOf(user.getTel()));
        modRoleUser.setText(user.getRole());
        modSpecialiteUser.setText(user.getSpecialite());
    }

    @FXML
    void modifier(ActionEvent event) {
<<<<<<< HEAD

        try {
            if (user == null) {
                System.out.println("Aucun user sélectionné !");
                return;
            }

            String nom = modNomUser.getText();
            String prenom = modPrenomUser.getText();
            String email = modEmailUser.getText();
            String role = modRoleUser.getText();
            String specialite = modSpecialiteUser.getText();
            String telStr = modTelUser.getText();
            String mdp = modMdpUser.getText();
            String confirmMdp = modMdpCUser.getText();

            if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty()) {
                showAlert("Champs obligatoires", "Nom, prénom et email sont obligatoires !");
                return;
            }

            if (role.equals("Choisissez un role")) {
                showAlert("Erreur", "Veuillez choisir un rôle !");
                return;
            }

            if (!mdp.isEmpty() && !mdp.equals(confirmMdp)) {
                showAlert("Erreur mot de passe", "Les mots de passe ne correspondent pas !");
                return;
            }

            user.setNom(nom);
            user.setPrenom(prenom);
            user.setMail(email);
            user.setRole(role);
            user.setSpecialite(specialite);

            if (!telStr.isEmpty()) {
                user.setTel(Integer.parseInt(telStr));
            }

            if (!mdp.isEmpty()) {
                user.setMdp(mdp);
            }

            userService.modifier(user);

            showAlert("Succès", "User modifié avec succès !");
            System.out.println("✅ User modifié avec succès");
            try {
                Parent root = FXMLLoader.load(getClass().getResource("/Dashboard.fxml"));
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(new Scene(root));
            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (NumberFormatException e) {
            showAlert("Erreur téléphone", "Le numéro de téléphone doit être un nombre !");
=======
        try {
            // 🔹 mettre à jour l'objet
            user.setNom(modNomUser.getText());
            user.setPrenom(modPrenomUser.getText());

            // 🔹 appel service
            userService.modifier(user);

            System.out.println("✅ User modifié avec succès");

>>>>>>> 34f983539afbf1fbe48f8fe4cc4c438bec376064
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
<<<<<<< HEAD

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

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(msg);
        alert.show();
    }
=======
>>>>>>> 34f983539afbf1fbe48f8fe4cc4c438bec376064
}