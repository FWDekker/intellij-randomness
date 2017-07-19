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
            minValue.commitEdit();
        } catch (final ParseException e) {
            return new ValidationInfo("Minimum value must be a number.", minValue);
        }
        try {
            maxValue.commitEdit();
        } catch (final ParseException e) {
            return new ValidationInfo("Maximum value must be a number.", maxValue);
        }
        try {
            decimalCount.commitEdit();
        } catch (final ParseException e) {
            return new ValidationInfo("Decimal count must be a number.", decimalCount);
        }

        final double newMinValue = ((Number) minValue.getValue()).doubleValue();
        final double newMaxValue = ((Number) maxValue.getValue()).doubleValue();
        if (newMaxValue < newMinValue) {
            return new ValidationInfo("Maximum value cannot be smaller than minimum value.", maxValue);
        }

        final double newDecimalCount = ((Number) decimalCount.getValue()).doubleValue();
        if (newDecimalCount < 0) {
            return new ValidationInfo("Decimal count must not be a negative number.", decimalCount);
        }
        if (newDecimalCount > Integer.MAX_VALUE) {
            return new ValidationInfo("Decimal count must not be greater than 2^31-1.", decimalCount);
        }
        if (newDecimalCount % 1 != 0) {
            return new ValidationInfo("Decimal count must be a whole number.", decimalCount);
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
