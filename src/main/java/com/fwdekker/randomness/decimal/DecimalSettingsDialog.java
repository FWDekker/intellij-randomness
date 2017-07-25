package com.fwdekker.randomness.decimal;

import com.fwdekker.randomness.SettingsDialog;
import com.fwdekker.randomness.common.ValidationException;
import com.fwdekker.randomness.ui.JDoubleSpinner;
import com.fwdekker.randomness.ui.JLongSpinner;
import com.fwdekker.randomness.ui.JSpinnerRange;
import com.intellij.openapi.ui.ValidationInfo;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * Dialog for settings of random decimal generation.
 */
final class DecimalSettingsDialog extends SettingsDialog<DecimalSettings> {
    private JPanel contentPane;
    private JSpinnerRange valueRange;
    private JDoubleSpinner minValue;
    private JDoubleSpinner maxValue;
    private JLongSpinner decimalCount;


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
        minValue = new JDoubleSpinner();
        maxValue = new JDoubleSpinner();
        valueRange = new JSpinnerRange(minValue, maxValue);
        decimalCount = new JLongSpinner(0, Integer.MAX_VALUE);
    }

    @Override
    @Nullable
    protected ValidationInfo doValidate() {
        try {
            minValue.validateValue();
            maxValue.validateValue();
            valueRange.validate();

            decimalCount.validateValue();
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
        settings.setMinValue(minValue.getValue());
        settings.setMaxValue(maxValue.getValue());
        settings.setDecimalCount(Math.toIntExact(decimalCount.getValue()));
    }
}
