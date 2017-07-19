package com.fwdekker.randomness.decimal;

import com.fwdekker.randomness.SettingsDialog;
import com.fwdekker.randomness.common.ValidationException;
import com.fwdekker.randomness.common.Validator;
import com.intellij.openapi.ui.ValidationInfo;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * Dialog for settings of random decimal generation.
 */
final class DecimalSettingsDialog extends SettingsDialog<DecimalSettings> {
    private static final double SPINNER_STEP_SIZE = 0.1;

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
     * @param settings the settings to manipulate with this dialog
     */
    DecimalSettingsDialog(@NotNull final DecimalSettings settings) {
        super(settings);

        init();
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
        decimalCount = new JSpinner(new SpinnerNumberModel(0L, Long.MIN_VALUE, Long.MAX_VALUE, 1L));
    }

    @Override
    @Nullable
    protected ValidationInfo doValidate() {
        try {
            Validator.hasValidFormat(minValue,
                    "Minimum value must be a number.");
            Validator.hasValidFormat(maxValue,
                    "Maximum value must be a number.");

            Validator.areValidRange(minValue, maxValue,
                    "Maximum value cannot be smaller than minimum value.");

            Validator.hasValidFormat(decimalCount,
                    "Decimal count must be a number.");
            Validator.isGreaterThan(decimalCount, 0,
                    "Decimal count must not be a negative number.");
            Validator.isLessThan(decimalCount, Integer.MAX_VALUE,
                    "Decimal count must not be greater than 2^31-1.");
            Validator.isInteger(decimalCount,
                    "Decimal count must be a whole number.");
        } catch (final ValidationException e) {
            return new ValidationInfo(e.getMessage(), e.getComponent());
        }

        return null;
    }


    @Override
    public void loadSettings(@NotNull final DecimalSettings settings) {
        minValue.setValue(settings.getMinValue());
        maxValue.setValue(settings.getMaxValue());
        decimalCount.setValue(settings.getDecimalCount());
    }

    @Override
    public void saveSettings(@NotNull final DecimalSettings settings) {
        final double newMinValue = ((Number) minValue.getValue()).doubleValue();
        final double newMaxValue = ((Number) maxValue.getValue()).doubleValue();
        final int newDecimalCount = ((Number) decimalCount.getValue()).intValue();

        settings.setMinValue(newMinValue);
        settings.setMaxValue(newMaxValue);
        settings.setDecimalCount(newDecimalCount);
    }
}
