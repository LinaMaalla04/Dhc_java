package tn.dhc.controllers;

import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextInputControl;

/**
 * Mise en évidence des champs invalides (bordure rouge) dans les formulaires médicaux.
 */
public final class MedicalFieldFeedback {

    static final String ERROR_STYLE_CLASS = "medical-input-error";

    private MedicalFieldFeedback() {
    }

    public static void setError(Control c, boolean error) {
        if (c == null) {
            return;
        }
        if (error) {
            if (!c.getStyleClass().contains(ERROR_STYLE_CLASS)) {
                c.getStyleClass().add(ERROR_STYLE_CLASS);
            }
        } else {
            c.getStyleClass().removeAll(ERROR_STYLE_CLASS);
        }
    }

    public static void clearErrors(Control... controls) {
        for (Control c : controls) {
            setError(c, false);
        }
    }

    public static void wireTextClear(TextInputControl field) {
        field.textProperty().addListener((o, a, b) -> setError((Control) field, false));
    }

    public static void wireComboClear(ComboBox<?> combo) {
        combo.valueProperty().addListener((o, a, b) -> setError(combo, false));
    }

    public static void wireDateClear(DatePicker picker) {
        picker.valueProperty().addListener((o, a, b) -> setError(picker, false));
    }

    public static void wireSpinnerClear(Spinner<?> spinner) {
        spinner.valueProperty().addListener((o, a, b) -> setError(spinner, false));
    }
}
