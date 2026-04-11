package tn.dhc.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import tn.dhc.entities.Fiche;
import tn.dhc.entities.Medicament;
import tn.dhc.entities.Ordonnance;
import tn.dhc.entities.User;
import tn.dhc.services.ServiceFiche;
import tn.dhc.services.ServiceOrdonnance;
import tn.dhc.services.UserService;
import tn.dhc.utils.OrdonnancePdfExporter;

import java.nio.file.Path;
import java.util.List;

public class Front {

    @FXML
    private FlowPane patientFichesFlow;

    @FXML
    private FlowPane patientOrdonnancesFlow;

    @FXML
    private Label reportLabel;

    private final UserService userService = new UserService();
    private final ServiceFiche ficheService = new ServiceFiche();
    private final ServiceOrdonnance ordonnanceService = new ServiceOrdonnance();

    @FXML
    public void initialize() {
        if (patientFichesFlow != null) {
            bindFlowWrap(patientFichesFlow);
        }
        if (patientOrdonnancesFlow != null) {
            bindFlowWrap(patientOrdonnancesFlow);
        }
        refreshPatientMedical();
    }

    private static void bindFlowWrap(FlowPane flow) {
        if (flow == null) {
            return;
        }
        ScrollPane sp = null;
        for (javafx.scene.Parent walk = flow.getParent(); walk != null; walk = walk.getParent()) {
            if (walk instanceof ScrollPane scroll) {
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

    @FXML
    public void refreshPatientMedical() {
        if (patientFichesFlow == null || patientOrdonnancesFlow == null) {
            return;
        }
        patientFichesFlow.getChildren().clear();
        patientOrdonnancesFlow.getChildren().clear();
        User me = UserService.getCurrentUser();
        if (me == null) {
            return;
        }
        String selfLabel = me.getPrenom() + " " + me.getNom();
        for (Fiche f : ficheService.findByPatientUserId(me.getId())) {
            patientFichesFlow.getChildren().add(MedicalAdminCards.ficheCardViewOnly(f, selfLabel));
        }
        for (Ordonnance o : ordonnanceService.findByPatientUserId(me.getId())) {
            Fiche linked = ficheService.getOneById(o.getFicheId());
            String ficheSummary = formatFicheSummary(linked);
            String medSummary = ordonnanceService.getMedicamentsSummaryForOrdonnance(o.getId());
            patientOrdonnancesFlow.getChildren().add(
                    MedicalAdminCards.ordonnanceCardWithPdfExport(o, ficheSummary, medSummary,
                            () -> exportOrdonnanceToPdf(o)));
        }
    }

    private static String formatFicheSummary(Fiche f) {
        if (f == null) {
            return "Fiche liée";
        }
        String d = f.getDate() != null ? f.getDate().toString() : "";
        return (f.getLibelleMaladie() != null ? f.getLibelleMaladie() : "Fiche") + " · " + d;
    }

    private void exportOrdonnanceToPdf(Ordonnance sel) {
        User me = UserService.getCurrentUser();
        if (me == null || patientOrdonnancesFlow == null) {
            return;
        }
        Fiche fiche = ficheService.getOneById(sel.getFicheId());
        if (fiche == null) {
            alert(Alert.AlertType.ERROR, "Fiche liée introuvable.");
            return;
        }
        List<Medicament> meds = ordonnanceService.findMedicamentsByOrdonnance(sel.getId());
        User prescripteur = sel.getMedecinUserId() != null ? userService.getOneById(sel.getMedecinUserId()) : null;

        FileChooser fc = new FileChooser();
        fc.setTitle("Enregistrer l'ordonnance PDF");
        fc.setInitialFileName("ordonnance-" + sel.getId() + ".pdf");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
        Stage stage = (Stage) patientOrdonnancesFlow.getScene().getWindow();
        java.io.File file = fc.showSaveDialog(stage);
        if (file == null) {
            return;
        }
        Path path = file.toPath();
        try {
            OrdonnancePdfExporter.write(sel, fiche, me, prescripteur, meds, path);
            alert(Alert.AlertType.INFORMATION, "PDF enregistré :\n" + path.toAbsolutePath());
        } catch (Exception ex) {
            ex.printStackTrace();
            alert(Alert.AlertType.ERROR, "Impossible d'écrire le PDF : " + ex.getMessage());
        }
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

    private void alert(Alert.AlertType type, String msg) {
        Alert a = new Alert(type);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
