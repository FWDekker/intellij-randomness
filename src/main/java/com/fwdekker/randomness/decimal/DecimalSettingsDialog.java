package com.fwdekker.randomness.decimal;

import com.fwdekker.randomness.SettingsDialog;
import com.intellij.openapi.ui.ValidationInfo;
import java.text.ParseException;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * Dialog for settings of random decimal generation.
 */
final class DecimalSettingsDialog extends SettingsDialog {
    private static final double SPINNER_STEP_SIZE = 0.1;

    private final DecimalSettings decimalSettings;

    private JPanel contentPane;
    private JSpinner minValue;
    private JSpinner maxValue;
    private JSpinner decimalCount;


    /**
     * Constructs a new {@code DecimalSettingsDialog} that uses the singleton {@code DecimalSettings} instance.
     */
    DecimalSettingsDialog() {
        this(DecimalSettings.getInstance());
    }

    /**
     * Constructs a new {@code DecimalSettingsDialog} that uses the given {@code DecimalSettings} instance.
     *
     * @param decimalSettings the settings to manipulate with this dialog
     */
    DecimalSettingsDialog(@NotNull final DecimalSettings decimalSettings) {
        super();

        init();

        this.decimalSettings = decimalSettings;
        loadSettings();
    }


    @Override
    @NotNull
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
    protected void loadSettings() {
        minValue.setValue(decimalSettings.getMinValue());
        maxValue.setValue(decimalSettings.getMaxValue());
        decimalCount.setValue(decimalSettings.getDecimalCount());
    }

    @Override
    protected void saveSettings() {
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

    @Override
    @Nullable
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
