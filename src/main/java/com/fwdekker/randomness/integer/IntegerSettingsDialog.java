package com.fwdekker.randomness.integer;

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import java.text.ParseException;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSpinner;


/**
 * Dialog for settings of random integer generation.
 */
final class IntegerSettingsDialog extends DialogWrapper {
    private final IntegerSettings integerSettings = IntegerSettings.getInstance();

    private JPanel contentPane;
    private JSpinner minValue;
    private JSpinner maxValue;


    /**
     * Constructs a new {@code DecimalSettingsDialog}.
     */
    IntegerSettingsDialog() {
        super(null);

        init();
        loadSettings();
    }


    @Override
    protected JComponent createCenterPanel() {
        return contentPane;
    }

    @Override
    protected String getDimensionServiceKey() {
        return getClass().getSimpleName();
    }


    @Override
    protected void doOKAction() {
        processDoNotAskOnOk(OK_EXIT_CODE);

        if (getOKAction().isEnabled()) {
            saveSettings();
            close(OK_EXIT_CODE);
        }
    }


    /**
     * Loads settings from the model into the UI.
     */
    private void loadSettings() {
        minValue.setValue(integerSettings.getMinValue());
        maxValue.setValue(integerSettings.getMaxValue());
    }

    /**
     * Saves settings from the UI into the model.
     */
    private void saveSettings() {
        try {
            minValue.commitEdit();
            maxValue.commitEdit();
        } catch (final ParseException e) {
            throw new IllegalStateException("Settings were committed, but input could not be parsed.", e);
        }

        integerSettings.setMinValue((Integer) minValue.getValue());
        integerSettings.setMaxValue((Integer) maxValue.getValue());
    }

    /**
     * Validates all input fields.
     *
     * @return {@code null} if the input is valid, or {@code ValidationInfo} indicating the error if input is not valid
     */
    @Override
    protected ValidationInfo doValidate() {
        if (!(minValue.getValue() instanceof Integer)) {
            return new ValidationInfo("Minimum value must be an integer.", minValue);
        }
        if (!(maxValue.getValue() instanceof Integer)) {
            return new ValidationInfo("Maximum value must be an integer.", maxValue);
        }

        final int newMinValue = (Integer) minValue.getValue();
        final int newMaxValue = (Integer) maxValue.getValue();
        if (newMaxValue < newMinValue) {
            return new ValidationInfo("Maximum value cannot be smaller than minimum value.", maxValue);
        }

        return null;
    }
}
