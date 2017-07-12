package com.fwdekker.randomness.decimal;

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import java.text.ParseException;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;


/**
 * Dialog for settings of random decimal generation.
 */
final class DecimalSettingsDialog extends DialogWrapper {
    private static final double SPINNER_STEP_SIZE = 0.1;

    private final DecimalSettings decimalSettings = DecimalSettings.getInstance();

    private JPanel contentPane;
    private JSpinner minValue;
    private JSpinner maxValue;
    private JSpinner decimalCount;


    /**
     * Constructs a new {@code DecimalSettingsDialog}.
     */
    DecimalSettingsDialog() {
        super(null);

        init();
        loadSettings();
    }


    /**
     * Returns the center panel containing all input fields.
     *
     * @return the center panel containing all input fields
     */
    @Override
    protected JComponent createCenterPanel() {
        return contentPane;
    }

    /**
     * Initialises custom UI components.
     * <p>
     * This method is called by the scene builder at the start of the constructor.
     */
    @SuppressWarnings("PMD.UnusedPrivateMethod") // Method used by scene builder
    private void createUIComponents() {
        minValue = new JSpinner(
                new SpinnerNumberModel(0.0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, SPINNER_STEP_SIZE));
        maxValue = new JSpinner(
                new SpinnerNumberModel(0.0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, SPINNER_STEP_SIZE));
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
        minValue.setValue(decimalSettings.getMinValue());
        maxValue.setValue(decimalSettings.getMaxValue());
        decimalCount.setValue(decimalSettings.getDecimalCount());
    }

    /**
     * Saves settings from the UI into the model.
     */
    private void saveSettings() {
        try {
            minValue.commitEdit();
            maxValue.commitEdit();
            decimalCount.commitEdit();
        } catch (final ParseException e) {
            throw new IllegalStateException("Settings were committed, but input could not be parsed.", e);
        }

        decimalSettings.setMinValue((Double) minValue.getValue());
        decimalSettings.setMaxValue((Double) maxValue.getValue());
        decimalSettings.setDecimalCount((Integer) decimalCount.getValue());
    }

    /**
     * Validates all input fields.
     *
     * @return {@code null} if the input is valid, or {@code ValidationInfo} indicating the error if input is not valid
     */
    @Override
    protected ValidationInfo doValidate() {
        if (!(minValue.getValue() instanceof Double)) {
            return new ValidationInfo("Minimum value must be a decimal.", minValue);
        }
        if (!(maxValue.getValue() instanceof Double)) {
            return new ValidationInfo("Maximum value must be a decimal.", maxValue);
        }
        if (!(decimalCount.getValue() instanceof Integer)) {
            return new ValidationInfo("Decimal count must be an integer.", decimalCount);
        }

        final double newMinValue = (Double) minValue.getValue();
        final double newMaxValue = (Double) maxValue.getValue();
        if (newMaxValue < newMinValue) {
            return new ValidationInfo("Maximum value cannot be smaller than minimum value.", maxValue);
        }

        final int newDecimalCount = (Integer) decimalCount.getValue();
        if (newDecimalCount < 0) {
            return new ValidationInfo("Decimal count must be at least 0.", decimalCount);
        }

        return null;
    }
}
