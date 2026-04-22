package tn.dhc.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import tn.dhc.entities.Lieu;
import tn.dhc.services.LieuService;

public class ModifierLieu {

    @FXML
    private TextField modLieuAdresse;

    @FXML
    private TextField modLieuCapacite;

    @FXML
    private TextField modLieuNom;

    @FXML
    private TextField modLieuVille;

    private Lieu lieu;
    private LieuService lieuService = new LieuService();

    public void setLieu(Lieu lieu) {
        this.lieu = lieu;

        if (lieu != null) {
            modLieuNom.setText(lieu.getNomLieu());
            modLieuAdresse.setText(lieu.getAdresse());
            modLieuVille.setText(lieu.getVille());

            if (lieu.getCapaciteMax() != null) {
                modLieuCapacite.setText(String.valueOf(lieu.getCapaciteMax()));
            }
        }
    }

    @FXML
    void ModifierLieu(ActionEvent event) {

        try {
            if (lieu == null) {
                System.out.println("Aucun lieu !");
                return;
            }

            String nom = modLieuNom.getText();
            String adresse = modLieuAdresse.getText();
            String ville = modLieuVille.getText();
            String capStr = modLieuCapacite.getText();

            if (nom.isEmpty() || adresse.isEmpty() || ville.isEmpty()) {
                showAlert("Erreur", "Veuillez remplir tous les champs !");
                return;
            }

            lieu.setNomLieu(nom);
            lieu.setAdresse(adresse);
            lieu.setVille(ville);

            if (!capStr.isEmpty()) {
                lieu.setCapaciteMax(Integer.parseInt(capStr));
            } else {
                lieu.setCapaciteMax(null);
            }

            lieuService.modifier(lieu);

            showAlert("Succès", "Lieu modifié !");
            System.out.println("✅ Lieu modifié");

            try {
                Parent root = FXMLLoader.load(getClass().getResource("/Lieux.fxml"));
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(new Scene(root));
            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (NumberFormatException e) {
            showAlert("Erreur", "Capacité doit être un nombre !");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void annuler(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Lieux.fxml"));
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
}