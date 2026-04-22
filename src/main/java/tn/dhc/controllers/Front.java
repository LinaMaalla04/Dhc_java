package tn.dhc.controllers;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.collections.FXCollections;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import tn.dhc.entities.*;
import tn.dhc.services.*;
import tn.dhc.utils.OrdonnancePdfExporter;

import java.awt.Desktop;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.sql.Date;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Front {

    @FXML
    private FlowPane patientFichesFlow;

    @FXML
    private FlowPane patientOrdonnancesFlow;

    @FXML
    private Label reportLabel;

    @FXML
    private TextField modEmailUser;

    @FXML
    private PasswordField modMdpCUser;

    @FXML
    private PasswordField modMdpUser;

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
    private FlowPane creneauFlow;
    @FXML
    private FlowPane evenementFlow;
    @FXML
    private FlowPane weatherForecastFlow;
    @FXML
    private ComboBox<User> doctorCombo;
    @FXML
    private PieChart patientRdvStatusPie;
    @FXML
    private LineChart<String, Number> patientRdvLineChart;
    @FXML
    private BarChart<String, Number> patientActivityBarChart;
    @FXML
    private TextField medChatQuestionField;
    @FXML
    private TextArea medChatAnswerArea;

    @FXML private TextField annoncePatientSearch;
    @FXML private ComboBox<String> annoncePatientSort;
    @FXML private VBox annoncePatientContainer;

    @FXML private ComboBox<Medicament> patientMedSafetyCombo;
    @FXML private TextArea patientMedSafetyArea;

    private final UserService userService = new UserService();
    private final ServiceFiche ficheService = new ServiceFiche();
    private final ServiceOrdonnance ordonnanceService = new ServiceOrdonnance();
    private final CreneauService creneauService = new CreneauService();
    private final RdvService rdvService = new RdvService();
    private final EventService eventService = new EventService();
    private final LieuService lieuService = new LieuService();
    private final ServiceAnnonce svcAnnonce = new ServiceAnnonce();
    private final ServiceCommentaire svcCommentaire = new ServiceCommentaire();
    private final SignatureApiService signatureApiService = new SignatureApiService();
    private final OrdonnanceSignedMailService ordonnanceSignedMailService = new OrdonnanceSignedMailService();
    private final RxNormMedicationChatService rxNormMedicationChatService = new RxNormMedicationChatService();
    private final OpenWeatherForecastService openWeatherForecastService = new OpenWeatherForecastService();
    private final ServiceMedicament medicamentService = new ServiceMedicament();
    private final OpenFdaDrugInfoService openFdaDrugInfoService = new OpenFdaDrugInfoService();

    @FXML
    public void initialize() {
        if (patientFichesFlow != null) {
            bindFlowWrap(patientFichesFlow);
        }
        if (patientOrdonnancesFlow != null) {
            bindFlowWrap(patientOrdonnancesFlow);
        }

        refreshPatientMedical();
        initPatientMedSafetyTab();

        initDoctorFilter();
        loadCreneauxCards();
        loadEvenementCards();
        loadPatientStats();

        User u = UserService.getCurrentUser();



        modNomUser.setText(u.getNom());
        modPrenomUser.setText(u.getPrenom());
        modEmailUser.setText(u.getMail());
        modTelUser.setText(String.valueOf(u.getTel()));
        modSpecialiteUser.setText(u.getSpecialite());
        modRoleUser.setText(u.getRole());

        javafx.application.Platform.runLater(this::initAnnoncesPatient);
    }

    private void initPatientMedSafetyTab() {
        if (patientMedSafetyCombo == null) {
            return;
        }
        List<Medicament> meds = medicamentService.getAll();
        patientMedSafetyCombo.setItems(FXCollections.observableArrayList(meds));
        Callback<ListView<Medicament>, ListCell<Medicament>> factory = lv -> new ListCell<>() {
            @Override
            protected void updateItem(Medicament item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : formatMedicamentChoice(item));
            }
        };
        patientMedSafetyCombo.setCellFactory(factory);
        patientMedSafetyCombo.setButtonCell(factory.call(null));
        if (patientMedSafetyArea != null && meds.isEmpty()) {
            patientMedSafetyArea.setText(
                    "Aucun médicament n'est enregistré dans le catalogue pour le moment. "
                            + "Demandez à l'administrateur d'en ajouter pour utiliser cette consultation FDA.");
        }
    }

    private static String formatMedicamentChoice(Medicament m) {
        String n = m.getNomMedicament() != null ? m.getNomMedicament() : "?";
        String d = m.getDosage() != null && !m.getDosage().isBlank() ? m.getDosage() : "";
        String f = m.getForme() != null && !m.getForme().isBlank() ? m.getForme() : "";
        if (!d.isBlank() && !f.isBlank()) {
            return n + " · " + d + " · " + f;
        }
        if (!d.isBlank()) {
            return n + " · " + d;
        }
        if (!f.isBlank()) {
            return n + " · " + f;
        }
        return n;
    }

    @FXML
    void patientFetchOpenFdaSafety(ActionEvent event) {
        if (patientMedSafetyCombo == null || patientMedSafetyArea == null) {
            return;
        }
        Medicament m = patientMedSafetyCombo.getSelectionModel().getSelectedItem();
        if (m == null) {
            alert(Alert.AlertType.WARNING, "Choisissez un médicament dans la liste.");
            return;
        }
        Node src = (Node) event.getSource();
        src.setDisable(true);
        patientMedSafetyArea.setText("Chargement des données openFDA…");
        final String queryName = m.getNomMedicament();
        Task<String> task = new Task<>() {
            @Override
            protected String call() throws Exception {
                return openFdaDrugInfoService.fetchSideEffectsAndWarningsReport(queryName);
            }
        };
        task.setOnSucceeded(e -> {
            src.setDisable(false);
            patientMedSafetyArea.setText(task.getValue());
        });
        task.setOnFailed(e -> {
            src.setDisable(false);
            patientMedSafetyArea.setText("");
            Throwable ex = task.getException();
            String msg = ex != null && ex.getMessage() != null ? ex.getMessage() : "Erreur inconnue";
            alert(Alert.AlertType.ERROR, "Impossible de récupérer les données openFDA : " + msg);
        });
        Thread t = new Thread(task, "openfda-patient-safety");
        t.setDaemon(true);
        t.start();
    }

    @FXML
    private void askMedicationChatbot(ActionEvent event) {
        if (medChatAnswerArea == null) {
            return;
        }
        String question = medChatQuestionField != null ? medChatQuestionField.getText() : "";
        medChatAnswerArea.setText("Recherche RxNorm en cours...");

        javafx.concurrent.Task<String> t = new javafx.concurrent.Task<>() {
            @Override
            protected String call() {
                return rxNormMedicationChatService.answerMedicationQuestion(question);
            }
        };
        t.setOnSucceeded(e -> medChatAnswerArea.setText(t.getValue()));
        t.setOnFailed(e -> medChatAnswerArea.setText("Erreur chatbot: " + t.getException().getMessage()));

        Thread worker = new Thread(t, "rxnorm-chatbot");
        worker.setDaemon(true);
        worker.start();
    }


    //_________________________________________________________________________________________________________________
    //_________________________________________________________________________________________________________________
    //_________________________ANNONCES CHAIMA_________________________________________________________

    private void initAnnoncesPatient() {
        if (annoncePatientSort != null) {
            annoncePatientSort.setItems(javafx.collections.FXCollections.observableArrayList(
                    "📅 Date (récente → ancienne)",
                    "📅 Date (ancienne → récente)",
                    "🔴 Urgence (haute → basse)",
                    "🟢 Urgence (basse → haute)",
                    "✅ Disponible en premier"
            ));
            annoncePatientSort.setOnAction(e -> filterPatientAnnonces());
        }

        if (annoncePatientSearch != null) {
            annoncePatientSearch.textProperty().addListener((obs, o, n) -> filterPatientAnnonces());
        }

        loadPatientAnnonces();
    }

    private void filterPatientAnnonces() {
        String keyword = annoncePatientSearch != null ? annoncePatientSearch.getText().trim() : "";
        String sortLabel = annoncePatientSort != null ? annoncePatientSort.getValue() : null;
        String sortKey = labelToKey(sortLabel);

        try {
            List<Annonce> list;

            if (!keyword.isEmpty())
                list = svcAnnonce.rechercher(keyword);
            else if (sortKey != null)
                list = svcAnnonce.trier(sortKey);
            else
                list = svcAnnonce.getAll();

            renderPatientCards(list);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void loadPatientAnnonces() {
        try {
            renderPatientCards(svcAnnonce.getAll());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void renderPatientCards(List<Annonce> annonces) {
        if (annoncePatientContainer == null) return;

        annoncePatientContainer.getChildren().clear();

        if (annonces.isEmpty()) {
            Label empty = new Label("Aucune annonce disponible.");
            annoncePatientContainer.getChildren().add(empty);
            return;
        }

        for (Annonce a : annonces) {
            annoncePatientContainer.getChildren().add(buildPatientCard(a));
        }
    }
    private HBox buildPatientCard(Annonce a) {

        HBox card = new HBox(10);

        Label titre = new Label("📌 " + a.getTitre_annonce());
        Label date = new Label("📅 " + a.getDate_publication());

        Button btnVoir = new Button("👁 Voir");
        btnVoir.setOnAction(e -> openPatientViewPopup(a));

        card.getChildren().addAll(titre, date, btnVoir);

        return card;
    }
    private void openPatientViewPopup(Annonce a) {

        User me = UserService.getCurrentUser();
        if (me == null) return;

        Stage popup = new Stage();

        VBox root = new VBox(10);

        Label titre = new Label(a.getTitre_annonce());
        TextArea desc = new TextArea(a.getDescription());
        desc.setEditable(false);

        TextArea comment = new TextArea();
        comment.setPromptText("Votre commentaire...");

        Button send = new Button("Envoyer");

        send.setOnAction(e -> {
            try {
                Commentaire c = new Commentaire(
                        comment.getText(),
                        a.getIdAnnonce(),
                        me.getId()
                );

                svcCommentaire.ajouter(c);
                alert(Alert.AlertType.INFORMATION, "Commentaire ajouté !");
                popup.close();

            } catch (Exception ex) {
                alert(Alert.AlertType.ERROR, ex.getMessage());
            }
        });

        root.getChildren().addAll(titre, desc, comment, send);

        popup.setScene(new Scene(root, 400, 400));
        popup.show();
    }
    private String labelToKey(String label) {
        if (label == null) return null;

        if (label.contains("récente")) return "DATE_DESC";
        if (label.contains("ancienne")) return "DATE_ASC";
        if (label.contains("haute")) return "URGENCE_HIGH";
        if (label.contains("basse")) return "URGENCE_LOW";
        if (label.contains("Disponible")) return "DISPO";

        return null;
    }
    //_________________________________________________________________________________________________________________
    //_________________________________________________________________________________________________________________
    //_________________________FICHES ET ORDONNANCES_________________________________________________________

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
            String rdvSummary = formatRdvSummaryForFiche(f);
            patientFichesFlow.getChildren().add(MedicalAdminCards.ficheCardViewOnly(f, selfLabel, rdvSummary));
        }
        for (Ordonnance o : ordonnanceService.findByPatientUserId(me.getId())) {
            syncSignedDeliverableIfReady(o);
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
        if (signatureApiService.isConfigured() && sel.getSignatureEnvelopeId() != null && !sel.getSignatureEnvelopeId().isBlank()) {
            try {
                SignatureApiService.DeliverableStatus deliverable = signatureApiService.fetchDeliverableStatus(sel.getSignatureEnvelopeId());
                if ("generated".equalsIgnoreCase(deliverable.status()) && deliverable.deliverableUrl() != null && !deliverable.deliverableUrl().isBlank()) {
                    ordonnanceService.updateSignatureInfo(sel.getId(), sel.getSignatureEnvelopeId(), sel.getSignatureCeremonyUrl(), deliverable.deliverableUrl(), deliverable.status());
                    FileChooser fc = new FileChooser();
                    fc.setTitle("Enregistrer le PDF signé");
                    fc.setInitialFileName("ordonnance-signee-" + sel.getId() + ".pdf");
                    fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
                    Stage stage = (Stage) patientOrdonnancesFlow.getScene().getWindow();
                    java.io.File file = fc.showSaveDialog(stage);
                    if (file == null) {
                        return;
                    }
                    signatureApiService.downloadSignedPdf(deliverable.deliverableUrl(), file.toPath());
                    alert(Alert.AlertType.INFORMATION, "PDF signé téléchargé :\n" + file.toPath().toAbsolutePath());
                    return;
                }
                alert(Alert.AlertType.INFORMATION, "Le PDF signé n'est pas encore prêt. Statut actuel : " + safeText(deliverable.status()));
                return;
            } catch (Exception ex) {
                alert(Alert.AlertType.WARNING, "Impossible de récupérer le PDF signé pour l'instant : " + ex.getMessage());
                return;
            }
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


    //_________________________________________________________________________________________________________________
    //_________________________________________________________________________________________________________________
    //_________________________USERS LINA_________________________________________________________
    @FXML
    void modifier(ActionEvent event) {

        try {
            User current = UserService.getCurrentUser();

            if (current == null) {
                alert(Alert.AlertType.ERROR, "Aucun utilisateur connecté !");
                return;
            }

            String nom = modNomUser.getText();
            String prenom = modPrenomUser.getText();
            String mail = modEmailUser.getText();
            String telStr = modTelUser.getText();
            String role = modRoleUser.getText();
            String specialite = modSpecialiteUser.getText();

            if (nom.isEmpty() || prenom.isEmpty() || mail.isEmpty() || telStr.isEmpty()) {
                alert(Alert.AlertType.WARNING, "Champs obligatoires !");
                return;
            }

            int tel = Integer.parseInt(telStr);


            current.setNom(nom);
            current.setPrenom(prenom);
            current.setMail(mail);
            current.setTel(tel);
            current.setRole(role);
            current.setSpecialite(specialite);


            userService.modifier(current);


            UserService.setCurrentUser(current);

            alert(Alert.AlertType.INFORMATION, "Profil modifié avec succès !");

        } catch (Exception e) {
            e.printStackTrace();
            alert(Alert.AlertType.ERROR, "Erreur : " + e.getMessage());
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


    //_________________________________________________________________________________________________________________
    //_________________________________________________________________________________________________________________
    //_________________________CRENEAUX FERDAWS_________________________________________________________
    @FXML
    public void loadCreneauxCards() {
        creneauFlow.getChildren().clear();

        User current = UserService.getCurrentUser();
        if (current == null) return;
        User selectedDoctor = doctorCombo != null ? doctorCombo.getValue() : null;

        List<Rdv> allRdvs = rdvService.getAll();

        for (Creneau c : creneauService.getAll()) {
            if (selectedDoctor != null && c.getUserId() != selectedDoctor.getId()) {
                continue;
            }

            // Chercher s'il y a un RDV sur ce créneau
            java.util.Optional<Rdv> rdvOpt = allRdvs.stream()
                    .filter(r -> r.getCreneauId() == c.getId())
                    .findFirst();

            if (rdvOpt.isPresent()) {
                Rdv rdv = rdvOpt.get();

                if (rdv.getUserId() == current.getId()) {
                    // C'est le RDV DU PATIENT CONNECTÉ → afficher sa card avec annulation
                    creneauFlow.getChildren().add(createMyRdvCard(rdv));
                }
                // Réservé par un AUTRE patient → on ne l'affiche PAS du tout

            } else {
                // Disponible → afficher normalement
                creneauFlow.getChildren().add(createAvailableCard(c));
            }
        }
    }

    private Node createAvailableCard(Creneau c) {

        VBox card = new VBox(10);
        card.setPrefWidth(220);

        card.setStyle("""
        -fx-padding: 12;
        -fx-background-color: #e8f5e9;
        -fx-border-color: #2ecc71;
        -fx-border-radius: 10;
        -fx-background-radius: 10;
    """);

        Label date = new Label("📅 " + c.getDateCreneau());
        Label heure = new Label("🕐 " + c.getHdebut() + " - " + c.getHfin());

        Button btn = new Button("Réserver");
        btn.setStyle("-fx-background-color:#2ecc71; -fx-text-fill:white;");

        btn.setOnAction(e -> reserverCreneau(c));

        card.getChildren().addAll(date, heure, btn);

        return card;
    }


    private Node createReservedCard(Creneau c) {

        VBox card = new VBox(10);
        card.setPrefWidth(220);

        card.setStyle("""
        -fx-padding: 12;
        -fx-background-color: #ffebee;
        -fx-border-color: #e74c3c;
        -fx-border-radius: 10;
        -fx-background-radius: 10;
    """);

        Label date = new Label("📅 " + c.getDateCreneau());
        Label heure = new Label("🕐 " + c.getHdebut() + " - " + c.getHfin());

        Label info = new Label("🔒 Réservé");

        card.getChildren().addAll(date, heure, info);

        return card;
    }
    //______________________________RDVS FERDAWS_________________________________________________
    private void reserverCreneau(Creneau c) {
        User current = UserService.getCurrentUser();
        if (current == null) {
            alert(Alert.AlertType.ERROR, "Vous devez être connecté !");
            return;
        }

        // ── Popup de confirmation avec motif et priorité ──────────────────
        javafx.stage.Stage popup = new javafx.stage.Stage();
        popup.setTitle("Confirmer le rendez-vous");
        popup.initModality(javafx.stage.Modality.APPLICATION_MODAL);

        VBox root = new VBox(14);
        root.setStyle("-fx-padding:24; -fx-background-color:white;");

        Label title = new Label("📅 " + c.getDateCreneau() + "   🕐 " + c.getHdebut() + " - " + c.getHfin());
        title.setStyle("-fx-font-size:15px; -fx-font-weight:bold; -fx-text-fill:#00b3a6;");

        Label motifLabel = new Label("Motif de la consultation :");
        TextField motifField = new TextField();
        motifField.setPromptText("Ex : Consultation générale, suivi...");
        motifField.setPrefHeight(38);

        Label prioriteLabel = new Label("Priorité :");
        ComboBox<String> prioriteCombo = new ComboBox<>();
        prioriteCombo.getItems().addAll("Normale", "Urgente", "Faible");
        prioriteCombo.setValue("Normale");
        prioriteCombo.setPrefHeight(38);
        prioriteCombo.setMaxWidth(Double.MAX_VALUE);

        Button btnConfirm = new Button("✅ Confirmer le RDV");
        btnConfirm.setStyle("-fx-background-color:#00b3a6; -fx-text-fill:white; -fx-font-size:14px; -fx-background-radius:8; -fx-padding:10 20;");
        btnConfirm.setMaxWidth(Double.MAX_VALUE);

        btnConfirm.setOnAction(ev -> {
            try {
                String motif = motifField.getText().trim();
                if (motif.isEmpty()) {
                    alert(Alert.AlertType.WARNING, "Veuillez saisir un motif.");
                    return;
                }

                Rdv rdv = new Rdv();
                rdv.setMotif(motif);
                rdv.setPriorite(prioriteCombo.getValue().toLowerCase());
                rdv.setStatut("en_attente");
                rdv.setDateRdv(c.getDateCreneau());
                rdv.setCreneauId(c.getId());
                rdv.setUserId(current.getId());
                Fiche linkedFiche = ensureDoctorFicheForPatient(c.getUserId(), current.getId());
                rdv.setFicheId(linkedFiche.getId());

                rdvService.add(rdv);

                // Marquer le créneau comme réservé
                c.setStatut("reserved");
                creneauService.modifier(c);

                popup.close();
                loadCreneauxCards();
                refreshPatientMedical();
                alert(Alert.AlertType.INFORMATION, "Rendez-vous pris avec succès !");

            } catch (Exception ex) {
                ex.printStackTrace();
                alert(Alert.AlertType.ERROR, ex.getMessage());
            }
        });

        root.getChildren().addAll(title, motifLabel, motifField, prioriteLabel, prioriteCombo, btnConfirm);
        popup.setScene(new javafx.scene.Scene(root, 380, 300));
        popup.showAndWait();
    }
    private Node createMyRdvCard(Rdv r) {

        Creneau c = creneauService.getOneById(r.getCreneauId());

        VBox card = new VBox(10);
        card.setPrefWidth(220);

        card.setStyle("""
        -fx-padding: 12;
        -fx-background-color: #e6ffe6;  /* 🟢 vert clair */
        -fx-border-color: #2ecc71;
        -fx-border-radius: 10;
        -fx-background-radius: 10;
    """);

        Label title = new Label("🟢 MON RDV");
        Label date = new Label("📅 " + c.getDateCreneau());
        Label heure = new Label("🕐 " + c.getHdebut() + " - " + c.getHfin());

        Button cancel = new Button("❌ Annuler mon rendez-vous");

        cancel.setStyle("-fx-background-color:#e74c3c; -fx-text-fill:white;");

        cancel.setOnAction(e -> annulerRdv(r, c));

        card.getChildren().addAll(title, date, heure, cancel);

        return card;
    }
    private void annulerRdv(Rdv r, Creneau c) {

        try {

            rdvService.delete(r.getId());

            c.setStatut("dispo");
            creneauService.modifier(c);

            loadCreneauxCards();
            refreshPatientMedical();

            alert(Alert.AlertType.INFORMATION, "Rendez-vous annulé");

        } catch (Exception e) {
            e.printStackTrace();
            alert(Alert.AlertType.ERROR, e.getMessage());
        }
    }

    private Fiche findDoctorFicheForPatient(int doctorId, int patientId) {
        return ficheService.findLatestByPatientAndMedecin(patientId, doctorId);
    }

    private Fiche ensureDoctorFicheForPatient(int doctorId, int patientId) {
        Fiche fiche = findDoctorFicheForPatient(doctorId, patientId);
        if (fiche != null) {
            return fiche;
        }

        // First booking path: create an initial doctor-linked fiche so the RDV can be related.
        Fiche created = new Fiche();
        created.setUserId(patientId);
        created.setMedecinUserId(doctorId);
        created.setDate(Date.valueOf(LocalDate.now()));
        created.setPoids(0.0);
        created.setTaille(0.0);
        created.setGlycemie(0.0);
        created.setGrpSanguin(null);
        created.setAllergie(null);
        created.setMaladieChronique(null);
        created.setTension(null);
        created.setLibelleMaladie("Consultation initiale");
        created.setGravite("Faible");
        created.setRecommandation("Fiche créée automatiquement lors de la première prise de rendez-vous.");
        ficheService.ajouter(created);

        Fiche inserted = findDoctorFicheForPatient(doctorId, patientId);
        if (inserted != null) {
            return inserted;
        }
        throw new IllegalStateException("Impossible de créer/récupérer la fiche médicale liée au médecin.");
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

    private void initDoctorFilter() {
        if (doctorCombo == null) {
            return;
        }
        doctorCombo.setItems(FXCollections.observableArrayList(doctorsOnly()));
        doctorCombo.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(User u, boolean empty) {
                super.updateItem(u, empty);
                setText(empty || u == null ? null : doctorLabel(u));
            }
        });
        doctorCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(User u, boolean empty) {
                super.updateItem(u, empty);
                setText(empty || u == null ? null : doctorLabel(u));
            }
        });
        doctorCombo.setOnAction(e -> loadCreneauxCards());
    }

    private List<User> doctorsOnly() {
        List<User> doctors = new ArrayList<>();
        for (User u : userService.getAll()) {
            if (u == null || u.getRole() == null) {
                continue;
            }
            String role = u.getRole().trim().toLowerCase();
            if (role.contains("medecin") || role.contains("médecin")) {
                doctors.add(u);
            }
        }
        return doctors;
    }

    private static String doctorLabel(User u) {
        String fullName = (u.getPrenom() != null ? u.getPrenom().trim() : "")
                + " " + (u.getNom() != null ? u.getNom().trim() : "");
        String specialite = (u.getSpecialite() != null && !u.getSpecialite().isBlank())
                ? " · " + u.getSpecialite().trim()
                : "";
        return "Dr " + fullName.trim() + specialite;
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

    private void syncSignedDeliverableIfReady(Ordonnance o) {
        if (!signatureApiService.isConfigured()) {
            return;
        }
        if (o.getSignatureEnvelopeId() == null || o.getSignatureEnvelopeId().isBlank()) {
            return;
        }
        try {
            SignatureApiService.DeliverableStatus deliverable = signatureApiService.fetchDeliverableStatus(o.getSignatureEnvelopeId());
            if ("generated".equalsIgnoreCase(deliverable.status())
                    && deliverable.deliverableUrl() != null
                    && !deliverable.deliverableUrl().isBlank()
                    && (o.getSignatureDeliverableUrl() == null || o.getSignatureDeliverableUrl().isBlank())) {
                ordonnanceService.updateSignatureInfo(o.getId(), o.getSignatureEnvelopeId(), o.getSignatureCeremonyUrl(), deliverable.deliverableUrl(), deliverable.status());
                o.setSignatureDeliverableUrl(deliverable.deliverableUrl());
                o.setSignatureStatus(deliverable.status());
                sendSignedOrdonnanceMailIfNeeded(o);
            }
        } catch (Exception ignored) {
        }
    }

    private void loadPatientStats() {
        User me = UserService.getCurrentUser();
        if (me == null) {
            return;
        }
        List<Rdv> myRdvs = rdvService.getAll().stream()
                .filter(r -> r.getUserId() == me.getId())
                .toList();
        List<Ordonnance> myOrdonnances = ordonnanceService.findByPatientUserId(me.getId());

        if (patientRdvStatusPie != null) {
            Map<String, Integer> byStatus = new LinkedHashMap<>();
            byStatus.put("En attente", 0);
            byStatus.put("Confirmé", 0);
            byStatus.put("Autres", 0);
            for (Rdv r : myRdvs) {
                String s = safeText(r.getStatut()).toLowerCase(Locale.ROOT);
                if (s.contains("attente")) {
                    byStatus.put("En attente", byStatus.get("En attente") + 1);
                } else if (s.contains("confirm")) {
                    byStatus.put("Confirmé", byStatus.get("Confirmé") + 1);
                } else {
                    byStatus.put("Autres", byStatus.get("Autres") + 1);
                }
            }
            patientRdvStatusPie.setData(FXCollections.observableArrayList(
                    byStatus.entrySet().stream()
                            .filter(e -> e.getValue() > 0)
                            .map(e -> new PieChart.Data(e.getKey(), e.getValue()))
                            .toList()
            ));
        }

        if (patientRdvLineChart != null) {
            patientRdvLineChart.getData().clear();
            XYChart.Series<String, Number> rdvSeries = new XYChart.Series<>();
            rdvSeries.setName("RDV");
            LocalDate start = LocalDate.now().minusDays(29);
            Map<LocalDate, Integer> rdvByDay = new LinkedHashMap<>();
            for (int i = 0; i < 30; i++) {
                rdvByDay.put(start.plusDays(i), 0);
            }
            for (Rdv r : myRdvs) {
                LocalDate d = r.getDateRdv();
                if (d != null && !d.isBefore(start)) {
                    rdvByDay.put(d, rdvByDay.getOrDefault(d, 0) + 1);
                }
            }
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM");
            for (Map.Entry<LocalDate, Integer> e : rdvByDay.entrySet()) {
                rdvSeries.getData().add(new XYChart.Data<>(e.getKey().format(fmt), e.getValue()));
            }
            patientRdvLineChart.getData().add(rdvSeries);
        }

        if (patientActivityBarChart != null) {
            patientActivityBarChart.getData().clear();
            XYChart.Series<String, Number> ordSeries = new XYChart.Series<>();
            ordSeries.setName("Ordonnances");
            XYChart.Series<String, Number> rdvSeries = new XYChart.Series<>();
            rdvSeries.setName("RDV");

            List<YearMonth> months = new ArrayList<>();
            YearMonth now = YearMonth.now();
            for (int i = 5; i >= 0; i--) {
                months.add(now.minusMonths(i));
            }
            DateTimeFormatter monthFmt = DateTimeFormatter.ofPattern("MM/yy");
            for (YearMonth ym : months) {
                int ordCount = (int) myOrdonnances.stream()
                        .filter(o -> o.getDate() != null && YearMonth.from(o.getDate().toLocalDate()).equals(ym))
                        .count();
                int rdvCount = (int) myRdvs.stream()
                        .filter(r -> r.getDateRdv() != null && YearMonth.from(r.getDateRdv()).equals(ym))
                        .count();
                String label = ym.format(monthFmt);
                ordSeries.getData().add(new XYChart.Data<>(label, ordCount));
                rdvSeries.getData().add(new XYChart.Data<>(label, rdvCount));
            }
            patientActivityBarChart.getData().addAll(ordSeries, rdvSeries);
        }
    }

    private void sendSignedOrdonnanceMailIfNeeded(Ordonnance o) {
        if (o.getSignatureEmailSentAt() != null) {
            return;
        }
        if (o.getSignatureDeliverableUrl() == null || o.getSignatureDeliverableUrl().isBlank()) {
            return;
        }
        try {
            User patient = UserService.getCurrentUser();
            if (patient == null) {
                return;
            }
            byte[] signedPdf = signatureApiService.downloadSignedPdfBytes(o.getSignatureDeliverableUrl());
            ordonnanceSignedMailService.sendSignedOrdonnance(patient, signedPdf, "ordonnance-signee-" + o.getId() + ".pdf");
            ordonnanceService.markSignedEmailSent(o.getId());
            o.setSignatureEmailSentAt(new java.sql.Timestamp(System.currentTimeMillis()));
        } catch (Exception ignored) {
        }
    }



    //_________________________________________________________________________________________________________________
    //_________________________________________________________________________________________________________________
    //_________________________EVENEMENTS HAIFA_________________________________________________________

    @FXML
    public void loadEvenementCards() {

        evenementFlow.getChildren().clear();

        List<Event> events = eventService.getAll();

        for (Event e : events) {
            evenementFlow.getChildren().add(createEventCard(e));
        }
        loadWeatherForecastPanel(events);
    }

    @FXML
    public void refreshWeatherForecast(ActionEvent event) {
        loadWeatherForecastPanel(eventService.getAll());
    }

    private Node createOtherRdvCard(Rdv r) {

        Creneau c = creneauService.getOneById(r.getCreneauId());

        VBox card = new VBox(10);
        card.setPrefWidth(220);

        card.setStyle("""
        -fx-padding: 12;
        -fx-background-color: #ffffff;
        -fx-border-color: #ddd;
        -fx-border-radius: 10;
        -fx-background-radius: 10;
    """);

        Label date = new Label("📅 " + c.getDateCreneau());
        Label heure = new Label("🕐 " + c.getHdebut() + " - " + c.getHfin());

        Label info = new Label("🔒 Réservé");

        card.getChildren().addAll(date, heure, info);

        return card;
    }

    private Node createEventCard(Event e) {

        VBox card = new VBox(10);
        card.setPrefWidth(240);

        card.setStyle("""
        -fx-padding: 14;
        -fx-background-color: #f0fafa;
        -fx-border-color: #00b3a6;
        -fx-border-radius: 10;
        -fx-background-radius: 10;
    """);

        Label title = new Label("📌 " + e.getTitreEvent());
        title.setStyle("-fx-font-weight:bold; -fx-font-size:14px; -fx-text-fill:#00796b;");

        Label theme = new Label("🏥 " + (e.getThemeSante() != null ? e.getThemeSante() : ""));
        Label date  = new Label("📅 " + e.getDateEvent());

        Label desc = new Label("📝 " + (e.getDescription() != null ? e.getDescription() : ""));
        desc.setWrapText(true);
        desc.setMaxWidth(210);
        Label meteo = new Label("🌤 Prévision météo: chargement...");
        meteo.setWrapText(true);
        meteo.setMaxWidth(210);
        meteo.setStyle("-fx-text-fill:#2563eb; -fx-font-size:11px;");

        // Compteur participants mis à jour dynamiquement
        Label participants = new Label("👥 Participants : " + (e.getNbParticipant() != null ? e.getNbParticipant() : 0));
        participants.setStyle("-fx-text-fill:#00796b; -fx-font-weight:bold;");

        Button btnParticiper = new Button("✅ Participer");
        btnParticiper.setStyle("-fx-background-color:#00b3a6; -fx-text-fill:white; -fx-background-radius:8; -fx-cursor:hand; -fx-padding:7 16;");
        Button btnPartager = new Button("🔗 Partager");
        btnPartager.setStyle("-fx-background-color:#3b82f6; -fx-text-fill:white; -fx-background-radius:8; -fx-cursor:hand; -fx-padding:7 16;");

        btnParticiper.setOnAction(ev -> {
            // Incrémenter en DB
            eventService.incrementerParticipant(e.getId());

            // Mettre à jour l'affichage localement
            int current = e.getNbParticipant() != null ? e.getNbParticipant() : 0;
            e.setNbParticipant(current + 1);
            participants.setText("👥 Participants : " + e.getNbParticipant());

            btnParticiper.setDisable(true);
            btnParticiper.setText("✔ Inscrit");
            btnParticiper.setStyle("-fx-background-color:#a5d6a7; -fx-text-fill:#1b5e20; -fx-background-radius:8; -fx-padding:7 16;");
        });

        ContextMenu shareMenu = new ContextMenu();
        MenuItem shareFacebook = new MenuItem("Facebook");
        MenuItem shareX = new MenuItem("X / Twitter");
        MenuItem shareLinkedIn = new MenuItem("LinkedIn");
        MenuItem shareWhatsApp = new MenuItem("WhatsApp");
        shareFacebook.setOnAction(ev -> shareEventToNetwork(e, "facebook"));
        shareX.setOnAction(ev -> shareEventToNetwork(e, "x"));
        shareLinkedIn.setOnAction(ev -> shareEventToNetwork(e, "linkedin"));
        shareWhatsApp.setOnAction(ev -> shareEventToNetwork(e, "whatsapp"));
        shareMenu.getItems().addAll(shareFacebook, shareX, shareLinkedIn, shareWhatsApp);
        btnPartager.setOnAction(ev -> {
            if (shareMenu.isShowing()) {
                shareMenu.hide();
            } else {
                shareMenu.show(btnPartager, javafx.geometry.Side.BOTTOM, 0, 0);
            }
        });

        HBox actions = new HBox(8, btnParticiper, btnPartager);
        card.getChildren().addAll(title, theme, date, desc, meteo, participants, actions);
        fillWeatherForecastAsync(e, meteo);

        return card;
    }

    private void fillWeatherForecastAsync(Event e, Label meteoLabel) {
        javafx.concurrent.Task<String> task = new javafx.concurrent.Task<>() {
            @Override
            protected String call() {
                String city = "Tunis";
                try {
                    if (e.getEventLieuId() > 0) {
                        Lieu lieu = lieuService.getOneById(e.getEventLieuId());
                        if (lieu != null && lieu.getVille() != null && !lieu.getVille().isBlank()) {
                            city = lieu.getVille().trim();
                        }
                    }
                } catch (Exception ignored) {
                }
                return openWeatherForecastService.getForecastSummary(city, e.getDateEvent());
            }
        };
        task.setOnSucceeded(ev -> meteoLabel.setText(task.getValue()));
        task.setOnFailed(ev -> meteoLabel.setText("🌤 Prévision météo indisponible."));
        Thread worker = new Thread(task, "event-weather-" + e.getId());
        worker.setDaemon(true);
        worker.start();
    }

    private void loadWeatherForecastPanel(List<Event> events) {
        if (weatherForecastFlow == null) {
            return;
        }
        weatherForecastFlow.getChildren().clear();
        Label loading = new Label("Chargement des prévisions...");
        loading.setStyle("-fx-text-fill:#2563eb;");
        weatherForecastFlow.getChildren().add(loading);

        javafx.concurrent.Task<List<OpenWeatherForecastService.DailyForecast>> task = new javafx.concurrent.Task<>() {
            @Override
            protected List<OpenWeatherForecastService.DailyForecast> call() {
                String city = "Tunis";
                try {
                    if (events != null && !events.isEmpty()) {
                        Event first = events.get(0);
                        if (first.getEventLieuId() > 0) {
                            Lieu l = lieuService.getOneById(first.getEventLieuId());
                            if (l != null && l.getVille() != null && !l.getVille().isBlank()) {
                                city = l.getVille().trim();
                            }
                        }
                    }
                } catch (Exception ignored) {
                }
                return openWeatherForecastService.getFiveDayForecast(city);
            }
        };
        task.setOnSucceeded(ev -> {
            weatherForecastFlow.getChildren().clear();
            List<OpenWeatherForecastService.DailyForecast> rows = task.getValue();
            if (rows == null || rows.isEmpty()) {
                Label empty = new Label("Prévisions indisponibles pour le moment.");
                empty.setStyle("-fx-text-fill:#ef4444; -fx-font-weight:600;");
                weatherForecastFlow.getChildren().add(empty);
                return;
            }
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM");
            for (OpenWeatherForecastService.DailyForecast r : rows) {
                VBox box = new VBox(4);
                box.setPrefWidth(145);
                box.setStyle("-fx-background-color:white; -fx-background-radius:10; -fx-padding:10; -fx-border-color:#c7d2fe; -fx-border-radius:10; -fx-border-width:1;");
                Label d = new Label("📅 " + r.date().format(fmt));
                d.setStyle("-fx-font-weight:bold; -fx-text-fill:#1f2937;");
                Label s = new Label(r.summary());
                s.setWrapText(true);
                s.setStyle("-fx-text-fill:#334155; -fx-font-size:11px;");
                Label t = new Label("🌡 " + String.format("%.1f", r.tempC()) + "°C");
                t.setStyle("-fx-font-weight:700; -fx-text-fill:#0284c7;");
                box.getChildren().addAll(d, s, t);
                weatherForecastFlow.getChildren().add(box);
            }
        });
        task.setOnFailed(ev -> {
            weatherForecastFlow.getChildren().clear();
            Label err = new Label("Prévisions indisponibles.");
            err.setStyle("-fx-text-fill:#ef4444; -fx-font-weight:600;");
            weatherForecastFlow.getChildren().add(err);
        });

        Thread worker = new Thread(task, "weather-forecast-panel");
        worker.setDaemon(true);
        worker.start();
    }

    private void shareEventToNetwork(Event e, String network) {
        try {
            String eventUrl = "https://dhc-events.local/event/" + e.getId();
            String text = "Je participe a l'evenement \"" + safeText(e.getTitreEvent())
                    + "\" le " + safeText(String.valueOf(e.getDateEvent())) + " via DHC.";

            String encodedUrl = URLEncoder.encode(eventUrl, StandardCharsets.UTF_8);
            String encodedText = URLEncoder.encode(text, StandardCharsets.UTF_8);

            String shareUrl = switch (network.toLowerCase(Locale.ROOT)) {
                case "facebook" -> "https://www.facebook.com/sharer/sharer.php?u=" + encodedUrl + "&quote=" + encodedText;
                case "x" -> "https://twitter.com/intent/tweet?text=" + encodedText + "&url=" + encodedUrl;
                case "linkedin" -> "https://www.linkedin.com/sharing/share-offsite/?url=" + encodedUrl;
                case "whatsapp" -> "https://wa.me/?text=" + encodedText + "%20" + encodedUrl;
                default -> null;
            };

            if (shareUrl == null) {
                alert(Alert.AlertType.WARNING, "Réseau social non supporté.");
                return;
            }
            if (!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                alert(Alert.AlertType.WARNING, "Ouverture du navigateur non supportée sur cette machine.");
                return;
            }
            Desktop.getDesktop().browse(URI.create(shareUrl));
        } catch (Exception ex) {
            alert(Alert.AlertType.ERROR, "Partage impossible : " + ex.getMessage());
        }
    }



    //_______________________________________________________________________________________________
    private void alert(Alert.AlertType type, String msg) {
        Alert a = new Alert(type);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}