package tn.dhc.controllers;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import tn.dhc.entities.Fiche;
import tn.dhc.entities.Medicament;
import tn.dhc.entities.Ordonnance;
import tn.dhc.entities.Pharmacie;
import tn.dhc.entities.User;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BooleanSupplier;

/**
 * Modal forms for clinical / pharmacy CRUD (IDs are never shown; relations use pickers).
 */
public final class MedicalFormDialogs {

    public record OrdonnanceDialogResult(Ordonnance ordonnance, List<Integer> medicamentIds) {
    }

    private MedicalFormDialogs() {
    }

    /** Rôle patient (inscription / base). */
    public static boolean isPatientUser(User u) {
        if (u == null || u.getRole() == null) {
            return false;
        }
        return "patient".equalsIgnoreCase(u.getRole().trim());
    }

    private static String patientNomPrenom(User u) {
        if (u == null) {
            return "";
        }
        return (u.getPrenom() != null ? u.getPrenom().trim() : "") + " " + (u.getNom() != null ? u.getNom().trim() : "");
    }

   
    /**
     * @param preselectPatient si non null et {@code existing == null}, pré-sélectionne ce patient dans la liste.
     * @param medecinUserId    si non null et nouvelle fiche, enregistre l’id du médecin créateur.
     */
    public static Optional<Fiche> showFicheDialog(Window owner, String title, Fiche existing, List<User> patientUsers,
                                                   User preselectPatient) {
        return showFicheDialog(owner, title, existing, patientUsers, preselectPatient, null);
    }

    public static Optional<Fiche> showFicheDialog(Window owner, String title, Fiche existing, List<User> patientUsers,
                                                   User preselectPatient, Integer medecinUserId) {
        Dialog<Fiche> dialog = new Dialog<>();
        dialog.initOwner(owner);
        dialog.setTitle(title);
        style(dialog);

        ComboBox<User> userBox = new ComboBox<>();
        userBox.setItems(FXCollections.observableArrayList(patientUsers));
        userBox.setPrefWidth(360);
        userBox.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(User u, boolean empty) {
                super.updateItem(u, empty);
                setText(empty || u == null ? null : patientNomPrenom(u).trim());
            }
        });
        userBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(User u, boolean empty) {
                super.updateItem(u, empty);
                setText(empty || u == null ? null : patientNomPrenom(u).trim());
            }
        });

        TextField poids = new TextField();
        TextField taille = new TextField();
        ComboBox<String> grpSang = new ComboBox<>(FXCollections.observableArrayList(
                "", "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"));
        grpSang.setEditable(false);
        grpSang.setPrefWidth(360);
        TextField allergie = new TextField();
        TextField chronique = new TextField();
        TextField tension = new TextField();
        TextField glycemie = new TextField();
        DatePicker date = new DatePicker(LocalDate.now());
        TextField libelle = new TextField();
        ComboBox<String> graviteCombo = new ComboBox<>(FXCollections.observableArrayList("Faible", "Modérée", "Élevée"));
        graviteCombo.setPrefWidth(360);
        TextArea reco = new TextArea();
        reco.setPrefRowCount(3);
        reco.setWrapText(true);

        if (existing != null) {
            patientUsers.stream().filter(u -> u.getId() == existing.getUserId()).findFirst().ifPresent(userBox::setValue);
            poids.setText(String.valueOf(existing.getPoids()));
            taille.setText(String.valueOf(existing.getTaille()));
            String gs = existing.getGrpSanguin();
            if (gs != null && !gs.isBlank() && !grpSang.getItems().contains(gs)) {
                grpSang.getItems().add(gs);
            }
            grpSang.setValue(gs == null || gs.isBlank() ? "" : gs);
            allergie.setText(s(existing.getAllergie()));
            chronique.setText(s(existing.getMaladieChronique()));
            tension.setText(s(existing.getTension()));
            glycemie.setText(String.valueOf(existing.getGlycemie()));
            if (existing.getDate() != null) {
                date.setValue(existing.getDate().toLocalDate());
            }
            libelle.setText(s(existing.getLibelleMaladie()));
            String gCanon = MedicalInputValidation.canonicalGraviteLabel(existing.getGravite());
            if (!gCanon.isEmpty()) {
                graviteCombo.setValue(gCanon);
            }
            reco.setText(s(existing.getRecommandation()));
        } else if (preselectPatient != null) {
            patientUsers.stream().filter(u -> u.getId() == preselectPatient.getId()).findFirst().ifPresent(userBox::setValue);
        }

        GridPane grid = formGrid(
                new Row("Patient", userBox),
                new Row("Poids (kg)", poids),
                new Row("Taille (cm)", taille),
                new Row("Groupe sanguin", grpSang),
                new Row("Allergies", allergie),
                new Row("Maladie chronique", chronique),
                new Row("Tension", tension),
                new Row("Glycémie", glycemie),
                new Row("Date", date),
                new Row("Libellé maladie", libelle),
                new Row("Gravité", graviteCombo),
                new Row("Recommandations", reco)
        );
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setPrefWidth(560);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        MedicalFieldFeedback.wireComboClear(userBox);
        MedicalFieldFeedback.wireTextClear(poids);
        MedicalFieldFeedback.wireTextClear(taille);
        MedicalFieldFeedback.wireComboClear(grpSang);
        MedicalFieldFeedback.wireTextClear(allergie);
        MedicalFieldFeedback.wireTextClear(chronique);
        MedicalFieldFeedback.wireTextClear(tension);
        MedicalFieldFeedback.wireTextClear(glycemie);
        MedicalFieldFeedback.wireDateClear(date);
        MedicalFieldFeedback.wireTextClear(libelle);
        MedicalFieldFeedback.wireComboClear(graviteCombo);
        MedicalFieldFeedback.wireTextClear(reco);

        Map<String, Control> ficheControls = new HashMap<>();
        ficheControls.put("patient", userBox);
        ficheControls.put("poids", poids);
        ficheControls.put("taille", taille);
        ficheControls.put("glycemie", glycemie);
        ficheControls.put("libelle", libelle);
        ficheControls.put("gravite", graviteCombo);
        ficheControls.put("recommandation", reco);
        ficheControls.put("date", date);
        ficheControls.put("allergie", allergie);
        ficheControls.put("chronique", chronique);
        ficheControls.put("tension", tension);
        ficheControls.put("grpSanguin", grpSang);

        attachOkGuard(dialog, () -> {
            List<MedicalInputValidation.FieldIssue> issues = new ArrayList<>();
            if (userBox.getValue() == null) {
                issues.add(new MedicalInputValidation.FieldIssue("patient", "Sélectionnez un patient."));
            }
            if (date.getValue() == null) {
                issues.add(new MedicalInputValidation.FieldIssue("date", "Choisissez une date pour la fiche."));
            }
            String graviteVal = graviteCombo.getValue() != null ? graviteCombo.getValue() : "";
            String grpVal = grpSang.getValue() == null ? "" : grpSang.getValue();
            issues.addAll(MedicalInputValidation.collectFicheIssues(
                    poids.getText(), taille.getText(), glycemie.getText(),
                    libelle.getText(), graviteVal, reco.getText(),
                    allergie.getText(), chronique.getText(), tension.getText(),
                    grpVal,
                    date.getValue()));
            if (issues.isEmpty()) {
                return true;
            }
            applyFieldIssues(issues, ficheControls);
            alertFormIssues(issues);
            return false;
        });

        dialog.setResultConverter(btn -> {
            if (btn != ButtonType.OK) {
                return null;
            }
            double po = Double.parseDouble(poids.getText().trim().replace(',', '.'));
            double ta = Double.parseDouble(taille.getText().trim().replace(',', '.'));
            double gl = Double.parseDouble(glycemie.getText().trim().replace(',', '.'));
            Fiche f = existing != null ? existing : new Fiche();
            if (existing != null) {
                f.setId(existing.getId());
            }
            f.setUserId(userBox.getValue().getId());
            f.setPoids(po);
            f.setTaille(ta);
            String gv = grpSang.getValue();
            f.setGrpSanguin(gv == null || gv.isBlank() ? null : gv.trim());
            f.setAllergie(trimOrNull(allergie.getText()));
            f.setMaladieChronique(trimOrNull(chronique.getText()));
            f.setTension(trimOrNull(tension.getText()));
            f.setGlycemie(gl);
            f.setDate(Date.valueOf(date.getValue()));
            f.setLibelleMaladie(libelle.getText().trim());
            f.setGravite(MedicalInputValidation.canonicalGraviteLabel(graviteCombo.getValue()));
            f.setRecommandation(trimOrNull(reco.getText()));
            if (existing != null) {
                f.setMedecinUserId(existing.getMedecinUserId());
            } else if (medecinUserId != null) {
                f.setMedecinUserId(medecinUserId);
            } else {
                f.setMedecinUserId(null);
            }
            return f;
        });

        return dialog.showAndWait();
    }

    public static Optional<OrdonnanceDialogResult> showOrdonnanceDialog(Window owner, String title, Ordonnance existing,
                                                                        List<Fiche> fiches, List<Medicament> medicaments,
                                                                        List<Integer> initialMedicamentIds) {
        return showOrdonnanceDialog(owner, title, existing, fiches, medicaments, initialMedicamentIds, null);
    }

    public static Optional<OrdonnanceDialogResult> showOrdonnanceDialog(Window owner, String title, Ordonnance existing,
                                                                        List<Fiche> fiches, List<Medicament> medicaments,
                                                                        List<Integer> initialMedicamentIds,
                                                                        Integer medecinUserId) {
        Dialog<OrdonnanceDialogResult> dialog = new Dialog<>();
        dialog.initOwner(owner);
        dialog.setTitle(title);
        style(dialog);

        ComboBox<Fiche> ficheBox = new ComboBox<>();
        ficheBox.getItems().addAll(fiches);
        ficheBox.setPrefWidth(360);
        ficheBox.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Fiche f, boolean empty) {
                super.updateItem(f, empty);
                setText(empty || f == null ? null : formatFicheChoice(f));
            }
        });
        ficheBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Fiche f, boolean empty) {
                super.updateItem(f, empty);
                setText(empty || f == null ? null : formatFicheChoice(f));
            }
        });

        ListView<Medicament> medList = new ListView<>(FXCollections.observableArrayList(medicaments));
        medList.setPrefHeight(240);
        medList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        medList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Medicament m, boolean empty) {
                super.updateItem(m, empty);
                if (empty || m == null) {
                    setText(null);
                } else {
                    setText(m.getNomMedicament() + "  ·  stock " + m.getStock());
                }
            }
        });

        TextArea posologie = new TextArea();
        posologie.setPrefRowCount(3);
        posologie.setWrapText(true);
        TextField frequence = new TextField();
        TextField duree = new TextField();
        DatePicker date = new DatePicker(LocalDate.now());

        if (existing != null) {
            fiches.stream().filter(f -> f.getId() == existing.getFicheId()).findFirst().ifPresent(ficheBox::setValue);
            posologie.setText(s(existing.getPosologie()));
            frequence.setText(s(existing.getFrequence()));
            duree.setText(String.valueOf(existing.getDureeTraitement()));
            if (existing.getDate() != null) {
                date.setValue(existing.getDate().toLocalDate());
            }
        }

        List<Integer> initial = initialMedicamentIds != null ? initialMedicamentIds : List.of();
        for (Medicament m : medicaments) {
            if (initial.contains(m.getId())) {
                medList.getSelectionModel().select(m);
            }
        }

        Label medHint = new Label("Ctrl + clic pour sélectionner plusieurs médicaments.");
        medHint.setStyle("-fx-text-fill:#7f8c8d; -fx-font-size:12px;");
        VBox medBlock = new VBox(6, medHint, medList);

        GridPane grid = formGrid(
                new Row("Fiche médicale", ficheBox),
                new Row("Médicaments", medBlock),
                new Row("Posologie", posologie),
                new Row("Fréquence", frequence),
                new Row("Durée (jours)", duree),
                new Row("Date", date)
        );
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setPrefWidth(580);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        MedicalFieldFeedback.wireTextClear(posologie);
        MedicalFieldFeedback.wireTextClear(frequence);
        MedicalFieldFeedback.wireTextClear(duree);
        MedicalFieldFeedback.wireDateClear(date);
        MedicalFieldFeedback.wireComboClear(ficheBox);
        medList.getSelectionModel().selectedItemProperty().addListener((o, a, b) ->
                MedicalFieldFeedback.setError(medList, false));

        Map<String, Control> ordonnanceControls = new HashMap<>();
        ordonnanceControls.put("fiche", ficheBox);
        ordonnanceControls.put("medicaments", medList);
        ordonnanceControls.put("posologie", posologie);
        ordonnanceControls.put("frequence", frequence);
        ordonnanceControls.put("duree", duree);
        ordonnanceControls.put("date", date);

        attachOkGuard(dialog, () -> {
            List<MedicalInputValidation.FieldIssue> issues = new ArrayList<>();
            if (ficheBox.getValue() == null) {
                issues.add(new MedicalInputValidation.FieldIssue("fiche", "Sélectionnez une fiche médicale."));
            }
            if (medList.getSelectionModel().getSelectedItems().isEmpty()) {
                issues.add(new MedicalInputValidation.FieldIssue("medicaments", "Sélectionnez au moins un médicament."));
            }
            if (date.getValue() == null) {
                issues.add(new MedicalInputValidation.FieldIssue("date", "Choisissez une date pour l'ordonnance."));
            }
            issues.addAll(MedicalInputValidation.collectOrdonnanceIssues(
                    frequence.getText(), duree.getText(), posologie.getText(), date.getValue()));
            if (issues.isEmpty()) {
                return true;
            }
            applyFieldIssues(issues, ordonnanceControls);
            alertFormIssues(issues);
            return false;
        });

        dialog.setResultConverter(btn -> {
            if (btn != ButtonType.OK) {
                return null;
            }
            int j = Integer.parseInt(duree.getText().trim());
            Ordonnance o = existing != null ? existing : new Ordonnance();
            if (existing != null) {
                o.setId(existing.getId());
            }
            o.setFicheId(ficheBox.getValue().getId());
            o.setPosologie(posologie.getText().trim());
            o.setFrequence(trimOrNull(frequence.getText()));
            o.setDureeTraitement(j);
            o.setDate(Date.valueOf(date.getValue()));
            if (existing != null) {
                o.setMedecinUserId(existing.getMedecinUserId());
            } else if (medecinUserId != null) {
                o.setMedecinUserId(medecinUserId);
            } else {
                o.setMedecinUserId(null);
            }
            List<Integer> ids = new ArrayList<>();
            for (Medicament m : medList.getSelectionModel().getSelectedItems()) {
                ids.add(m.getId());
            }
            return new OrdonnanceDialogResult(o, ids);
        });

        return dialog.showAndWait();
    }

    private static void style(Dialog<?> dialog) {
        var url = MedicalFormDialogs.class.getResource("/theme.css");
        if (url != null) {
            dialog.getDialogPane().getStylesheets().add(url.toExternalForm());
        }
    }

    private static void applyFieldIssues(List<MedicalInputValidation.FieldIssue> issues, Map<String, Control> byId) {
        for (Control c : byId.values()) {
            MedicalFieldFeedback.setError(c, false);
        }
        for (MedicalInputValidation.FieldIssue fi : issues) {
            Control c = byId.get(fi.fieldId());
            if (c != null) {
                MedicalFieldFeedback.setError(c, true);
            }
        }
    }

    private static void alertFormIssues(List<MedicalInputValidation.FieldIssue> issues) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle("Formulaire incomplet ou incorrect");
        a.setHeaderText("Corrigez les champs surlignés en rouge :");
        a.setContentText(MedicalInputValidation.formatIssueMessages(issues));
        a.showAndWait();
    }

    private static void attachOkGuard(Dialog<?> dialog, BooleanSupplier validator) {
        Button ok = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        ok.addEventFilter(ActionEvent.ACTION, ev -> {
            if (!validator.getAsBoolean()) {
                ev.consume();
            }
        });
    }

    private record Row(String label, javafx.scene.Node field) {
    }

    private static GridPane formGrid(Row... rows) {
        GridPane g = new GridPane();
        g.setHgap(12);
        g.setVgap(10);
        g.setPadding(new Insets(16));
        ColumnConstraints c0 = new ColumnConstraints(140);
        ColumnConstraints c1 = new ColumnConstraints(200, 360, Double.MAX_VALUE);
        c1.setHgrow(Priority.ALWAYS);
        g.getColumnConstraints().addAll(c0, c1);
        int r = 0;
        for (Row row : rows) {
            Label lb = new Label(row.label);
            lb.setStyle("-fx-font-weight:600;");
            GridPane.setConstraints(lb, 0, r);
            GridPane.setConstraints(row.field, 1, r);
            g.getChildren().addAll(lb, row.field);
            r++;
        }
        return g;
    }

    private static String s(String v) {
        return v == null ? "" : v;
    }

    private static String trimOrNull(String t) {
        if (t == null) {
            return null;
        }
        String x = t.trim();
        return x.isEmpty() ? null : x;
    }

    private static String formatFicheChoice(Fiche f) {
        String d = f.getDate() != null ? f.getDate().toString() : "";
        return (f.getLibelleMaladie() != null ? f.getLibelleMaladie() : "Fiche") + " · " + d;
    }

    private static void alert(Alert.AlertType type, String msg) {
        Alert a = new Alert(type);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
