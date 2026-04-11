package tn.dhc.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.SplitMenuButton;
import javafx.scene.control.TextField;
import tn.dhc.entities.User;
import tn.dhc.services.UserService;

public class ModifierUser {

    @FXML
    private TextField modEmailUser;


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

    private UserService userService = new UserService();

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
        try {
            // 🔹 mettre à jour l'objet
            user.setNom(modNomUser.getText());
            user.setPrenom(modPrenomUser.getText());

            // 🔹 appel service
            userService.modifier(user);

            System.out.println("✅ User modifié avec succès");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}