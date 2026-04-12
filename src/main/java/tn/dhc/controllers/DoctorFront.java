package tn.dhc.controllers;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;
import javafx.stage.Window;
import tn.dhc.entities.Fiche;
import tn.dhc.entities.Medicament;
import tn.dhc.entities.Ordonnance;
import tn.dhc.entities.User;
import tn.dhc.services.ServiceFiche;
import tn.dhc.services.ServiceMedicament;
import tn.dhc.services.ServiceOrdonnance;
import tn.dhc.services.UserService;

import java.util.ArrayList;
import java.util.List;

/**
 * Espace médecin : création de fiches et ordonnances pour un patient sélectionné,
 * et consultation des fiches / ordonnances créées par ce médecin.
 */
public class DoctorFront {

    @FXML
    private Label doctorWelcomeLabel;

    @FXML
    private ComboBox<User> doctorPatientCombo;

    @FXML
    private FlowPane doctorMyFichesFlow;

    @FXML
    private FlowPane doctorMyOrdonnancesFlow;

    private final UserService userService = new UserService();
    private final ServiceFiche ficheService = new ServiceFiche();
    private final ServiceOrdonnance ordonnanceService = new ServiceOrdonnance();
    private final ServiceMedicament medicamentService = new ServiceMedicament();

    @FXML
    public void initialize() {
        User me = UserService.getCurrentUser();
        if (doctorWelcomeLabel != null && me != null) {
            doctorWelcomeLabel.setText("Dr " + me.getPrenom() + " " + me.getNom()
                    + "  ·  Créez des fiches et ordonnances pour vos patients.");
        }
        if (doctorPatientCombo != null) {
            wirePatientCombo(doctorPatientCombo);
            doctorPatientCombo.setItems(FXCollections.observableArrayList(patientsOnly()));
        }
        if (doctorMyFichesFlow != null) {
            bindFlowWrap(doctorMyFichesFlow);
        }
        if (doctorMyOrdonnancesFlow != null) {
            bindFlowWrap(doctorMyOrdonnancesFlow);
        }
        doctorRefreshMyCare(null);
    }

    private static void bindFlowWrap(FlowPane flow) {
        if (flow == null) {
            return;
        }
        javafx.scene.control.ScrollPane sp = null;
        for (javafx.scene.Parent walk = flow.getParent(); walk != null; walk = walk.getParent()) {
            if (walk instanceof javafx.scene.control.ScrollPane scroll) {
                sp = scroll;
                break;
            }
        }
        if (sp == null) {
            return;
        }
        javafx.beans.value.ChangeListener<Number> listener = (obs, old, w) ->
                flow.setPrefWrapLength(Math.max(480, w.doubleValue() - 56));
        sp.widthProperty().addListener(listener);
        listener.changed(sp.widthProperty(), sp.getWidth(), sp.getWidth());
    }

    private List<User> patientsOnly() {
        return userService.getAll().stream().filter(MedicalFormDialogs::isPatientUser).toList();
    }

    private static void wirePatientCombo(ComboBox<User> combo) {
        combo.setPrefWidth(360);
        combo.setPromptText("Choisir un patient…");
        combo.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(User u, boolean empty) {
                super.updateItem(u, empty);
                setText(empty || u == null ? null : formatUser(u));
            }
        });
        combo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(User u, boolean empty) {
                super.updateItem(u, empty);
                setText(empty || u == null ? null : formatUser(u));
            }
        });
    }

    private static String formatUser(User u) {
        return (u.getPrenom() != null ? u.getPrenom().trim() : "") + " " + (u.getNom() != null ? u.getNom().trim() : "")
                + (u.getMail() != null ? "  ·  " + u.getMail() : "");
    }

    private String patientLabel(int userId) {
        User u = userService.getOneById(userId);
        if (u == null) {
            return "—";
        }
        return u.getPrenom() + " " + u.getNom();
    }

    

    @FXML
    public void deconnexion(ActionEvent event) {
        new UserService().logout();
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Login.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void doctorCreateFiche(ActionEvent event) {
        User p = doctorPatientCombo.getSelectionModel().getSelectedItem();
        if (p == null) {
            alert(Alert.AlertType.WARNING, "Sélectionnez d'abord un patient.");
            return;
        }
        User me = UserService.getCurrentUser();
        if (me == null) {
            return;
        }
        List<User> one = new ArrayList<>();
        one.add(p);
        MedicalFormDialogs.showFicheDialog(window(), "Nouvelle fiche médicale", null, one, p, me.getId())
                .ifPresent(ficheService::ajouter);
        doctorRefreshMyCare(null);
    }

    @FXML
    public void doctorCreateOrdonnance(ActionEvent event) {
        User p = doctorPatientCombo.getSelectionModel().getSelectedItem();
        if (p == null) {
            alert(Alert.AlertType.WARNING, "Sélectionnez d'abord un patient.");
            return;
        }
        User me = UserService.getCurrentUser();
        if (me == null) {
            return;
        }
        List<Fiche> fiches = ficheService.findByPatientUserId(p.getId());
        if (fiches.isEmpty()) {
            alert(Alert.AlertType.INFORMATION, "Créez d'abord une fiche médicale pour ce patient.");
            return;
        }
        List<Medicament> meds = medicamentService.getAll();
        if (meds.isEmpty()) {
            alert(Alert.AlertType.INFORMATION, "Aucun médicament en base : l'administrateur doit en ajouter.");
            return;
        }
        MedicalFormDialogs.showOrdonnanceDialog(window(), "Nouvelle ordonnance", null, fiches, meds, List.of(), me.getId())
                .ifPresent(r -> ordonnanceService.ajouterAvecMedicaments(r.ordonnance(), r.medicamentIds()));
        doctorRefreshMyCare(null);
    }

    @FXML
    public void doctorRefreshPatients(ActionEvent event) {
        if (doctorPatientCombo != null) {
            doctorPatientCombo.setItems(FXCollections.observableArrayList(patientsOnly()));
        }
    }

    @FXML
    public void doctorRefreshMyCare(ActionEvent event) {
        if (doctorMyFichesFlow == null || doctorMyOrdonnancesFlow == null) {
            return;
        }
        doctorMyFichesFlow.getChildren().clear();
        doctorMyOrdonnancesFlow.getChildren().clear();
        User me = UserService.getCurrentUser();
        if (me == null) {
            return;
        }
        for (Fiche f : ficheService.findByMedecinUserId(me.getId())) {
            doctorMyFichesFlow.getChildren().add(MedicalAdminCards.ficheCardViewOnly(f, patientLabel(f.getUserId())));
        }
        for (Ordonnance o : ordonnanceService.findByMedecinUserId(me.getId())) {
            Fiche linked = ficheService.getOneById(o.getFicheId());
            String ficheSummary = formatFicheSummary(linked);
            String medSummary = ordonnanceService.getMedicamentsSummaryForOrdonnance(o.getId());
            doctorMyOrdonnancesFlow.getChildren().add(MedicalAdminCards.ordonnanceCardViewOnly(o, ficheSummary, medSummary));
        }
    }

    private Window window() {
        if (doctorPatientCombo != null && doctorPatientCombo.getScene() != null) {
            return doctorPatientCombo.getScene().getWindow();
        }
        return null;
    }

    private void alert(Alert.AlertType type, String msg) {
        Alert a = new Alert(type);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
