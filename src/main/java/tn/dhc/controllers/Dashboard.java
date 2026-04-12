package tn.dhc.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.collections.FXCollections;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;
import tn.dhc.entities.Creneau;
import tn.dhc.entities.Event;
import tn.dhc.entities.Fiche;
import tn.dhc.entities.Medicament;
import tn.dhc.entities.Ordonnance;
import tn.dhc.entities.Pharmacie;
import tn.dhc.entities.User;
import tn.dhc.services.CreneauService;
import tn.dhc.services.EventService;
import tn.dhc.services.ServiceFiche;
import tn.dhc.services.ServiceMedicament;
import tn.dhc.services.ServiceOrdonnance;
import tn.dhc.services.ServicePharmacie;
import tn.dhc.services.UserService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class Dashboard {

    @FXML
    private ListView<User> AffUsers;

    @FXML
    private ListView<Creneau> AffCreneaux;

    @FXML
    private ListView<Event> AffEvents;

    @FXML
    private TextField RechCreneauText;

    @FXML
    private TextField RechEventText;

    @FXML
    private TextField RechUserText;

    @FXML
    private Label adminNameLabel;

    @FXML
    private Label reportLabel;

    @FXML
    private FlowPane pharmacyCardsFlow;

    @FXML
    private FlowPane medicamentCardsFlow;

    @FXML
    private FlowPane ficheCardsFlow;

    @FXML
    private FlowPane ordonnanceCardsFlow;

    @FXML
    private TextField searchPharmacy;

    @FXML
    private TextField searchMedicament;

    @FXML
    private TextField searchFiche;

    @FXML
    private TextField searchOrdonnance;

    @FXML
    private ComboBox<String> pharmacyFilterCombo;

    @FXML
    private ComboBox<String> pharmacySortCombo;

    @FXML
    private ComboBox<String> medicamentFilterCombo;

    @FXML
    private ComboBox<String> medicamentSortCombo;

    @FXML
    private ComboBox<String> ficheFilterCombo;

    @FXML
    private ComboBox<String> ficheSortCombo;

    @FXML
    private ComboBox<String> ordonnanceFilterCombo;

    @FXML
    private ComboBox<String> ordonnanceSortCombo;

    private final UserService userService = new UserService();
    private final CreneauService creneauService = new CreneauService();
    private final EventService eventService = new EventService();
    private final ServicePharmacie pharmacieService = new ServicePharmacie();
    private final ServiceMedicament medicamentService = new ServiceMedicament();
    private final ServiceFiche ficheService = new ServiceFiche();
    private final ServiceOrdonnance ordonnanceService = new ServiceOrdonnance();

    private Window primaryWindow;

    @FXML
    public void initialize() {
        loadUsers();
        loadCreneaux();
        loadEvents();

        User cur = UserService.getCurrentUser();
        if (cur != null && adminNameLabel != null) {
            adminNameLabel.setText(cur.getPrenom() + " " + cur.getNom() + "  ·  " + cur.getRole());
        }

        Platform.runLater(() -> {
            if (adminNameLabel != null && adminNameLabel.getScene() != null) {
                primaryWindow = adminNameLabel.getScene().getWindow();
            }
            wireClinicalSection();
        });
    }

    private void wireClinicalSection() {
        if (pharmacyCardsFlow == null) {
            return;
        }
        bindFlowWrap(pharmacyCardsFlow);
        bindFlowWrap(medicamentCardsFlow);
        bindFlowWrap(ficheCardsFlow);
        bindFlowWrap(ordonnanceCardsFlow);

        initClinicalFilterSortCombos();

        searchPharmacy.textProperty().addListener((obs, o, n) -> loadPharmacyCards());
        searchMedicament.textProperty().addListener((obs, o, n) -> loadMedicamentCards());
        searchFiche.textProperty().addListener((obs, o, n) -> loadFicheCards());
        searchOrdonnance.textProperty().addListener((obs, o, n) -> loadOrdonnanceCards());

        loadPharmacyCards();
        loadMedicamentCards();
        loadFicheCards();
        loadOrdonnanceCards();
    }

    private void initClinicalFilterSortCombos() {
        if (pharmacyFilterCombo != null) {
            pharmacyFilterCombo.setItems(FXCollections.observableArrayList("Toutes"));
            pharmacyFilterCombo.getSelectionModel().selectFirst();
            pharmacySortCombo.setItems(FXCollections.observableArrayList("Nom (A → Z)", "Nom (Z → A)"));
            pharmacySortCombo.getSelectionModel().selectFirst();
            pharmacyFilterCombo.valueProperty().addListener((o, a, b) -> loadPharmacyCards());
            pharmacySortCombo.valueProperty().addListener((o, a, b) -> loadPharmacyCards());
        }
        if (medicamentFilterCombo != null) {
            medicamentFilterCombo.setItems(FXCollections.observableArrayList("Toutes", "Stock < 10"));
            medicamentFilterCombo.getSelectionModel().selectFirst();
            medicamentSortCombo.setItems(FXCollections.observableArrayList(
                    "Nom (A → Z)", "Nom (Z → A)", "Expiration ↑", "Expiration ↓", "Stock ↑", "Stock ↓"));
            medicamentSortCombo.getSelectionModel().selectFirst();
            medicamentFilterCombo.valueProperty().addListener((o, a, b) -> loadMedicamentCards());
            medicamentSortCombo.valueProperty().addListener((o, a, b) -> loadMedicamentCards());
        }
        if (ficheFilterCombo != null) {
            ficheFilterCombo.setItems(FXCollections.observableArrayList(
                    "Toutes gravités", "Faible", "Modérée", "Élevée"));
            ficheFilterCombo.getSelectionModel().selectFirst();
            ficheSortCombo.setItems(FXCollections.observableArrayList(
                    "Date (↑ ancien)", "Date (↓ récent)", "Patient (A → Z)"));
            ficheSortCombo.getSelectionModel().selectFirst();
            ficheFilterCombo.valueProperty().addListener((o, a, b) -> loadFicheCards());
            ficheSortCombo.valueProperty().addListener((o, a, b) -> loadFicheCards());
        }
        if (ordonnanceFilterCombo != null) {
            ordonnanceFilterCombo.setItems(FXCollections.observableArrayList("Toutes", "Durée ≥ 30 jours"));
            ordonnanceFilterCombo.getSelectionModel().selectFirst();
            ordonnanceSortCombo.setItems(FXCollections.observableArrayList(
                    "Date (↑)", "Date (↓)", "Durée (↑)", "Durée (↓)"));
            ordonnanceSortCombo.getSelectionModel().selectFirst();
            ordonnanceFilterCombo.valueProperty().addListener((o, a, b) -> loadOrdonnanceCards());
            ordonnanceSortCombo.valueProperty().addListener((o, a, b) -> loadOrdonnanceCards());
        }
    }

    private static void bindFlowWrap(FlowPane flow) {
        if (flow == null) {
            return;
        }
        javafx.scene.Parent p = flow.getParent();
        if (!(p instanceof ScrollPane sp)) {
            return;
        }
        javafx.beans.value.ChangeListener<Number> listener = (obs, old, w) ->
                flow.setPrefWrapLength(Math.max(300, w.doubleValue() - 36));
        sp.widthProperty().addListener(listener);
        listener.changed(sp.widthProperty(), sp.getWidth(), sp.getWidth());
    }

    private Window window() {
        if (primaryWindow != null) {
            return primaryWindow;
        }
        if (adminNameLabel != null && adminNameLabel.getScene() != null) {
            return adminNameLabel.getScene().getWindow();
        }
        return null;
    }

    /* ---------- Pharmacies ---------- */

    @FXML
    public void refreshPharmacyCards(ActionEvent event) {
        loadPharmacyCards();
    }

    @FXML
    public void addPharmacie(ActionEvent event) {
        MedicalFormDialogs.showPharmacieDialog(window(), "Nouvelle pharmacie", null)
                .ifPresent(p -> {
                    pharmacieService.ajouter(p);
                    loadPharmacyCards();
                });
    }

    private void editPharmacie(Pharmacie p) {
        MedicalFormDialogs.showPharmacieDialog(window(), "Modifier la pharmacie", p)
                .ifPresent(x -> {
                    pharmacieService.modifier(x);
                    loadPharmacyCards();
                });
    }

    private void deletePharmacie(Pharmacie p) {
        if (!confirm("Supprimer", "Supprimer la pharmacie « " + p.getNom() + " » ?")) {
            return;
        }
        pharmacieService.supprimer(p);
        loadPharmacyCards();
    }

    private void loadPharmacyCards() {
        if (pharmacyCardsFlow == null) {
            return;
        }
        pharmacyCardsFlow.getChildren().clear();
        String q = norm(searchPharmacy.getText());
        String sort = pharmacySortCombo != null && pharmacySortCombo.getValue() != null
                ? pharmacySortCombo.getValue() : "Nom (A → Z)";

        List<Pharmacie> rows = new ArrayList<>();
        for (Pharmacie p : pharmacieService.getAll()) {
            if (passesPharmacyFilters(p, q)) {
                rows.add(p);
            }
        }
        rows.sort(pharmacyComparator(sort));
        for (Pharmacie p : rows) {
            pharmacyCardsFlow.getChildren().add(MedicalAdminCards.pharmacieCard(p,
                    () -> editPharmacie(p),
                    () -> deletePharmacie(p)));
        }
    }

    private boolean passesPharmacyFilters(Pharmacie p, String q) {
        return q.isEmpty() || matchesPharmacieAllFields(p, q);
    }

    private static boolean matchesPharmacieAllFields(Pharmacie p, String q) {
        return contains(String.valueOf(p.getId()), q)
                || contains(p.getNom(), q) || contains(p.getAdresse(), q)
                || contains(p.getTelephone(), q) || contains(p.getResponsable(), q)
                || contains(p.getHopital(), q);
    }

    private static Comparator<Pharmacie> pharmacyComparator(String sort) {
        Comparator<Pharmacie> byNom = Comparator.comparing(
                pr -> safeLower(pr.getNom()), Comparator.nullsLast(String::compareTo));
        if ("Nom (Z → A)".equals(sort)) {
            return byNom.reversed();
        }
        return byNom;
    }

    /* ---------- Médicaments ---------- */

    @FXML
    public void refreshMedicamentCards(ActionEvent event) {
        loadMedicamentCards();
    }

    @FXML
    public void addMedicament(ActionEvent event) {
        MedicalFormDialogs.showMedicamentDialog(window(), "Nouveau médicament", null)
                .ifPresent(m -> {
                    medicamentService.ajouter(m);
                    loadMedicamentCards();
                });
    }

    private void editMedicament(Medicament m) {
        MedicalFormDialogs.showMedicamentDialog(window(), "Modifier le médicament", m)
                .ifPresent(x -> {
                    medicamentService.modifier(x);
                    loadMedicamentCards();
                });
    }

    private void deleteMedicament(Medicament m) {
        if (!confirm("Supprimer", "Supprimer le médicament « " + m.getNomMedicament() + " » ?")) {
            return;
        }
        medicamentService.supprimer(m);
        loadMedicamentCards();
    }

    private void loadMedicamentCards() {
        if (medicamentCardsFlow == null) {
            return;
        }
        medicamentCardsFlow.getChildren().clear();
        String q = norm(searchMedicament.getText());
        String filter = medicamentFilterCombo != null && medicamentFilterCombo.getValue() != null
                ? medicamentFilterCombo.getValue() : "Toutes";
        String sort = medicamentSortCombo != null && medicamentSortCombo.getValue() != null
                ? medicamentSortCombo.getValue() : "Nom (A → Z)";

        List<Medicament> rows = new ArrayList<>();
        for (Medicament m : medicamentService.getAll()) {
            if (passesMedicamentFilters(m, q, filter)) {
                rows.add(m);
            }
        }
        rows.sort(medicamentComparator(sort));
        for (Medicament m : rows) {
            medicamentCardsFlow.getChildren().add(MedicalAdminCards.medicamentCard(m,
                    () -> editMedicament(m),
                    () -> deleteMedicament(m)));
        }
    }

    private boolean passesMedicamentFilters(Medicament m, String q, String filter) {
        if ("Stock < 10".equals(filter) && m.getStock() >= 10) {
            return false;
        }
        if (!q.isEmpty() && !matchesMedicamentAllFields(m, q)) {
            return false;
        }
        return true;
    }

    private static boolean matchesMedicamentAllFields(Medicament m, String q) {
        return contains(String.valueOf(m.getId()), q)
                || contains(m.getNomMedicament(), q) || contains(m.getCategorie(), q)
                || contains(m.getDosage(), q) || contains(m.getForme(), q)
                || contains(String.valueOf(m.getStock()), q)
                || (m.getDateExpiration() != null && contains(m.getDateExpiration().toString(), q));
    }

    private static Comparator<Medicament> medicamentComparator(String sort) {
        Comparator<Medicament> byNom = Comparator.comparing(
                m -> safeLower(m.getNomMedicament()), Comparator.nullsLast(String::compareTo));
        Comparator<Medicament> byExp = Comparator.comparing(Medicament::getDateExpiration,
                Comparator.nullsLast(Comparator.naturalOrder()));
        Comparator<Medicament> byStock = Comparator.comparingInt(Medicament::getStock);
        return switch (sort) {
            case "Nom (Z → A)" -> byNom.reversed();
            case "Expiration ↑" -> byExp;
            case "Expiration ↓" -> byExp.reversed();
            case "Stock ↑" -> byStock;
            case "Stock ↓" -> byStock.reversed();
            default -> byNom;
        };
    }

    /* ---------- Fiches ---------- */

    @FXML
    public void refreshFicheCards(ActionEvent event) {
        loadFicheCards();
    }

    @FXML
    public void addFiche(ActionEvent event) {
        List<User> patients = patientsForFicheForm(null);
        if (patients.isEmpty()) {
            alert(Alert.AlertType.INFORMATION, "Aucun compte patient. Créez des utilisateurs avec le rôle « patient » avant d'ajouter une fiche.");
            return;
        }
        MedicalFormDialogs.showFicheDialog(window(), "Nouvelle fiche médicale", null, patients)
                .ifPresent(f -> {
                    ficheService.ajouter(f);
                    loadFicheCards();
                    loadOrdonnanceCards();
                });
    }

    private void editFiche(Fiche f) {
        List<User> patients = patientsForFicheForm(f);
        MedicalFormDialogs.showFicheDialog(window(), "Modifier la fiche", f, patients)
                .ifPresent(x -> {
                    ficheService.modifier(x);
                    loadFicheCards();
                    loadOrdonnanceCards();
                });
    }

    private List<User> patientsForFicheForm(Fiche existing) {
        List<User> patients = new ArrayList<>(userService.getAll().stream()
                .filter(MedicalFormDialogs::isPatientUser)
                .toList());
        if (existing != null) {
            User linked = userService.getOneById(existing.getUserId());
            if (linked != null && patients.stream().noneMatch(u -> u.getId() == linked.getId())) {
                patients.add(linked);
            }
        }
        return patients;
    }

    private void deleteFiche(Fiche f) {
        if (!confirm("Supprimer", "Supprimer cette fiche médicale ? Les ordonnances liées peuvent être affectées.")) {
            return;
        }
        ficheService.supprimer(f);
        loadFicheCards();
        loadOrdonnanceCards();
    }

    private void loadFicheCards() {
        if (ficheCardsFlow == null) {
            return;
        }
        ficheCardsFlow.getChildren().clear();
        String q = norm(searchFiche.getText());
        String filter = ficheFilterCombo != null && ficheFilterCombo.getValue() != null
                ? ficheFilterCombo.getValue() : "Toutes gravités";
        String sort = ficheSortCombo != null && ficheSortCombo.getValue() != null
                ? ficheSortCombo.getValue() : "Date (↑ ancien)";

        List<Fiche> rows = new ArrayList<>();
        for (Fiche f : ficheService.getAll()) {
            if (passesFicheFilters(f, q, filter)) {
                rows.add(f);
            }
        }
        rows.sort(ficheComparator(sort));
        for (Fiche f : rows) {
            String patient = patientLabel(f.getUserId());
            ficheCardsFlow.getChildren().add(MedicalAdminCards.ficheCard(f, patient,
                    () -> editFiche(f),
                    () -> deleteFiche(f)));
        }
    }

    private String patientLabel(int userId) {
        User u = userService.getOneById(userId);
        if (u == null) {
            return "—";
        }
        return u.getPrenom() + " " + u.getNom();
    }

    private boolean passesFicheFilters(Fiche f, String q, String graviteFilter) {
        if (!MedicalInputValidation.graviteFilterMatches(f.getGravite(), graviteFilter)) {
            return false;
        }
        if (!q.isEmpty() && !matchesFicheAllFields(f, q)) {
            return false;
        }
        return true;
    }

    private boolean matchesFicheAllFields(Fiche f, String q) {
        if (contains(patientLabel(f.getUserId()), q)) {
            return true;
        }
        if (contains(f.getLibelleMaladie(), q) || contains(f.getAllergie(), q)
                || contains(f.getMaladieChronique(), q) || contains(f.getGravite(), q)
                || contains(f.getRecommandation(), q) || contains(f.getGrpSanguin(), q)
                || contains(f.getTension(), q)) {
            return true;
        }
        if (contains(String.valueOf(f.getPoids()), q) || contains(String.valueOf(f.getTaille()), q)
                || contains(String.valueOf(f.getGlycemie()), q)) {
            return true;
        }
        return (f.getDate() != null && contains(f.getDate().toString(), q))
                || contains(String.valueOf(f.getId()), q);
    }

    private Comparator<Fiche> ficheComparator(String sort) {
        Comparator<Fiche> byDate = Comparator.comparing(Fiche::getDate, Comparator.nullsLast(Comparator.naturalOrder()));
        Comparator<Fiche> byPatient = Comparator.comparing(
                f -> safeLower(patientLabel(f.getUserId())), Comparator.nullsLast(String::compareTo));
        if ("Date (↓ récent)".equals(sort)) {
            return byDate.reversed();
        }
        if ("Patient (A → Z)".equals(sort)) {
            return byPatient;
        }
        return byDate;
    }

    /* ---------- Ordonnances ---------- */

    @FXML
    public void refreshOrdonnanceCards(ActionEvent event) {
        loadOrdonnanceCards();
    }

    @FXML
    public void addOrdonnance(ActionEvent event) {
        List<Fiche> fiches = ficheService.getAll();
        if (fiches.isEmpty()) {
            alert(Alert.AlertType.INFORMATION, "Créez au moins une fiche médicale avant d'ajouter une ordonnance.");
            return;
        }
        List<Medicament> meds = medicamentService.getAll();
        if (meds.isEmpty()) {
            alert(Alert.AlertType.INFORMATION, "Ajoutez au moins un médicament avant de créer une ordonnance.");
            return;
        }
        MedicalFormDialogs.showOrdonnanceDialog(window(), "Nouvelle ordonnance", null, fiches, meds, List.of())
                .ifPresent(r -> {
                    ordonnanceService.ajouterAvecMedicaments(r.ordonnance(), r.medicamentIds());
                    loadOrdonnanceCards();
                });
    }

    private void editOrdonnance(Ordonnance o) {
        List<Fiche> fiches = ficheService.getAll();
        List<Medicament> meds = medicamentService.getAll();
        if (meds.isEmpty()) {
            alert(Alert.AlertType.INFORMATION, "Aucun médicament en base.");
            return;
        }
        List<Integer> initial = ordonnanceService.findMedicamentIdsByOrdonnance(o.getId());
        MedicalFormDialogs.showOrdonnanceDialog(window(), "Modifier l'ordonnance", o, fiches, meds, initial)
                .ifPresent(r -> {
                    Ordonnance x = r.ordonnance();
                    x.setId(o.getId());
                    ordonnanceService.modifierAvecMedicaments(x, r.medicamentIds());
                    loadOrdonnanceCards();
                });
    }

    private void deleteOrdonnance(Ordonnance o) {
        if (!confirm("Supprimer", "Supprimer cette ordonnance ?")) {
            return;
        }
        ordonnanceService.supprimer(o);
        loadOrdonnanceCards();
    }

    private void loadOrdonnanceCards() {
        if (ordonnanceCardsFlow == null) {
            return;
        }
        ordonnanceCardsFlow.getChildren().clear();
        String q = norm(searchOrdonnance.getText());
        String filter = ordonnanceFilterCombo != null && ordonnanceFilterCombo.getValue() != null
                ? ordonnanceFilterCombo.getValue() : "Toutes";
        String sort = ordonnanceSortCombo != null && ordonnanceSortCombo.getValue() != null
                ? ordonnanceSortCombo.getValue() : "Date (↑)";

        List<Ordonnance> rows = new ArrayList<>();
        for (Ordonnance o : ordonnanceService.getAll()) {
            if (passesOrdonnanceFilters(o, q, filter)) {
                rows.add(o);
            }
        }
        rows.sort(ordonnanceComparator(sort));
        for (Ordonnance o : rows) {
            Fiche linked = ficheService.getOneById(o.getFicheId());
            String ficheSummary = ordonnanceFicheSummary(linked);
            String medSummary = ordonnanceService.getMedicamentsSummaryForOrdonnance(o.getId());
            ordonnanceCardsFlow.getChildren().add(MedicalAdminCards.ordonnanceCard(o, ficheSummary, medSummary,
                    () -> editOrdonnance(o),
                    () -> deleteOrdonnance(o)));
        }
    }

    private static String ordonnanceFicheSummary(Fiche linked) {
        if (linked == null) {
            return "Fiche liée";
        }
        return (linked.getLibelleMaladie() != null ? linked.getLibelleMaladie() : "Fiche")
                + (linked.getDate() != null ? " · " + linked.getDate() : "");
    }

    private boolean passesOrdonnanceFilters(Ordonnance o, String q, String filter) {
        if ("Durée ≥ 30 jours".equals(filter) && o.getDureeTraitement() < 30) {
            return false;
        }
        if (!q.isEmpty() && !matchesOrdonnanceAllFields(o, q)) {
            return false;
        }
        return true;
    }



    private static String safeLower(String s) {
        return s == null ? "" : s.toLowerCase(Locale.ROOT);
    }

    /* ---------- Utilisateurs / créneaux / événements (existant) ---------- */

    private void loadUsers() {
        AffUsers.getItems().clear();
        AffUsers.getItems().addAll(userService.getAll());
    }

    private void loadCreneaux() {
        AffCreneaux.getItems().clear();
        AffCreneaux.getItems().addAll(creneauService.getAll());
    }

    private void loadEvents() {
        AffEvents.getItems().clear();
        AffEvents.getItems().addAll(eventService.getAll());
    }

    @FXML
    void refreshUsers(ActionEvent event) {
        loadUsers();
    }

    @FXML
    void deleteUser(ActionEvent event) {
        User selected = AffUsers.getSelectionModel().getSelectedItem();
        if (selected != null) {
            userService.supprimer(selected);
            loadUsers();
        } else {
            System.out.println("Aucun user sélectionne");
        }
    }

    @FXML
    void RechCreneau(ActionEvent event) {
    }

    @FXML
    void RechEvent(ActionEvent event) {
    }

    @FXML
    void RechUser(ActionEvent event) {
    }

    @FXML
    void addCreneau(ActionEvent event) {
    }

    @FXML
    void addEvent(ActionEvent event) {
    }

    @FXML
    public void deconnexion(ActionEvent event) {
        userService.logout();
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Login.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void deleteCreneau(ActionEvent event) {
        Creneau selected = AffCreneaux.getSelectionModel().getSelectedItem();
        if (selected != null) {
            creneauService.delete(selected.getId());
            loadCreneaux();
        } else {
            System.out.println("Aucun creneau sélectionne");
        }
    }

    @FXML
    void deleteEvent(ActionEvent event) {
        Event selected = AffEvents.getSelectionModel().getSelectedItem();
        if (selected != null) {
            eventService.supprimer(selected);
            loadEvents();
        } else {
            System.out.println("Aucun evenement sélectionne");
        }
    }

    @FXML
    void editCreneau(ActionEvent event) {
    }

    @FXML
    void editEvent(ActionEvent event) {
    }

    @FXML
    void editUser(ActionEvent event) {
        try {
            User selected = AffUsers.getSelectionModel().getSelectedItem();
            if (selected == null) {
                return;
            }
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierUser.fxml"));
            Parent root = loader.load();
            ModifierUser controller = loader.getController();
            controller.setUser(selected);
            Stage stage = (Stage) AffUsers.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void goToLieux(ActionEvent event) {
    }

    @FXML
    void goToRdv(ActionEvent event) {
    }

    @FXML
    void refreshCreneaux(ActionEvent event) {
        loadCreneaux();
    }

    @FXML
    void refreshEvent(ActionEvent event) {
        loadEvents();
    }

    /* ---------- helpers ---------- */

    private static String norm(String t) {
        return t == null ? "" : t.trim().toLowerCase(Locale.ROOT);
    }

    private static boolean contains(String field, String q) {
        return field != null && field.toLowerCase(Locale.ROOT).contains(q);
    }

    private boolean confirm(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        return a.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    private void alert(Alert.AlertType type, String msg) {
        Alert a = new Alert(type);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
