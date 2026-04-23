package tn.dhc.controllers;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;
import tn.dhc.entities.Fiche;
import tn.dhc.entities.Medicament;
import tn.dhc.entities.Ordonnance;
import tn.dhc.entities.Rdv;
import tn.dhc.entities.Creneau;
import tn.dhc.entities.User;
import tn.dhc.services.CreneauService;
import tn.dhc.services.RdvService;
import tn.dhc.services.ServiceFiche;
import tn.dhc.services.ServiceMedicament;
import tn.dhc.services.ServiceOrdonnance;
import tn.dhc.services.OrdonnanceSignedMailService;
import tn.dhc.services.SignatureApiService;
import tn.dhc.services.UserService;

import java.awt.Desktop;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

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
    @FXML
    private FlowPane doctorPendingRdvsFlow;
    @FXML
    private PieChart doctorRdvStatusPie;
    @FXML
    private LineChart<String, Number> doctorRdvLineChart;
    @FXML
    private BarChart<String, Number> doctorGraviteBarChart;



    private final UserService userService = new UserService();
    private final ServiceFiche ficheService = new ServiceFiche();
    private final ServiceOrdonnance ordonnanceService = new ServiceOrdonnance();
    private final ServiceMedicament medicamentService = new ServiceMedicament();
    private final RdvService rdvService = new RdvService();
    private final CreneauService creneauService = new CreneauService();
    private final SignatureApiService signatureApiService = new SignatureApiService();
    private final OrdonnanceSignedMailService ordonnanceSignedMailService = new OrdonnanceSignedMailService();

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
        if (doctorPendingRdvsFlow != null) {
            bindFlowWrap(doctorPendingRdvsFlow);
        }
        doctorRefreshRendezVous(null);
        doctorRefreshMyCare(null);
        loadDoctorStats();
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

    private static String formatFicheSummary(Fiche f) {
        if (f == null) {
            return "Fiche liée";
        }
        String d = f.getDate() != null ? f.getDate().toString() : "";
        return (f.getLibelleMaladie() != null ? f.getLibelleMaladie() : "Fiche") + " · " + d;
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
        MedicalFormDialogs.showFicheDialog(window(), "Nouvelle fiche médicale", null, one, p, me.getId(), true)
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
                .ifPresent(r -> {
                    int newId = ordonnanceService.ajouterAvecMedicaments(r.ordonnance(), r.medicamentIds());
                    if (newId <= 0) {
                        return;
                    }
                    Ordonnance created = r.ordonnance();
                    created.setId(newId);
                    if (!signatureApiService.isConfigured()) {
                        alert(Alert.AlertType.INFORMATION, "Ordonnance créée. Définissez SIGNATURE_API_KEY pour activer la signature électronique.");
                        return;
                    }
                    try {
                        Fiche linkedFiche = ficheService.getOneById(created.getFicheId());
                        List<Medicament> linkedMeds = ordonnanceService.findMedicamentsByOrdonnance(created.getId());
                        SignatureApiService.SignatureLaunchResult sign = signatureApiService.launchOrdonnanceSignature(
                                created, linkedFiche, p, me, linkedMeds);
                        ordonnanceService.updateSignatureInfo(created.getId(), sign.envelopeId(), sign.ceremonyUrl(), null, sign.status());
                        if (sign.ceremonyUrl() != null && !sign.ceremonyUrl().isBlank()) {
                            Desktop.getDesktop().browse(URI.create(sign.ceremonyUrl()));
                            alert(Alert.AlertType.INFORMATION, "Session de signature ouverte dans votre navigateur.");
                        } else {
                            alert(Alert.AlertType.INFORMATION, "Ordonnance envoyée à SignatureAPI. Vérifiez votre email pour signer.");
                        }
                    } catch (Exception ex) {
                        alert(Alert.AlertType.WARNING, "Ordonnance créée, mais la signature n'a pas pu démarrer: " + ex.getMessage());
                    }
                });
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
            String rdvSummary = formatRdvSummaryForFiche(f);
            doctorMyFichesFlow.getChildren().add(MedicalAdminCards.ficheCardViewOnly(f, patientLabel(f.getUserId()), rdvSummary));
        }
        for (Ordonnance o : ordonnanceService.findByMedecinUserId(me.getId())) {
            syncAndSendSignedOrdonnanceIfReady(o);
            Fiche linked = ficheService.getOneById(o.getFicheId());
            String ficheSummary = formatFicheSummary(linked);
            String medSummary = ordonnanceService.getMedicamentsSummaryForOrdonnance(o.getId());
            doctorMyOrdonnancesFlow.getChildren().add(MedicalAdminCards.ordonnanceCardViewOnly(o, ficheSummary, medSummary));
        }
        doctorRefreshRendezVous(null);
        loadDoctorStats();
    }

    @FXML
    public void doctorRefreshRendezVous(ActionEvent event) {
        if (doctorPendingRdvsFlow == null) {
            return;
        }
        doctorPendingRdvsFlow.getChildren().clear();
        User me = UserService.getCurrentUser();
        if (me == null) {
            return;
        }
        for (Rdv r : rdvService.findByDoctorUserId(me.getId())) {
            doctorPendingRdvsFlow.getChildren().add(buildDoctorRdvCard(r));
        }
        loadDoctorStats();
    }

    private VBox buildDoctorRdvCard(Rdv r) {
        User patient = userService.getOneById(r.getUserId());
        String patientLabel = patient != null ? (patient.getPrenom() + " " + patient.getNom()) : "Patient";
        Creneau c = creneauService.getOneById(r.getCreneauId());
        String schedule = r.getDateRdv() != null ? r.getDateRdv().toString() : "";
        if (c != null) {
            schedule = schedule + " " + c.getHdebut() + "-" + c.getHfin();
        }

        Label title = new Label("🩺 " + patientLabel);
        title.setStyle("-fx-font-size:16px; -fx-font-weight:bold; -fx-text-fill:#008f85;");
        Label lSchedule = new Label("📅 " + schedule);
        Label lMotif = new Label("📝 Motif : " + safeText(r.getMotif()));
        Label lPrio = new Label("⚡ Priorité : " + safeText(r.getPriorite()));
        Label lStatut = new Label("📌 Statut : " + safeText(r.getStatut()));

        Button confirm = new Button("✅ Confirmer");
        confirm.getStyleClass().add("btn-primary");
        confirm.setDisable("confirme".equalsIgnoreCase(safeText(r.getStatut())));
        confirm.setOnAction(e -> {
            rdvService.updateStatut(r.getId(), "confirme");
            doctorRefreshRendezVous(null);
            doctorRefreshMyCare(null);
            loadDoctorStats();
        });

        VBox box = new VBox(8, title, lSchedule, lMotif, lPrio, lStatut, confirm);
        box.getStyleClass().addAll("clinical-entity-card", "clinical-entity-card-comfort");
        box.setStyle("-fx-padding:18; -fx-background-color:white;");
        return box;
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

    private String formatRdvSummaryForFiche(Fiche fiche) {
        List<Rdv> rdvs = rdvService.findByFicheId(fiche.getId());
        if (rdvs.isEmpty()) {
            return "Aucun rendez-vous";
        }
        List<String> rows = new ArrayList<>();
        for (Rdv r : rdvs) {
            Creneau c = creneauService.getOneById(r.getCreneauId());
            String when = r.getDateRdv() != null ? r.getDateRdv().toString() : "";
            if (c != null) {
                when = when + " " + c.getHdebut() + "-" + c.getHfin();
            }
            rows.add(when + " · " + safeText(r.getMotif()) + " · " + safeText(r.getPriorite()) + " · " + safeText(r.getStatut()));
        }
        return String.join(" ; ", rows);
    }

    private static String safeText(String s) {
        return s == null || s.isBlank() ? "—" : s.trim();
    }

    private void loadDoctorStats() {
        User me = UserService.getCurrentUser();
        if (me == null) {
            return;
        }
        List<Rdv> rdvs = rdvService.findByDoctorUserId(me.getId());
        List<Fiche> fiches = ficheService.findByMedecinUserId(me.getId());

        if (doctorRdvStatusPie != null) {
            Map<String, Integer> byStatus = new LinkedHashMap<>();
            byStatus.put("En attente", 0);
            byStatus.put("Confirmé", 0);
            byStatus.put("Autres", 0);
            for (Rdv r : rdvs) {
                String s = safeText(r.getStatut()).toLowerCase(Locale.ROOT);
                if (s.contains("attente")) {
                    byStatus.put("En attente", byStatus.get("En attente") + 1);
                } else if (s.contains("confirm")) {
                    byStatus.put("Confirmé", byStatus.get("Confirmé") + 1);
                } else {
                    byStatus.put("Autres", byStatus.get("Autres") + 1);
                }
            }
            doctorRdvStatusPie.setData(FXCollections.observableArrayList(
                    byStatus.entrySet().stream()
                            .filter(e -> e.getValue() > 0)
                            .map(e -> new PieChart.Data(e.getKey(), e.getValue()))
                            .toList()
            ));
        }

        if (doctorRdvLineChart != null) {
            doctorRdvLineChart.getData().clear();
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("RDV");
            LocalDate start = LocalDate.now().minusDays(29);
            Map<LocalDate, Integer> byDay = new LinkedHashMap<>();
            for (int i = 0; i < 30; i++) {
                byDay.put(start.plusDays(i), 0);
            }
            for (Rdv r : rdvs) {
                if (r.getDateRdv() != null && !r.getDateRdv().isBefore(start)) {
                    byDay.put(r.getDateRdv(), byDay.getOrDefault(r.getDateRdv(), 0) + 1);
                }
            }
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM");
            for (Map.Entry<LocalDate, Integer> e : byDay.entrySet()) {
                series.getData().add(new XYChart.Data<>(e.getKey().format(fmt), e.getValue()));
            }
            doctorRdvLineChart.getData().add(series);
        }

        if (doctorGraviteBarChart != null) {
            doctorGraviteBarChart.getData().clear();
            int faible = 0, moderee = 0, elevee = 0, autres = 0;
            for (Fiche f : fiches) {
                String g = safeText(f.getGravite()).toLowerCase(Locale.ROOT);
                if (g.contains("faible")) {
                    faible++;
                } else if (g.contains("mod")) {
                    moderee++;
                } else if (g.contains("élev") || g.contains("elev")) {
                    elevee++;
                } else {
                    autres++;
                }
            }
            XYChart.Series<String, Number> s = new XYChart.Series<>();
            s.setName("Fiches");
            s.getData().add(new XYChart.Data<>("Faible", faible));
            s.getData().add(new XYChart.Data<>("Modérée", moderee));
            s.getData().add(new XYChart.Data<>("Élevée", elevee));
            s.getData().add(new XYChart.Data<>("Autres", autres));
            doctorGraviteBarChart.getData().add(s);
        }
    }
