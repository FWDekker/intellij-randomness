package com.fwdekker.randomness.decimal;

import com.fwdekker.randomness.JavaHelperKt;
import com.fwdekker.randomness.SettingsDialog;
import com.fwdekker.randomness.ui.ButtonGroupKt;
import com.fwdekker.randomness.ui.JDoubleSpinner;
import com.fwdekker.randomness.ui.JIntSpinner;
import com.fwdekker.randomness.ui.JSpinnerRange;
import com.intellij.openapi.ui.ValidationInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JPanel;


/**
 * Dialog for settings of random decimal generation.
 *
 * @see DecimalSettings
 * @see DecimalSettingsAction
 */
public final class DecimalSettingsDialog extends SettingsDialog<DecimalSettings> {
    private JPanel contentPane;
    private JSpinnerRange valueRange;
    private JDoubleSpinner minValue;
    private JDoubleSpinner maxValue;
    private JIntSpinner decimalCount;
    private ButtonGroup groupingSeparatorGroup;
    private ButtonGroup decimalSeparatorGroup;


    /**
     * Constructs a new {@code DecimalSettingsDialog} that uses the singleton {@code DecimalSettings} instance.
     */
    /* default */ DecimalSettingsDialog() {
        this(DecimalSettings.Companion.getDefault());
    }

    /**
     * Constructs a new {@code DecimalSettingsDialog} that uses the given {@code DecimalSettings} instance.
     *
     * @param settings the settings to manipulate with this dialog
     */
    /* default */ DecimalSettingsDialog(final @NotNull DecimalSettings settings) {
        super(settings);

        init();
        loadSettings();
    }


    @Override
    protected JComponent createCenterPanel() {
        return contentPane;
    }

    /**
     * Initialises custom UI components.
     * <p>
     * This method is called by the scene builder at the start of the constructor.
     */
    private void createUIComponents() {
        minValue = new JDoubleSpinner();
        maxValue = new JDoubleSpinner();
        valueRange = new JSpinnerRange(minValue, maxValue);
        decimalCount = new JIntSpinner(0, 0);
    }


    @Override
    public void loadSettings(final @NotNull DecimalSettings settings) {
        minValue.setValue(settings.getMinValue());
        maxValue.setValue(settings.getMaxValue());
        decimalCount.setValue(settings.getDecimalCount());
        ButtonGroupKt.setValue(groupingSeparatorGroup, settings.getGroupingSeparator());
        ButtonGroupKt.setValue(decimalSeparatorGroup, settings.getDecimalSeparator());
    }

    @Override
    public void saveSettings(final @NotNull DecimalSettings settings) {
        settings.setMinValue(minValue.getValue());
        settings.setMaxValue(maxValue.getValue());
        settings.setDecimalCount(decimalCount.getValue());

        final String groupingSeparator = ButtonGroupKt.getValue(groupingSeparatorGroup);
        settings.setGroupingSeparator(groupingSeparator == null || groupingSeparator.isEmpty()
            ? DecimalSettings.DEFAULT_GROUPING_SEPARATOR : groupingSeparator);

        final String decimalSeparator = ButtonGroupKt.getValue(decimalSeparatorGroup);
        settings.setDecimalSeparator(decimalSeparator == null || decimalSeparator.isEmpty()
            ? DecimalSettings.DEFAULT_DECIMAL_SEPARATOR : decimalSeparator);
    }

    @Override
    @Nullable
    public ValidationInfo doValidate() {
        return JavaHelperKt.firstNonNull(
            minValue.validateValue(),
            maxValue.validateValue(),
            valueRange.validateValue(),
            decimalCount.validateValue()
        );
    }
}
