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
import tn.dhc.entities.*;
import tn.dhc.services.*;
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
        loadEvenementCards();

        User u = UserService.getCurrentUser();



        modNomUser.setText(u.getNom());
        modPrenomUser.setText(u.getPrenom());
        modEmailUser.setText(u.getMail());
        modTelUser.setText(String.valueOf(u.getTel()));
        modSpecialiteUser.setText(u.getSpecialite());
        modRoleUser.setText(u.getRole());

        javafx.application.Platform.runLater(this::initAnnoncesPatient);
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

        List<Creneau> creneaux = creneauService.getAll();

        for (Creneau c : creneaux) {

            boolean isReserved = rdvService.getAll().stream()
                    .anyMatch(r -> r.getCreneauId() == c.getId());

            if (isReserved) {
                creneauFlow.getChildren().add(createReservedCard(c));
            } else {
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

        try {
            User current = UserService.getCurrentUser();

            if (current == null) {
                alert(Alert.AlertType.ERROR, "Vous devez être connecté !");
                return;
            }

            Rdv rdv = new Rdv();
            rdv.setDateRdv(c.getDateCreneau());
            rdv.setCreneauId(c.getId());
            rdv.setUserId(current.getId());
            rdv.setStatut("en_attente");

            rdvService.add(rdv);

            c.setStatut("reserved");
            creneauService.modifier(c);

            loadCreneauxCards();
            alert(Alert.AlertType.INFORMATION, "Rendez-vous pris avec succès !");

        } catch (Exception e) {
            e.printStackTrace();
            alert(Alert.AlertType.ERROR, e.getMessage());
        }
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
        card.setPrefWidth(220);

        card.setStyle("""
        -fx-padding: 12;
        -fx-background-color: #ffffff;
        -fx-border-color: #ddd;
        -fx-border-radius: 10;
        -fx-background-radius: 10;
    """);

        Label title = new Label("📌 " + e.getTitreEvent());
        Label date = new Label("📅 " + e.getDateEvent());

        Label desc = new Label("📝 " + e.getDescription());


        card.getChildren().addAll(title, date, desc);

        return card;
    }



    //_______________________________________________________________________________________________
    private void alert(Alert.AlertType type, String msg) {
        Alert a = new Alert(type);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
