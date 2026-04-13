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
            initAnnoncesAdmin();
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

    private boolean matchesOrdonnanceAllFields(Ordonnance o, String q) {
        if (contains(o.getPosologie(), q) || contains(o.getFrequence(), q)) {
            return true;
        }
        if (contains(String.valueOf(o.getDureeTraitement()), q)) {
            return true;
        }
        if (o.getDate() != null && contains(o.getDate().toString(), q)) {
            return true;
        }
        if (contains(String.valueOf(o.getId()), q) || contains(String.valueOf(o.getFicheId()), q)) {
            return true;
        }
        Fiche f = ficheService.getOneById(o.getFicheId());
        if (f != null && matchesFicheAllFields(f, q)) {
            return true;
        }
        String ficheSummary = ordonnanceFicheSummary(f);
        if (contains(ficheSummary, q)) {
            return true;
        }
        String medSummary = ordonnanceService.getMedicamentsSummaryForOrdonnance(o.getId());
        return contains(medSummary, q);
    }

    private static Comparator<Ordonnance> ordonnanceComparator(String sort) {
        Comparator<Ordonnance> byDate = Comparator.comparing(Ordonnance::getDate, Comparator.nullsLast(Comparator.naturalOrder()));
        Comparator<Ordonnance> byDuree = Comparator.comparingInt(Ordonnance::getDureeTraitement);
        return switch (sort) {
            case "Date (↓)" -> byDate.reversed();
            case "Durée (↑)" -> byDuree;
            case "Durée (↓)" -> byDuree.reversed();
            default -> byDate;
        };
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

    // ═══════════════════════════════════════════════════════════════════════════
    //  ANNONCES – ADMIN
    // ═══════════════════════════════════════════════════════════════════════════

    @FXML private javafx.scene.control.TextField annonceAdminSearch;
    @FXML private javafx.scene.control.ComboBox<String> annonceAdminSort;
    @FXML private javafx.scene.layout.VBox annonceAdminContainer;

    private final tn.dhc.services.ServiceAnnonce serviceAnnonce = new tn.dhc.services.ServiceAnnonce();

    /** Called by initialize() – wires search & sort listeners */
    private void initAnnoncesAdmin() {
        if (annonceAdminSort != null) {
            annonceAdminSort.setItems(javafx.collections.FXCollections.observableArrayList(
                "📅 Date (récente → ancienne)",
                "📅 Date (ancienne → récente)",
                "🔴 Urgence (haute → basse)",
                "🟢 Urgence (basse → haute)",
                "✅ État (disponible en premier)"
            ));
            annonceAdminSort.setOnAction(e -> filterAndLoadAdminAnnonces());
        }
        if (annonceAdminSearch != null) {
            annonceAdminSearch.textProperty().addListener((obs, o, n) -> filterAndLoadAdminAnnonces());
        }
        loadAdminAnnonces(null);
    }

    private void filterAndLoadAdminAnnonces() {
        String keyword = annonceAdminSearch != null ? annonceAdminSearch.getText().trim() : "";
        String sortLabel = annonceAdminSort != null ? annonceAdminSort.getValue() : null;
        String sortKey = labelToSortKey(sortLabel);
        try {
            java.util.List<tn.dhc.entities.Annonce> list;
            if (!keyword.isEmpty()) {
                list = serviceAnnonce.rechercher(keyword);
            } else if (sortKey != null) {
                list = serviceAnnonce.trier(sortKey);
            } else {
                list = serviceAnnonce.getAll();
            }
            renderAdminAnnonceCards(list);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private String labelToSortKey(String label) {
        if (label == null) return null;
        if (label.contains("récente"))   return "DATE_DESC";
        if (label.contains("ancienne"))  return "DATE_ASC";
        if (label.contains("haute"))     return "URGENCE_HIGH";
        if (label.contains("basse"))     return "URGENCE_LOW";
        if (label.contains("disponible")) return "DISPO";
        return null;
    }

    private void loadAdminAnnonces(java.util.List<tn.dhc.entities.Annonce> preloaded) {
        try {
            java.util.List<tn.dhc.entities.Annonce> list = preloaded != null ? preloaded : serviceAnnonce.getAll();
            renderAdminAnnonceCards(list);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void renderAdminAnnonceCards(java.util.List<tn.dhc.entities.Annonce> annonces) {
        if (annonceAdminContainer == null) return;
        annonceAdminContainer.getChildren().clear();
        if (annonces.isEmpty()) {
            javafx.scene.control.Label empty = new javafx.scene.control.Label("Aucune annonce trouvée.");
            empty.setStyle("-fx-text-fill:#95a5a6; -fx-font-size:14px; -fx-padding:30;");
            annonceAdminContainer.getChildren().add(empty);
            return;
        }
        for (tn.dhc.entities.Annonce a : annonces) {
            annonceAdminContainer.getChildren().add(buildAdminCard(a));
        }
    }

    private javafx.scene.layout.HBox buildAdminCard(tn.dhc.entities.Annonce a) {
        javafx.scene.layout.HBox card = new javafx.scene.layout.HBox(12);
        card.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color:white; -fx-background-radius:12; -fx-padding:14 18; " +
                      "-fx-border-radius:12; -fx-border-width:1.5; -fx-border-color:" + urgenceBorderColor(a.getUrgence()) + "; " +
                      "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.07), 10, 0, 0, 3);");

        // urgence dot
        javafx.scene.control.Label dot = new javafx.scene.control.Label(urgenceEmoji(a.getUrgence()));
        dot.setStyle("-fx-font-size:18px;");

        // title + meta
        javafx.scene.layout.VBox info = new javafx.scene.layout.VBox(3);
        javafx.scene.control.Label titre = new javafx.scene.control.Label(a.getTitre_annonce());
        titre.setStyle("-fx-font-size:15px; -fx-font-weight:bold; -fx-text-fill:#2c3e50;");
        javafx.scene.control.Label meta = new javafx.scene.control.Label(
            "📅 " + a.getDate_publication() + "  |  " + etatLabel(a.getEtat_annonce()));
        meta.setStyle("-fx-font-size:12px; -fx-text-fill:#7f8c8d;");
        info.getChildren().addAll(titre, meta);
        javafx.scene.layout.HBox.setHgrow(info, javafx.scene.layout.Priority.ALWAYS);

        // buttons
        javafx.scene.control.Button btnVoir = styledBtn("👁 Voir", "#00b3a6", "white");
        javafx.scene.control.Button btnEdit = styledBtn("✏️ Modifier", "#3498db", "white");
        javafx.scene.control.Button btnDel  = styledBtn("🗑️ Supprimer", "#e74c3c", "white");

        btnVoir.setOnAction(e -> openAnnonceViewPopup(a, true));
        btnEdit.setOnAction(e -> openAnnonceFormPopup(a));
        btnDel.setOnAction(e -> {
            if (confirm("Supprimer", "Supprimer l'annonce « " + a.getTitre_annonce() + " » ?")) {
                try {
                    serviceAnnonce.supprimer(a.getIdAnnonce());
                    filterAndLoadAdminAnnonces();
                } catch (Exception ex) {
                    alert(javafx.scene.control.Alert.AlertType.ERROR, "Erreur : " + ex.getMessage());
                }
            }
        });

        card.getChildren().addAll(dot, info, btnVoir, btnEdit, btnDel);
        return card;
    }

    @FXML
    public void addAnnonce() {
        openAnnonceFormPopup(null);
    }

    /** Opens a Stage with a form to add or edit an annonce. */
    private void openAnnonceFormPopup(tn.dhc.entities.Annonce existing) {
        boolean isEdit = existing != null;
        javafx.stage.Stage popup = new javafx.stage.Stage();
        popup.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        popup.setTitle(isEdit ? "✏️ Modifier l'annonce" : "➕ Nouvelle annonce");

        javafx.scene.layout.VBox root = new javafx.scene.layout.VBox(16);
        root.setStyle("-fx-background-color:white; -fx-padding:28; -fx-background-radius:14;");
        root.setPrefWidth(520);

        // Title header
        javafx.scene.control.Label header = new javafx.scene.control.Label(isEdit ? "✏️ Modifier l'annonce" : "➕ Nouvelle annonce");
        header.setStyle("-fx-font-size:20px; -fx-font-weight:bold; -fx-text-fill:#00b3a6;");

        // Fields
        javafx.scene.control.TextField tfTitre = new javafx.scene.control.TextField(isEdit ? existing.getTitre_annonce() : "");
        tfTitre.setPromptText("Titre de l'annonce *");
        tfTitre.setStyle("-fx-pref-height:40; -fx-background-radius:8; -fx-border-radius:8; -fx-border-color:#bdc3c7; -fx-border-width:1;");

        javafx.scene.control.TextArea taDesc = new javafx.scene.control.TextArea(isEdit ? existing.getDescription() : "");
        taDesc.setPromptText("Description *");
        taDesc.setPrefRowCount(4);
        taDesc.setStyle("-fx-background-radius:8; -fx-border-radius:8; -fx-border-color:#bdc3c7; -fx-border-width:1;");
        taDesc.setWrapText(true);

        javafx.scene.control.DatePicker dpDate = new javafx.scene.control.DatePicker(isEdit ? existing.getDate_publication() : java.time.LocalDate.now());

        javafx.scene.control.ComboBox<String> cbUrgence = new javafx.scene.control.ComboBox<>();
        cbUrgence.getItems().addAll("VERT", "ORANGE", "ROUGE");
        cbUrgence.setPromptText("Urgence *");
        cbUrgence.setPrefWidth(230);
        if (isEdit) cbUrgence.setValue(existing.getUrgence());

        javafx.scene.control.ComboBox<String> cbEtat = new javafx.scene.control.ComboBox<>();
        cbEtat.getItems().addAll("DISPONIBLE", "NON_DISPONIBLE");
        cbEtat.setPromptText("État *");
        cbEtat.setPrefWidth(230);
        if (isEdit) cbEtat.setValue(existing.getEtat_annonce());

        javafx.scene.layout.HBox rowCombos = new javafx.scene.layout.HBox(12, cbUrgence, cbEtat);

        // error label
        javafx.scene.control.Label errLabel = new javafx.scene.control.Label();
        errLabel.setStyle("-fx-text-fill:#e74c3c; -fx-font-size:12px;");
        errLabel.setWrapText(true);

        // Buttons
        javafx.scene.control.Button btnSave = styledBtn(isEdit ? "💾 Enregistrer" : "✅ Ajouter", "#00b3a6", "white");
        javafx.scene.control.Button btnCancel = styledBtn("❌ Annuler", "#95a5a6", "white");
        javafx.scene.layout.HBox btnRow = new javafx.scene.layout.HBox(12, btnSave, btnCancel);
        btnRow.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);

        btnCancel.setOnAction(e -> popup.close());
        btnSave.setOnAction(e -> {
            // ── Validation ────────────────────────────────────────────
            StringBuilder err = new StringBuilder();
            String titre = tfTitre.getText().trim();
            String desc  = taDesc.getText().trim();

            if (titre.isEmpty())           err.append("• Le titre est obligatoire.\n");
            else if (titre.length() > 150) err.append("• Le titre ne doit pas dépasser 150 caractères.\n");
            if (desc.isEmpty())            err.append("• La description est obligatoire.\n");
            if (dpDate.getValue() == null) err.append("• La date de publication est obligatoire.\n");
            if (cbUrgence.getValue() == null) err.append("• Veuillez choisir un niveau d'urgence.\n");
            if (cbEtat.getValue() == null)    err.append("• Veuillez choisir un état.\n");

            if (err.length() > 0) {
                errLabel.setText(err.toString());
                return;
            }
            // ── Persist ───────────────────────────────────────────────
            try {
                tn.dhc.entities.User admin = tn.dhc.services.UserService.getCurrentUser();
                int idU = admin != null ? admin.getId() : 1;
                if (isEdit) {
                    existing.setTitre_annonce(titre);
                    existing.setDescription(desc);
                    existing.setDate_publication(dpDate.getValue());
                    existing.setUrgence(cbUrgence.getValue());
                    existing.setEtat_annonce(cbEtat.getValue());
                    serviceAnnonce.modifier(existing);
                } else {
                    tn.dhc.entities.Annonce nouv = new tn.dhc.entities.Annonce(
                        titre, desc, dpDate.getValue(),
                        cbUrgence.getValue(), cbEtat.getValue(), idU);
                    serviceAnnonce.ajouter(nouv);
                }
                popup.close();
                filterAndLoadAdminAnnonces();
            } catch (Exception ex) {
                errLabel.setText("Erreur base de données : " + ex.getMessage());
            }
        });

        root.getChildren().addAll(header,
            labelFor("Titre"), tfTitre,
            labelFor("Description"), taDesc,
            labelFor("Date de publication"), dpDate,
            labelFor("Urgence & État"), rowCombos,
            errLabel, btnRow);

        javafx.scene.Scene scene = new javafx.scene.Scene(root);
        popup.setScene(scene);
        popup.sizeToScene();
        popup.showAndWait();
    }

    /** View popup – shows annonce details + comments list (admin can delete each). */
    private void openAnnonceViewPopup(tn.dhc.entities.Annonce a, boolean adminMode) {
        javafx.stage.Stage popup = new javafx.stage.Stage();
        popup.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        popup.setTitle("📢 " + a.getTitre_annonce());

        javafx.scene.layout.VBox root = new javafx.scene.layout.VBox(14);
        root.setStyle("-fx-background-color:white; -fx-padding:28;");
        root.setPrefWidth(620);

        // ── urgence badge ──────────────────────────────────────────────────
        javafx.scene.control.Label urgBadge = new javafx.scene.control.Label(
            urgenceEmoji(a.getUrgence()) + "  " + a.getUrgence());
        urgBadge.setStyle("-fx-font-size:13px; -fx-font-weight:bold; -fx-text-fill:white; -fx-background-color:" +
                urgenceBgColor(a.getUrgence()) + "; -fx-background-radius:20; -fx-padding:4 14;");

        javafx.scene.control.Label titre = new javafx.scene.control.Label(a.getTitre_annonce());
        titre.setStyle("-fx-font-size:22px; -fx-font-weight:bold; -fx-text-fill:#2c3e50;");
        titre.setWrapText(true);

        javafx.scene.control.Label dateLbl = new javafx.scene.control.Label(
            "📅 Publié le : " + a.getDate_publication() + "   " + etatLabel(a.getEtat_annonce()));
        dateLbl.setStyle("-fx-text-fill:#7f8c8d; -fx-font-size:13px;");

        javafx.scene.control.Label descTitle = new javafx.scene.control.Label("Description");
        descTitle.setStyle("-fx-font-size:14px; -fx-font-weight:bold; -fx-text-fill:#00b3a6;");

        javafx.scene.control.TextArea descArea = new javafx.scene.control.TextArea(a.getDescription());
        descArea.setEditable(false);
        descArea.setWrapText(true);
        descArea.setPrefRowCount(4);
        descArea.setStyle("-fx-background-color:#f8fffe; -fx-border-color:#e0f2f0; -fx-background-radius:8; -fx-border-radius:8;");

        // ── Comments section (admin only) ──────────────────────────────────
        javafx.scene.layout.VBox commentsSection = buildAdminCommentsSection(a);

        javafx.scene.control.Button btnClose = styledBtn("✖ Fermer", "#95a5a6", "white");
        btnClose.setOnAction(e -> popup.close());

        root.getChildren().addAll(urgBadge, titre, dateLbl, descTitle, descArea, commentsSection, btnClose);

        javafx.scene.control.ScrollPane sp = new javafx.scene.control.ScrollPane(root);
        sp.setFitToWidth(true);
        sp.setHbarPolicy(javafx.scene.control.ScrollPane.ScrollBarPolicy.NEVER);
        javafx.scene.Scene scene = new javafx.scene.Scene(sp, 640, 620);
        popup.setScene(scene);
        popup.showAndWait();
    }

    /** Builds the comments panel shown inside admin view popup */
    private javafx.scene.layout.VBox buildAdminCommentsSection(tn.dhc.entities.Annonce a) {
        tn.dhc.services.ServiceCommentaire svcCom = new tn.dhc.services.ServiceCommentaire();

        javafx.scene.layout.VBox section = new javafx.scene.layout.VBox(10);
        section.setStyle("-fx-background-color:#f4f7f6; -fx-background-radius:10; " +
                         "-fx-border-color:#e0f2f0; -fx-border-radius:10; -fx-border-width:1; -fx-padding:14;");

        javafx.scene.control.Label secTitle = new javafx.scene.control.Label("💬 Commentaires des patients");
        secTitle.setStyle("-fx-font-size:15px; -fx-font-weight:bold; -fx-text-fill:#00b3a6;");
        section.getChildren().add(secTitle);

        // Container that can be refreshed
        javafx.scene.layout.VBox listBox = new javafx.scene.layout.VBox(8);
        refreshCommentList(listBox, a, svcCom);
        section.getChildren().add(listBox);

        return section;
    }

    /** Loads/refreshes the comment rows inside the listBox */
    private void refreshCommentList(javafx.scene.layout.VBox listBox,
                                    tn.dhc.entities.Annonce a,
                                    tn.dhc.services.ServiceCommentaire svcCom) {
        listBox.getChildren().clear();
        try {
            java.util.List<tn.dhc.entities.Commentaire> comments = svcCom.getByAnnonce(a.getIdAnnonce());
            if (comments.isEmpty()) {
                javafx.scene.control.Label none = new javafx.scene.control.Label("Aucun commentaire pour cette annonce.");
                none.setStyle("-fx-text-fill:#95a5a6; -fx-font-size:13px; -fx-padding:8 0;");
                listBox.getChildren().add(none);
                return;
            }
            for (tn.dhc.entities.Commentaire c : comments) {
                listBox.getChildren().add(buildCommentRow(c, a, listBox, svcCom));
            }
        } catch (Exception ex) {
            javafx.scene.control.Label err = new javafx.scene.control.Label("Erreur chargement : " + ex.getMessage());
            err.setStyle("-fx-text-fill:#e74c3c; -fx-font-size:12px;");
            listBox.getChildren().add(err);
        }
    }

    /** Builds one comment row: user info + text + delete button */
    private javafx.scene.layout.HBox buildCommentRow(tn.dhc.entities.Commentaire c,
                                                      tn.dhc.entities.Annonce a,
                                                      javafx.scene.layout.VBox listBox,
                                                      tn.dhc.services.ServiceCommentaire svcCom) {
        javafx.scene.layout.HBox row = new javafx.scene.layout.HBox(10);
        row.setAlignment(javafx.geometry.Pos.TOP_LEFT);
        row.setStyle("-fx-background-color:white; -fx-background-radius:8; -fx-padding:10 14; " +
                     "-fx-border-color:#e8f4f3; -fx-border-radius:8; -fx-border-width:1;");

        // Avatar circle
        javafx.scene.control.Label avatar = new javafx.scene.control.Label("👤");
        avatar.setStyle("-fx-font-size:20px; -fx-padding:4 0 0 0;");

        // User id + date + body
        javafx.scene.layout.VBox body = new javafx.scene.layout.VBox(4);
        javafx.scene.layout.HBox.setHgrow(body, javafx.scene.layout.Priority.ALWAYS);

        String userId = c.getHeaders() != null ? c.getHeaders().replace("user_", "Patient #") : "Patient";
        String dateStr = c.getCreated_at() != null
            ? c.getCreated_at().toLocalDate() + " à " +
              String.format("%02d:%02d", c.getCreated_at().getHour(), c.getCreated_at().getMinute())
            : "";

        javafx.scene.control.Label meta = new javafx.scene.control.Label(userId + "   🕐 " + dateStr);
        meta.setStyle("-fx-font-size:11px; -fx-text-fill:#7f8c8d; -fx-font-weight:600;");

        javafx.scene.control.Label textLbl = new javafx.scene.control.Label(c.getBody());
        textLbl.setWrapText(true);
        textLbl.setStyle("-fx-font-size:13px; -fx-text-fill:#2c3e50;");
        textLbl.setMaxWidth(400);

        body.getChildren().addAll(meta, textLbl);

        // Delete button
        javafx.scene.control.Button btnDel = styledBtn("🗑️", "#e74c3c", "white");
        btnDel.setStyle("-fx-background-color:#e74c3c; -fx-text-fill:white; " +
                        "-fx-background-radius:6; -fx-padding:5 10; -fx-cursor:hand; -fx-font-size:13px;");
        btnDel.setTooltip(new javafx.scene.control.Tooltip("Supprimer ce commentaire"));
        btnDel.setOnAction(e -> {
            Alert conf = new Alert(Alert.AlertType.CONFIRMATION);
            conf.setTitle("Supprimer commentaire");
            conf.setHeaderText(null);
            conf.setContentText("Supprimer ce commentaire ?");
            if (conf.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                try {
                    svcCom.supprimer(c.getId());
                    refreshCommentList(listBox, a, svcCom);
                } catch (Exception ex) {
                    alert(Alert.AlertType.ERROR, "Erreur : " + ex.getMessage());
                }
            }
        });

        row.getChildren().addAll(avatar, body, btnDel);
        return row;
    }

    // ── Small helpers ──────────────────────────────────────────────────────────

    private static javafx.scene.control.Button styledBtn(String text, String bg, String fg) {
        javafx.scene.control.Button b = new javafx.scene.control.Button(text);
        b.setStyle("-fx-background-color:" + bg + "; -fx-text-fill:" + fg + "; " +
                   "-fx-background-radius:8; -fx-padding:7 14; -fx-font-weight:bold; -fx-cursor:hand;");
        b.setOnMouseEntered(e -> b.setOpacity(0.85));
        b.setOnMouseExited(e -> b.setOpacity(1.0));
        return b;
    }

    private static javafx.scene.control.Label labelFor(String text) {
        javafx.scene.control.Label l = new javafx.scene.control.Label(text);
        l.setStyle("-fx-font-size:13px; -fx-font-weight:600; -fx-text-fill:#555;");
        return l;
    }

    private static String urgenceEmoji(String u) {
        if (u == null) return "⚪";
        switch (u.toUpperCase()) {
            case "ROUGE":  return "🔴";
            case "ORANGE": return "🟠";
            case "VERT":   return "🟢";
            default:       return "⚪";
        }
    }

    private static String urgenceBorderColor(String u) {
        if (u == null) return "#e0e0e0";
        switch (u.toUpperCase()) {
            case "ROUGE":  return "#e74c3c";
            case "ORANGE": return "#e67e22";
            case "VERT":   return "#27ae60";
            default:       return "#bdc3c7";
        }
    }

    private static String urgenceBgColor(String u) {
        if (u == null) return "#95a5a6";
        switch (u.toUpperCase()) {
            case "ROUGE":  return "#e74c3c";
            case "ORANGE": return "#e67e22";
            case "VERT":   return "#27ae60";
            default:       return "#95a5a6";
        }
    }

    private static String etatLabel(String e) {
        if ("DISPONIBLE".equalsIgnoreCase(e)) return "✅ Disponible";
        if ("NON_DISPONIBLE".equalsIgnoreCase(e)) return "❌ Non disponible";
        return e != null ? e : "";
    }
}
