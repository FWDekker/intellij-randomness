package com.fwdekker.randomness.decimal;

import com.fwdekker.randomness.SettingsDialog;
import com.fwdekker.randomness.common.JSpinnerHelper;
import com.fwdekker.randomness.common.ValidationException;
import com.fwdekker.randomness.common.Validator;
import com.intellij.openapi.ui.ValidationInfo;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * Dialog for settings of random decimal generation.
 */
final class DecimalSettingsDialog extends SettingsDialog<DecimalSettings> {
    private static final double MIN_MIN_VALUE = -1E53;
    private static final double MAX_MAX_VALUE = 1E53;
    private static final double MAX_VALUE_RANGE = 1E53;

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
        minValue = JSpinnerHelper.createSpinner();
        maxValue = JSpinnerHelper.createSpinner();
        decimalCount = JSpinnerHelper.createSpinner();
    }

    @Override
    @Nullable
    protected ValidationInfo doValidate() {
        try {
            Validator.isGreaterThanOrEqualTo(minValue, MIN_MIN_VALUE);
            Validator.isLessThanOrEqualTo(maxValue, MAX_MAX_VALUE);
            Validator.areValidRange(minValue, maxValue, MAX_VALUE_RANGE);

            Validator.isGreaterThan(decimalCount, 0);
            Validator.isLessThanOrEqualTo(decimalCount, Integer.MAX_VALUE);
            Validator.isInteger(decimalCount);
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
