package tn.dhc.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import tn.dhc.entities.*;
import tn.dhc.entities.NoteMedecin;
import tn.dhc.services.*;
import tn.dhc.utils.OrdonnancePdfExporter;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.Priority;

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

    @FXML private TextField annoncePatientSearch;
    @FXML private ComboBox<String> annoncePatientSort;
    @FXML private VBox annoncePatientContainer;

    private final UserService userService = new UserService();
    private final ServiceFiche ficheService = new ServiceFiche();
    private final ServiceOrdonnance ordonnanceService = new ServiceOrdonnance();
    private final CreneauService creneauService = new CreneauService();
    private final RdvService rdvService = new RdvService();
    private final EventService eventService = new EventService();
    private final LieuService lieuService = new LieuService();
    private final GoogleCalendarService googleCalendar = GoogleCalendarService.getInstance();
    private final NoteMedecinService noteService = new NoteMedecinService();

    // ── Médecins ──────────────────────────────────────────────────────────
    @FXML private VBox      medecinCardsContainer;
    @FXML private TextField medecinSearchField;
    @FXML private ComboBox<String> medecinSpecialiteFilter;

    // ── Calendrier ────────────────────────────────────────────────────────
    @FXML private GridPane calGrid;
    @FXML private GridPane calDaysHeader;
    @FXML private Label    calMonthLabel;
    @FXML private VBox     calEventDetail;
    private YearMonth calCurrentMonth = YearMonth.now();
    private final ServiceAnnonce svcAnnonce = new ServiceAnnonce();
    private final ServiceCommentaire svcCommentaire = new ServiceCommentaire();

    @FXML
    public void initialize() {
        if (patientFichesFlow != null) {
            bindFlowWrap(patientFichesFlow);
        }
        if (patientOrdonnancesFlow != null) {
            bindFlowWrap(patientOrdonnancesFlow);
        }

        refreshPatientMedical();

        loadCreneauxCards();

        // Connexion Google Calendar en arrière-plan
        javafx.application.Platform.runLater(() -> {
            new Thread(() -> {
                try {
                    googleCalendar.connect();
                    // Reconstruire le calendrier une fois connecté
                    javafx.application.Platform.runLater(this::buildCalendar);
                } catch (Exception ex) {
                    System.out.println("[Google Calendar] Connexion ignorée : " + ex.getMessage());
                }
            }, "google-cal-connect").start();
        });
        loadEvenementCards();

        User u = UserService.getCurrentUser();



        modNomUser.setText(u.getNom());
        modPrenomUser.setText(u.getPrenom());
        modEmailUser.setText(u.getMail());
        modTelUser.setText(String.valueOf(u.getTel()));
        modSpecialiteUser.setText(u.getSpecialite());
        modRoleUser.setText(u.getRole());

        javafx.application.Platform.runLater(this::initAnnoncesPatient);
        javafx.application.Platform.runLater(this::loadMedecinCards);
        // Wire search + filter
        javafx.application.Platform.runLater(() -> {
            if (medecinSearchField != null)
                medecinSearchField.textProperty().addListener((o, ov, nv) -> loadMedecinCards());
            if (medecinSpecialiteFilter != null) {
                // Alimenter le filtre spécialités
                medecinSpecialiteFilter.getItems().add("Toutes spécialités");
                userService.getAll().stream()
                        .filter(user -> isMedecin(u) && u.getSpecialite() != null && !u.getSpecialite().isBlank())
                        .map(user -> u.getSpecialite().trim())
                        .distinct().sorted()
                        .forEach(s -> medecinSpecialiteFilter.getItems().add(s));
                medecinSpecialiteFilter.setValue("Toutes spécialités");
                medecinSpecialiteFilter.setOnAction(e -> loadMedecinCards());
            }
        });
        javafx.application.Platform.runLater(this::buildCalendar);
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


    /**
     * Extrait la LocalDate d'un événement Google Calendar.
     */
    private static LocalDate extractGoogleEventDate(com.google.api.services.calendar.model.Event ge) {
        try {
            if (ge.getStart() == null) return null;
            if (ge.getStart().getDate() != null) {
                return LocalDate.parse(ge.getStart().getDate().toString());
            }
            if (ge.getStart().getDateTime() != null) {
                return java.time.Instant.ofEpochMilli(ge.getStart().getDateTime().getValue())
                        .atZone(java.time.ZoneId.of("Africa/Tunis"))
                        .toLocalDate();
            }
        } catch (Exception ex) {
            System.err.println("[Google Calendar] Parse date : " + ex.getMessage());
        }
        return null;
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

    // ════════════════════════════════════════════════════════════════════
    //  MÉDECINS + NOTATION
    // ════════════════════════════════════════════════════════════════════

    private void loadMedecinCards() {
        if (medecinCardsContainer == null) return;
        medecinCardsContainer.getChildren().clear();

        String search = medecinSearchField != null
                ? medecinSearchField.getText().toLowerCase().trim() : "";
        String specFilter = medecinSpecialiteFilter != null
                ? medecinSpecialiteFilter.getValue() : null;

        List<User> medecins = userService.getAll().stream()
                .filter(this::isMedecin)
                .filter(u -> search.isEmpty()
                        || u.getNom().toLowerCase().contains(search)
                        || u.getPrenom().toLowerCase().contains(search)
                        || (u.getSpecialite() != null && u.getSpecialite().toLowerCase().contains(search)))
                .filter(u -> specFilter == null || specFilter.equals("Toutes spécialités")
                        || specFilter.equals(u.getSpecialite()))
                .toList();

        if (medecins.isEmpty()) {
            Label empty = new Label("Aucun médecin trouvé.");
            empty.setStyle("-fx-text-fill:#999; -fx-font-size:14px;");
            medecinCardsContainer.getChildren().add(empty);
            return;
        }
        for (User m : medecins) {
            medecinCardsContainer.getChildren().add(buildMedecinCard(m));
        }
    }

    private HBox buildMedecinCard(User m) {
        double moyenne = noteService.getMoyenne(m.getId());
        int    nbNotes = noteService.getNbNotes(m.getId());

        HBox card = new HBox(16);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(16, 20, 16, 20));
        String base = "-fx-background-color:white; -fx-border-color:#b2ebf2; -fx-border-radius:12;" +
                " -fx-background-radius:12; -fx-effect:dropshadow(gaussian,rgba(0,0,0,0.07),8,0,0,2); -fx-cursor:hand;";
        card.setStyle(base);

        // Avatar
        Label avatar = new Label(initiales(m));
        avatar.setMinSize(52, 52); avatar.setMaxSize(52, 52);
        avatar.setStyle("-fx-background-color:#2980b9; -fx-background-radius:26;" +
                " -fx-text-fill:white; -fx-font-weight:bold; -fx-font-size:18px; -fx-alignment:center;");

        // Infos
        VBox info = new VBox(4); HBox.setHgrow(info, Priority.ALWAYS);
        Label name = new Label("Dr " + m.getPrenom() + " " + m.getNom());
        name.setStyle("-fx-font-size:16px; -fx-font-weight:bold; -fx-text-fill:#2c3e50;");
        Label spec = new Label(m.getSpecialite() != null && !m.getSpecialite().isBlank()
                ? "🏥 " + m.getSpecialite() : "🏥 Généraliste");
        spec.setStyle("-fx-font-size:13px; -fx-text-fill:#00796b;");

        // Étoiles moyenne
        HBox stars = buildStarsDisplay(moyenne);
        Label nbLabel = new Label("(" + nbNotes + " avis)");
        nbLabel.setStyle("-fx-font-size:12px; -fx-text-fill:#7f8c8d;");
        HBox starsRow = new HBox(6, stars, nbLabel);
        starsRow.setAlignment(Pos.CENTER_LEFT);

        info.getChildren().addAll(name, spec, starsRow);

        // Bouton voir avis
        Button btnAvis = new Button("⭐ Voir avis & noter");
        btnAvis.setStyle("-fx-background-color:#00b3a6; -fx-text-fill:white; -fx-background-radius:8;" +
                " -fx-font-size:13px; -fx-padding:9 18; -fx-cursor:hand;");
        btnAvis.setOnAction(ev -> openMedecinDetailPopup(m));

        card.getChildren().addAll(avatar, info, btnAvis);
        card.setOnMouseEntered(e -> card.setStyle(base.replace("white", "#f0fafa")));
        card.setOnMouseExited(e -> card.setStyle(base));
        return card;
    }

    private void openMedecinDetailPopup(User m) {
        User patient = UserService.getCurrentUser();

        Stage popup = new Stage();
        popup.setTitle("Dr " + m.getPrenom() + " " + m.getNom());
        popup.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        popup.setMinWidth(560);

        VBox root = new VBox(16);
        root.setPadding(new Insets(28));
        root.setStyle("-fx-background-color:#f5f7fa;");

        // ── En-tête médecin ──────────────────────────────────────────
        HBox header = new HBox(14);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(16));
        header.setStyle("-fx-background-color:white; -fx-background-radius:10;");

        Label avatar = new Label(initiales(m));
        avatar.setMinSize(56, 56); avatar.setMaxSize(56, 56);
        avatar.setStyle("-fx-background-color:#2980b9; -fx-background-radius:28;" +
                " -fx-text-fill:white; -fx-font-weight:bold; -fx-font-size:20px; -fx-alignment:center;");

        VBox headerInfo = new VBox(4);
        Label nameL = new Label("Dr " + m.getPrenom() + " " + m.getNom());
        nameL.setStyle("-fx-font-size:18px; -fx-font-weight:bold; -fx-text-fill:#2c3e50;");
        Label specL = new Label(m.getSpecialite() != null ? "🏥 " + m.getSpecialite() : "🏥 Généraliste");
        specL.setStyle("-fx-font-size:13px; -fx-text-fill:#00796b;");
        Label mailL = new Label("📧 " + m.getMail() + "   📞 " + m.getTel());
        mailL.setStyle("-fx-font-size:12px; -fx-text-fill:#7f8c8d;");

        double moyenne = noteService.getMoyenne(m.getId());
        int    nbNotes = noteService.getNbNotes(m.getId());
        HBox starsRow  = new HBox(6, buildStarsDisplay(moyenne),
                new Label(String.format("%.1f/5  (%d avis)", moyenne, nbNotes)));
        starsRow.setAlignment(Pos.CENTER_LEFT);
        ((Label) starsRow.getChildren().get(1)).setStyle("-fx-font-size:13px; -fx-text-fill:#555;");

        headerInfo.getChildren().addAll(nameL, specL, mailL, starsRow);
        header.getChildren().addAll(avatar, headerInfo);

        // ── Formulaire de notation (si patient connecté) ─────────────
        VBox noteBox = new VBox(10);
        noteBox.setPadding(new Insets(16));
        noteBox.setStyle("-fx-background-color:white; -fx-background-radius:10;");

        Label noteTitle = new Label("✏️ Votre note");
        noteTitle.setStyle("-fx-font-size:15px; -fx-font-weight:bold; -fx-text-fill:#00796b;");

        // Étoiles interactives
        final int[] selectedNote = {0};
        HBox starSelector = new HBox(6);
        starSelector.setAlignment(Pos.CENTER_LEFT);
        Label[] starBtns = new Label[5];
        for (int i = 0; i < 5; i++) {
            final int val = i + 1;
            Label star = new Label("☆");
            star.setStyle("-fx-font-size:28px; -fx-cursor:hand; -fx-text-fill:#f39c12;");
            star.setOnMouseClicked(ev -> {
                selectedNote[0] = val;
                for (int j = 0; j < 5; j++) {
                    starBtns[j].setText(j < val ? "★" : "☆");
                }
            });
            star.setOnMouseEntered(ev -> {
                for (int j = 0; j < 5; j++) {
                    starBtns[j].setText(j < val ? "★" : "☆");
                }
            });
            star.setOnMouseExited(ev -> {
                for (int j = 0; j < 5; j++) {
                    starBtns[j].setText(j < selectedNote[0] ? "★" : "☆");
                }
            });
            starBtns[i] = star;
            starSelector.getChildren().add(star);
        }

        // Pré-remplir si déjà noté
        if (patient != null) {
            NoteMedecin existing = noteService.getByMedecinAndPatient(m.getId(), patient.getId());
            if (existing != null) {
                selectedNote[0] = existing.getNote();
                for (int j = 0; j < 5; j++) starBtns[j].setText(j < existing.getNote() ? "★" : "☆");
            }
        }

        javafx.scene.control.TextArea commentArea = new javafx.scene.control.TextArea();
        commentArea.setPromptText("Laissez un commentaire (optionnel)...");
        commentArea.setPrefRowCount(3);
        commentArea.setWrapText(true);
        commentArea.setStyle("-fx-background-radius:8; -fx-border-radius:8; -fx-border-color:#e0e0e0;");

        // Pré-remplir le commentaire
        if (patient != null) {
            NoteMedecin existing = noteService.getByMedecinAndPatient(m.getId(), patient.getId());
            if (existing != null && existing.getCommentaire() != null)
                commentArea.setText(existing.getCommentaire());
        }

        Label noteStatus = new Label("");
        noteStatus.setStyle("-fx-font-size:12px;");

        Button btnSubmit = new Button("💾 Soumettre ma note");
        btnSubmit.setStyle("-fx-background-color:#00b3a6; -fx-text-fill:white; -fx-background-radius:8;" +
                " -fx-padding:9 20; -fx-cursor:hand; -fx-font-weight:bold;");

        if (patient == null) {
            btnSubmit.setDisable(true);
            noteStatus.setText("Connectez-vous pour noter.");
            noteStatus.setStyle("-fx-text-fill:#e74c3c;");
        }

        btnSubmit.setOnAction(ev -> {
            if (selectedNote[0] == 0) {
                noteStatus.setText("Sélectionnez une note (1 à 5 étoiles).");
                noteStatus.setStyle("-fx-text-fill:#e74c3c;");
                return;
            }
            noteService.ajouterOuModifier(m.getId(), patient.getId(),
                    selectedNote[0], commentArea.getText().trim());
            noteStatus.setText("✅ Note enregistrée !");
            noteStatus.setStyle("-fx-text-fill:#27ae60;");
            // Rafraîchir la liste
            loadMedecinCards();
        });

        noteBox.getChildren().addAll(noteTitle, starSelector, commentArea, noteStatus, btnSubmit);

        // ── Liste des avis ───────────────────────────────────────────
        Label avisTitle = new Label("💬 Avis des patients");
        avisTitle.setStyle("-fx-font-size:15px; -fx-font-weight:bold; -fx-text-fill:#00796b;");

        VBox avisList = new VBox(8);
        List<NoteMedecin> notes = noteService.getByMedecin(m.getId());
        if (notes.isEmpty()) {
            Label none = new Label("Aucun avis pour l'instant.");
            none.setStyle("-fx-text-fill:#999; -fx-font-size:13px;");
            avisList.getChildren().add(none);
        } else {
            for (NoteMedecin n : notes) {
                avisList.getChildren().add(buildAvisRow(n));
            }
        }

        javafx.scene.control.ScrollPane avisSP = new javafx.scene.control.ScrollPane(avisList);
        avisSP.setFitToWidth(true);
        avisSP.setHbarPolicy(javafx.scene.control.ScrollPane.ScrollBarPolicy.NEVER);
        avisSP.setPrefHeight(200);
        avisSP.setStyle("-fx-background-color:transparent; -fx-background:transparent;");

        Button btnFermer = new Button("✖ Fermer");
        btnFermer.setStyle("-fx-background-color:#e0e0e0; -fx-text-fill:#333; -fx-background-radius:8;" +
                " -fx-padding:9 20; -fx-cursor:hand;");
        btnFermer.setMaxWidth(Double.MAX_VALUE);
        btnFermer.setOnAction(ev -> popup.close());

        root.getChildren().addAll(header, noteBox, avisTitle, avisSP, btnFermer);

        javafx.scene.control.ScrollPane rootSP = new javafx.scene.control.ScrollPane(root);
        rootSP.setFitToWidth(true);
        rootSP.setHbarPolicy(javafx.scene.control.ScrollPane.ScrollBarPolicy.NEVER);
        popup.setScene(new Scene(rootSP, 580, 700));
        popup.show();
    }

    private HBox buildAvisRow(NoteMedecin n) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.TOP_LEFT);
        row.setPadding(new Insets(10, 14, 10, 14));
        row.setStyle("-fx-background-color:white; -fx-background-radius:8;" +
                " -fx-border-color:#e0e0e0; -fx-border-radius:8;");

        // Patient info
        User patient = userService.getOneById(n.getPatientId());
        String patName = patient != null ? patient.getPrenom() + " " + patient.getNom() : "Patient";
        String initials = patient != null ? initiales(patient) : "?";

        Label avatar = new Label(initials);
        avatar.setMinSize(38, 38); avatar.setMaxSize(38, 38);
        avatar.setStyle("-fx-background-color:#27ae60; -fx-background-radius:19;" +
                " -fx-text-fill:white; -fx-font-weight:bold; -fx-font-size:13px; -fx-alignment:center;");

        VBox body = new VBox(3); HBox.setHgrow(body, Priority.ALWAYS);
        HBox topRow = new HBox(8);
        Label patLabel = new Label(patName);
        patLabel.setStyle("-fx-font-weight:bold; -fx-font-size:13px; -fx-text-fill:#2c3e50;");
        Label dateLabel = new Label(n.getCreatedAt() != null
                ? n.getCreatedAt().toLocalDate().toString() : "");
        dateLabel.setStyle("-fx-font-size:11px; -fx-text-fill:#aaa;");
        HBox starsSmall = buildStarsDisplay(n.getNote());
        topRow.getChildren().addAll(patLabel, starsSmall, dateLabel);
        topRow.setAlignment(Pos.CENTER_LEFT);

        body.getChildren().add(topRow);
        if (n.getCommentaire() != null && !n.getCommentaire().isBlank()) {
            Label comment = new Label(n.getCommentaire());
            comment.setStyle("-fx-font-size:12px; -fx-text-fill:#555;");
            comment.setWrapText(true);
            body.getChildren().add(comment);
        }

        row.getChildren().addAll(avatar, body);
        return row;
    }

    /** Affiche des étoiles pleines/vides selon la note (double pour la moyenne). */
    private HBox buildStarsDisplay(double note) {
        HBox stars = new HBox(2);
        stars.setAlignment(Pos.CENTER_LEFT);
        for (int i = 1; i <= 5; i++) {
            Label star = new Label(i <= Math.round(note) ? "★" : "☆");
            star.setStyle("-fx-font-size:16px; -fx-text-fill:#f39c12;");
            stars.getChildren().add(star);
        }
        return stars;
    }

    private boolean isMedecin(User u) {
        if (u == null || u.getRole() == null) return false;
        String r = u.getRole().toLowerCase();
        return r.contains("medecin") || r.contains("médecin") || r.contains("doctor");
    }

    private String initiales(User u) {
        String p = (u.getPrenom() != null && !u.getPrenom().isBlank())
                ? u.getPrenom().substring(0, 1).toUpperCase() : "?";
        String n = (u.getNom() != null && !u.getNom().isBlank())
                ? u.getNom().substring(0, 1).toUpperCase() : "?";
        return p + n;
    }


    //_________________________________________________________________________________________________________________
    //_________________________________________________________________________________________________________________
    //_________________________CRENEAUX FERDAWS_________________________________________________________
    @FXML
    public void loadCreneauxCards() {
        creneauFlow.getChildren().clear();

        User current = UserService.getCurrentUser();
        if (current == null) return;

        List<Rdv> allRdvs = rdvService.getAll();

        for (Creneau c : creneauService.getAll()) {

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

                rdvService.add(rdv);

                // Marquer le créneau comme réservé
                c.setStatut("reserved");
                creneauService.modifier(c);

                popup.close();
                loadCreneauxCards();

                // Connexion Google Calendar en arrière-plan
                javafx.application.Platform.runLater(() -> {
                    new Thread(() -> {
                        try {
                            googleCalendar.connect();
                            // Reconstruire le calendrier une fois connecté
                            javafx.application.Platform.runLater(this::buildCalendar);
                        } catch (Exception ex) {
                            System.out.println("[Google Calendar] Connexion ignorée : " + ex.getMessage());
                        }
                    }, "google-cal-connect").start();
                });
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

            // Connexion Google Calendar en arrière-plan
            javafx.application.Platform.runLater(() -> {
                new Thread(() -> {
                    try {
                        googleCalendar.connect();
                        // Reconstruire le calendrier une fois connecté
                        javafx.application.Platform.runLater(this::buildCalendar);
                    } catch (Exception ex) {
                        System.out.println("[Google Calendar] Connexion ignorée : " + ex.getMessage());
                    }
                }, "google-cal-connect").start();
            });

            alert(Alert.AlertType.INFORMATION, "Rendez-vous annulé");

        } catch (Exception e) {
            e.printStackTrace();
            alert(Alert.AlertType.ERROR, e.getMessage());
        }
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

        // Compteur participants mis à jour dynamiquement
        Label participants = new Label("👥 Participants : " + (e.getNbParticipant() != null ? e.getNbParticipant() : 0));
        participants.setStyle("-fx-text-fill:#00796b; -fx-font-weight:bold;");

        Button btnParticiper = new Button("✅ Participer");
        btnParticiper.setStyle("-fx-background-color:#00b3a6; -fx-text-fill:white; -fx-background-radius:8; -fx-cursor:hand; -fx-padding:7 16;");

        // ── Vérifier capacité du lieu ──────────────────────────────────
        User currentUser = UserService.getCurrentUser();
        if (e.getEventLieuId() > 0) {
            Lieu lieuEvent = lieuService.getOneById(e.getEventLieuId());
            if (lieuEvent != null && lieuEvent.getCapaciteMax() != null) {
                int nbPart = e.getNbParticipant() != null ? e.getNbParticipant() : 0;
                if (nbPart >= lieuEvent.getCapaciteMax()) {
                    btnParticiper.setDisable(true);
                    btnParticiper.setText("🔒 Complet");
                    btnParticiper.setStyle("-fx-background-color:#bdbdbd; -fx-text-fill:#616161;" +
                            " -fx-background-radius:8; -fx-padding:7 16;");
                    // Afficher la capacité atteinte
                    participants.setText("👥 " + nbPart + "/" + lieuEvent.getCapaciteMax() + " — Complet");
                    participants.setStyle("-fx-text-fill:#e74c3c; -fx-font-weight:bold;");
                } else {
                    // Afficher nb/capacite_max
                    participants.setText("👥 " + nbPart + "/" + lieuEvent.getCapaciteMax() + " participants");
                }
            }
        }

        // Désactiver si déjà inscrit au chargement
        if (currentUser != null && eventService.isDejaParticipant(e.getId(), currentUser.getId())) {
            btnParticiper.setDisable(true);
            btnParticiper.setText("✔ Inscrit");
            btnParticiper.setStyle("-fx-background-color:#a5d6a7; -fx-text-fill:#1b5e20; -fx-background-radius:8; -fx-padding:7 16;");
        }

        btnParticiper.setOnAction(ev -> {
            User cur = UserService.getCurrentUser();
            if (cur == null) {
                alert(Alert.AlertType.ERROR, "Vous devez être connecté !");
                return;
            }

            // 1. Insérer dans table participant
            boolean added = eventService.ajouterParticipant(e.getId(), cur.getId());
            if (!added) {
                alert(Alert.AlertType.INFORMATION, "Vous participez déjà à cet événement.");
                return;
            }

            // 2. Incrémenter nb_participant
            eventService.incrementerParticipant(e.getId());

            // 3. Mettre à jour l'affichage
            int count = e.getNbParticipant() != null ? e.getNbParticipant() : 0;
            e.setNbParticipant(count + 1);

            // Afficher nb/capacite_max si connu
            Lieu lieuEvent = e.getEventLieuId() > 0 ? lieuService.getOneById(e.getEventLieuId()) : null;
            if (lieuEvent != null && lieuEvent.getCapaciteMax() != null) {
                int newCount = e.getNbParticipant();
                int cap = lieuEvent.getCapaciteMax();
                participants.setText("👥 " + newCount + "/" + cap + " participants");
                // Complet après inscription ?
                if (newCount >= cap) {
                    participants.setText("👥 " + newCount + "/" + cap + " — Complet");
                    participants.setStyle("-fx-text-fill:#e74c3c; -fx-font-weight:bold;");
                }
            } else {
                participants.setText("👥 Participants : " + e.getNbParticipant());
            }
            btnParticiper.setDisable(true);
            btnParticiper.setText("✔ Inscrit");
            btnParticiper.setStyle("-fx-background-color:#a5d6a7; -fx-text-fill:#1b5e20; -fx-background-radius:8; -fx-padding:7 16;");

            // 4. Sync Google Calendar
            if (googleCalendar.isConnected()) {
                new Thread(() -> {
                    String gId = googleCalendar.addEvent(
                            e.getTitreEvent(),
                            e.getDescription(),
                            e.getDateEvent(),
                            e.getHeureDebut(),
                            e.getHeureFin()
                    );
                    if (gId != null) {
                        javafx.application.Platform.runLater(() -> buildCalendar());
                        System.out.println("[Google Calendar] Événement synced ✓");
                    }
                }, "gc-sync").start();
            }
        });

        // ── Bouton "Voir détails + carte" ──────────────────────────────
        Button btnDetail = new Button("🗺️ Voir détails");
        btnDetail.setStyle("-fx-background-color:white; -fx-text-fill:#00b3a6;" +
                " -fx-border-color:#00b3a6; -fx-border-radius:8; -fx-background-radius:8;" +
                " -fx-cursor:hand; -fx-padding:7 16;");
        btnDetail.setOnAction(ev -> openEventDetailPopup(e));

        card.getChildren().addAll(title, theme, date, desc, participants, btnParticiper, btnDetail);

        return card;
    }

    // ════════════════════════════════════════════════════════════════════
    //  POPUP DÉTAIL ÉVÉNEMENT + GOOGLE MAPS
    // ════════════════════════════════════════════════════════════════════

    private void openEventDetailPopup(Event e) {
        // Récupérer le lieu
        Lieu lieu = null;
        if (e.getEventLieuId() > 0) {
            lieu = lieuService.getOneById(e.getEventLieuId());
        }
        final Lieu finalLieu = lieu;

        // ── Construire la popup ────────────────────────────────────────
        Stage popup = new Stage();
        popup.setTitle("📌 " + e.getTitreEvent());
        popup.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        popup.setMinWidth(820);
        popup.setMinHeight(620);

        // Layout principal : infos à gauche, carte à droite
        HBox root = new HBox(0);
        root.setStyle("-fx-background-color:#f5f7fa;");

        // ── Panneau gauche : infos ─────────────────────────────────────
        VBox leftPane = new VBox(16);
        leftPane.setPrefWidth(360);
        leftPane.setMinWidth(360);
        leftPane.setPadding(new Insets(28, 24, 28, 28));
        leftPane.setStyle("-fx-background-color:white;");

        // En-tête
        Label titleLbl = new Label(e.getTitreEvent());
        titleLbl.setStyle("-fx-font-size:20px; -fx-font-weight:bold; -fx-text-fill:#00796b;");
        titleLbl.setWrapText(true);

        // Badge thème
        Label themeBadge = new Label("🏥 " + (e.getThemeSante() != null ? e.getThemeSante() : "—"));
        themeBadge.setStyle("-fx-background-color:#e0f7fa; -fx-text-fill:#00796b;" +
                " -fx-background-radius:20; -fx-padding:4 12; -fx-font-size:12px; -fx-font-weight:bold;");

        // Séparateur
        javafx.scene.control.Separator sep = new javafx.scene.control.Separator();

        // Infos détaillées
        VBox details = new VBox(10);
        details.getChildren().addAll(
                infoRow("📅", "Date",
                        e.getDateEvent() != null ? e.getDateEvent().toString() : "—"),
                infoRow("🕐", "Horaires",
                        (e.getHeureDebut() != null ? e.getHeureDebut().toString() : "?") +
                                "  →  " + (e.getHeureFin() != null ? e.getHeureFin().toString() : "?")),
                infoRow("👥", "Participants",
                        String.valueOf(e.getNbParticipant() != null ? e.getNbParticipant() : 0))
        );

        // Description
        Label descTitle = new Label("📝 Description");
        descTitle.setStyle("-fx-font-size:13px; -fx-font-weight:bold; -fx-text-fill:#555;");
        javafx.scene.control.TextArea descArea = new javafx.scene.control.TextArea(
                e.getDescription() != null ? e.getDescription() : "—");
        descArea.setEditable(false);
        descArea.setWrapText(true);
        descArea.setPrefRowCount(4);
        descArea.setStyle("-fx-background-color:#f8fffe; -fx-border-color:#e0f2f0;" +
                " -fx-background-radius:8; -fx-border-radius:8;");

        // Lieu
        VBox lieuBox = new VBox(6);
        if (finalLieu != null) {
            Label lieuTitle = new Label("📍 Lieu");
            lieuTitle.setStyle("-fx-font-size:13px; -fx-font-weight:bold; -fx-text-fill:#555;");
            Label lieuNom  = new Label(finalLieu.getNomLieu());
            lieuNom.setStyle("-fx-font-size:14px; -fx-font-weight:bold; -fx-text-fill:#2c3e50;");
            Label lieuAddr = new Label(finalLieu.getAdresse() + ", " + finalLieu.getVille());
            lieuAddr.setStyle("-fx-font-size:12px; -fx-text-fill:#7f8c8d;");
            Label lieuCap  = new Label("👥 Capacité : " + (finalLieu.getCapaciteMax() != null
                    ? finalLieu.getCapaciteMax() : "—"));
            lieuCap.setStyle("-fx-font-size:12px; -fx-text-fill:#00796b;");
            lieuBox.getChildren().addAll(lieuTitle, lieuNom, lieuAddr, lieuCap);
        }

        Button btnFermer = new Button("✖ Fermer");
        btnFermer.setStyle("-fx-background-color:#e0e0e0; -fx-text-fill:#333;" +
                " -fx-background-radius:8; -fx-padding:10 20; -fx-cursor:hand;");
        btnFermer.setOnAction(ev -> popup.close());
        btnFermer.setMaxWidth(Double.MAX_VALUE);

        leftPane.getChildren().addAll(titleLbl, themeBadge, sep, details,
                descTitle, descArea, lieuBox, btnFermer);
        VBox.setVgrow(descArea, javafx.scene.layout.Priority.ALWAYS);

        // ── Panneau droit : Google Maps WebView ───────────────────────
        WebView webView = new WebView();
        webView.setMinWidth(460);
        HBox.setHgrow(webView, javafx.scene.layout.Priority.ALWAYS);
        WebEngine engine = webView.getEngine();
        engine.setJavaScriptEnabled(true);

        // Charger event_detail.html depuis resources
        String mapUrl = getClass().getResource("/event_detail.html").toExternalForm();
        engine.load(mapUrl);

        // Une fois chargé → géocoder l'adresse du lieu
        engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == javafx.concurrent.Worker.State.SUCCEEDED && finalLieu != null) {
                String address = finalLieu.getAdresse() + ", " + finalLieu.getVille() + ", Tunisie";
                // Échapper les apostrophes pour le JS
                String safeAddress = address.replace("'", "\'");
                engine.executeScript("locateAddress('" + safeAddress + "')");
            }
        });

        root.getChildren().addAll(leftPane, webView);

        popup.setScene(new Scene(root));
        popup.show();
    }

    /** Ligne d'info : icône + label + valeur */
    private HBox infoRow(String icon, String label, String value) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        Label iconLbl = new Label(icon); iconLbl.setStyle("-fx-font-size:16px;");
        VBox text = new VBox(1);
        Label lbl = new Label(label); lbl.setStyle("-fx-font-size:11px; -fx-text-fill:#999;");
        Label val = new Label(value);  val.setStyle("-fx-font-size:13px; -fx-font-weight:bold; -fx-text-fill:#2c3e50;");
        text.getChildren().addAll(lbl, val);
        row.getChildren().addAll(iconLbl, text);
        return row;
    }




    // ════════════════════════════════════════════════════════════════════
    //  CALENDRIER — navigation + grille mensuelle
    // ════════════════════════════════════════════════════════════════════

    @FXML public void calPrev(ActionEvent event)  { calCurrentMonth = calCurrentMonth.minusMonths(1); buildCalendar(); }
    @FXML public void calNext(ActionEvent event)  { calCurrentMonth = calCurrentMonth.plusMonths(1);  buildCalendar(); }
    @FXML public void calToday(ActionEvent event) { calCurrentMonth = YearMonth.now();                buildCalendar(); }

    private void buildCalendar() {
        if (calGrid == null || calMonthLabel == null) return;

        User current = UserService.getCurrentUser();

        // ── Titre du mois ──────────────────────────────────────────────
        String moisNom = calCurrentMonth.getMonth()
                .getDisplayName(TextStyle.FULL, Locale.FRENCH);
        moisNom = Character.toUpperCase(moisNom.charAt(0)) + moisNom.substring(1);
        calMonthLabel.setText(moisNom + " " + calCurrentMonth.getYear());

        // ── Header jours ──────────────────────────────────────────────
        if (calDaysHeader != null) {
            calDaysHeader.getChildren().clear();
            String[] jours = {"Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim"};
            for (int i = 0; i < 7; i++) {
                Label lbl = new Label(jours[i]);
                lbl.setMaxWidth(Double.MAX_VALUE);
                lbl.setAlignment(Pos.CENTER);
                lbl.setStyle("-fx-font-weight:bold; -fx-font-size:13px;" +
                        "-fx-text-fill:" + (i >= 5 ? "#e74c3c" : "#00796b") + "; -fx-padding:4 0;");
                GridPane.setHalignment(lbl, HPos.CENTER);
                calDaysHeader.add(lbl, i, 0);
            }
        }

        // ── Événements DHC du patient (table participant) ──────────────
        Map<LocalDate, List<tn.dhc.entities.Event>> eventMap = new HashMap<>();
        if (current != null) {
            for (tn.dhc.entities.Event e : eventService.getEventsByParticipant(current.getId())) {
                if (e.getDateEvent() != null) {
                    eventMap.computeIfAbsent(e.getDateEvent(), k -> new java.util.ArrayList<>()).add(e);
                }
            }
        }

        // ── Événements Google Calendar ─────────────────────────────────
        Map<LocalDate, List<String>> googleEventMap = new HashMap<>();
        if (googleCalendar.isConnected()) {
            for (com.google.api.services.calendar.model.Event ge
                    : googleCalendar.getEventsForMonth(calCurrentMonth.atDay(1))) {
                LocalDate gDate = extractGoogleEventDate(ge);
                if (gDate != null) {
                    googleEventMap.computeIfAbsent(gDate, k -> new java.util.ArrayList<>())
                            .add(ge.getSummary() != null ? ge.getSummary() : "Événement");
                }
            }
        }

        // ── Grille ────────────────────────────────────────────────────
        calGrid.getChildren().clear();
        calGrid.getRowConstraints().clear();

        LocalDate first   = calCurrentMonth.atDay(1);
        int startDow      = first.getDayOfWeek().getValue() - 1; // 0=Lun
        int daysInMonth   = calCurrentMonth.lengthOfMonth();
        LocalDate today   = LocalDate.now();

        int col = startDow, row = 0;

        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = calCurrentMonth.atDay(day);
            List<tn.dhc.entities.Event> eventsOnDay   = eventMap.getOrDefault(date, List.of());
            List<String> googleEventsOnDay = googleEventMap.getOrDefault(date, List.of());
            boolean isToday    = date.equals(today);
            boolean hasEvent   = !eventsOnDay.isEmpty() || !googleEventsOnDay.isEmpty();
            boolean isWeekend  = col >= 5;

            VBox cell = buildDayCell(day, isToday, hasEvent, isWeekend, eventsOnDay, googleEventsOnDay);
            calGrid.add(cell, col, row);

            if (calGrid.getRowConstraints().size() <= row) {
                RowConstraints rc = new RowConstraints();
                rc.setMinHeight(80); rc.setPrefHeight(90);
                calGrid.getRowConstraints().add(rc);
            }

            col++;
            if (col == 7) { col = 0; row++; }
        }
    }

    private VBox buildDayCell(int day, boolean isToday, boolean hasEvent,
                              boolean isWeekend,
                              List<tn.dhc.entities.Event> events,
                              List<String> googleEvents) {
        VBox cell = new VBox(3);
        cell.setPadding(new Insets(6));
        cell.setMaxWidth(Double.MAX_VALUE);
        cell.setMaxHeight(Double.MAX_VALUE);

        String bg, border, numColor;
        if (isToday)        { bg = "#00b3a6"; border = "#00796b"; numColor = "white"; }
        else if (hasEvent)  { bg = "#e0f7fa"; border = "#00b3a6"; numColor = "#00796b"; }
        else if (isWeekend) { bg = "#fafafa"; border = "#f0f0f0"; numColor = "#e74c3c"; }
        else                { bg = "white";   border = "#e0e0e0"; numColor = "#2c3e50"; }

        cell.setStyle("-fx-background-color:" + bg + "; -fx-border-color:" + border +
                "; -fx-border-radius:8; -fx-background-radius:8;" +
                " -fx-effect:dropshadow(gaussian,rgba(0,0,0,0.04),4,0,0,1); -fx-cursor:hand;");

        Label numLabel = new Label(String.valueOf(day));
        numLabel.setStyle("-fx-font-size:15px; -fx-font-weight:bold; -fx-text-fill:" + numColor + ";");
        cell.getChildren().add(numLabel);

        // Pastilles DHC (turquoise)
        int shown = 0;
        for (tn.dhc.entities.Event e : events) {
            if (shown >= 2) break;
            Label evtLbl = new Label("● " + truncate(e.getTitreEvent(), 13));
            evtLbl.setStyle("-fx-font-size:10px; -fx-text-fill:" + (isToday ? "white" : "#00796b") +
                    "; -fx-background-color:" + (isToday ? "rgba(255,255,255,0.25)" : "#b2ebf2") +
                    "; -fx-background-radius:4; -fx-padding:1 4;");
            evtLbl.setMaxWidth(Double.MAX_VALUE);
            cell.getChildren().add(evtLbl);
            shown++;
        }

        // Pastilles Google (violet)
        for (String gTitle : googleEvents) {
            if (shown >= 3) break;
            Label gLbl = new Label("🗓 " + truncate(gTitle, 12));
            gLbl.setStyle("-fx-font-size:10px; -fx-text-fill:" + (isToday ? "white" : "#7b1fa2") +
                    "; -fx-background-color:" + (isToday ? "rgba(255,255,255,0.25)" : "#e1bee7") +
                    "; -fx-background-radius:4; -fx-padding:1 4;");
            gLbl.setMaxWidth(Double.MAX_VALUE);
            cell.getChildren().add(gLbl);
            shown++;
        }

        if (events.size() + googleEvents.size() > 3) {
            int extra = events.size() + googleEvents.size() - 3;
            Label more = new Label("+" + extra + " autres");
            more.setStyle("-fx-font-size:10px; -fx-text-fill:" + (isToday ? "white" : "#7f8c8d") + ";");
            cell.getChildren().add(more);
        }

        cell.setOnMouseClicked(ev -> showEventDetail(events, googleEvents, calCurrentMonth.atDay(day)));
        return cell;
    }

    private void showEventDetail(List<tn.dhc.entities.Event> events,
                                 List<String> googleEvents, LocalDate date) {
        if (calEventDetail == null) return;
        calEventDetail.getChildren().clear();

        if (events.isEmpty() && googleEvents.isEmpty()) {
            calEventDetail.setVisible(false);
            calEventDetail.setManaged(false);
            return;
        }
        calEventDetail.setVisible(true);
        calEventDetail.setManaged(true);

        Label header = new Label("📅 Événements du " + date.getDayOfMonth() + " " +
                date.getMonth().getDisplayName(TextStyle.FULL, Locale.FRENCH) + " " + date.getYear());
        header.setStyle("-fx-font-size:15px; -fx-font-weight:bold; -fx-text-fill:#00796b;");
        calEventDetail.getChildren().add(header);

        // DHC events
        for (tn.dhc.entities.Event e : events) {
            HBox row = new HBox(12);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(8, 12, 8, 12));
            row.setStyle("-fx-background-color:white; -fx-background-radius:8;" +
                    " -fx-border-color:#b2ebf2; -fx-border-radius:8;");
            Label icon = new Label("🏥"); icon.setStyle("-fx-font-size:18px;");
            VBox info = new VBox(2); HBox.setHgrow(info, Priority.ALWAYS);
            Label title = new Label(e.getTitreEvent());
            title.setStyle("-fx-font-size:14px; -fx-font-weight:bold; -fx-text-fill:#00796b;");
            Label meta = new Label(
                    (e.getThemeSante() != null ? "🏷️ " + e.getThemeSante() + "   " : "") +
                            (e.getHeureDebut() != null ? "🕐 " + e.getHeureDebut() + " → " + e.getHeureFin() : "") +
                            (e.getNbParticipant() != null ? "   👥 " + e.getNbParticipant() : ""));
            meta.setStyle("-fx-font-size:12px; -fx-text-fill:#555;");
            info.getChildren().addAll(title, meta);
            row.getChildren().addAll(icon, info);
            calEventDetail.getChildren().add(row);
        }

        // Google events
        for (String gTitle : googleEvents) {
            HBox row = new HBox(12);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(8, 12, 8, 12));
            row.setStyle("-fx-background-color:white; -fx-background-radius:8;" +
                    " -fx-border-color:#ce93d8; -fx-border-radius:8;");
            Label icon = new Label("🗓"); icon.setStyle("-fx-font-size:18px;");
            Label title = new Label(gTitle);
            title.setStyle("-fx-font-size:14px; -fx-font-weight:bold; -fx-text-fill:#7b1fa2;");
            row.getChildren().addAll(icon, title);
            calEventDetail.getChildren().add(row);
        }
    }

    private static String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max) + "…";
    }




    //_______________________________________________________________________________________________
    private void alert(Alert.AlertType type, String msg) {
        Alert a = new Alert(type);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}