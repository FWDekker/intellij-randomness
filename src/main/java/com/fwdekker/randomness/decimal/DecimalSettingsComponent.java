package com.fwdekker.randomness.decimal;

import com.fwdekker.randomness.JavaHelperKt;
import com.fwdekker.randomness.SettingsComponent;
import com.fwdekker.randomness.ui.ButtonGroupKt;
import com.fwdekker.randomness.ui.JDoubleSpinner;
import com.fwdekker.randomness.ui.JIntSpinner;
import com.fwdekker.randomness.ui.JSpinnerRange;
import com.intellij.openapi.ui.ValidationInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JPanel;


/**
 * Component for settings of random decimal generation.
 *
 * @see DecimalSettings
 * @see DecimalSettingsAction
 */
public final class DecimalSettingsComponent extends SettingsComponent<DecimalSettings> {
    private JPanel contentPane;
    private JSpinnerRange valueRange;
    private JDoubleSpinner minValue;
    private JDoubleSpinner maxValue;
    private JIntSpinner decimalCount;
    private JCheckBox showTrailingZeroesCheckBox;
    private ButtonGroup groupingSeparatorGroup;
    private ButtonGroup decimalSeparatorGroup;


    /**
     * Constructs a new {@code DecimalSettingsComponent} that uses the singleton {@code DecimalSettings} instance.
     */
    /* default */ DecimalSettingsComponent() {
        this(DecimalSettings.Companion.getDefault());
    }

    /**
     * Constructs a new {@code DecimalSettingsComponent} that uses the given {@code DecimalSettings} instance.
     *
     * @param settings the settings to manipulate with this component
     */
    /* default */ DecimalSettingsComponent(final @NotNull DecimalSettings settings) {
        super(settings);

        loadSettings();
    }


    @Override
    public JPanel getRootPane() {
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

        decimalCount.addChangeListener(event -> {
            final int value = ((JIntSpinner) event.getSource()).getValue();
            showTrailingZeroesCheckBox.setEnabled(value > 0);
        });
    }


    @Override
    public void loadSettings(final @NotNull DecimalSettings settings) {
        minValue.setValue(settings.getMinValue());
        maxValue.setValue(settings.getMaxValue());
        decimalCount.setValue(settings.getDecimalCount());
        showTrailingZeroesCheckBox.setSelected(settings.getShowTrailingZeroes());
        ButtonGroupKt.setValue(groupingSeparatorGroup, settings.getGroupingSeparator());
        ButtonGroupKt.setValue(decimalSeparatorGroup, settings.getDecimalSeparator());
    }

    @Override
    public void saveSettings(final @NotNull DecimalSettings settings) {
        settings.setMinValue(minValue.getValue());
        settings.setMaxValue(maxValue.getValue());
        settings.setDecimalCount(decimalCount.getValue());
        settings.setShowTrailingZeroes(showTrailingZeroesCheckBox.isSelected());
        settings.safeSetGroupingSeparator(ButtonGroupKt.getValue(groupingSeparatorGroup));
        settings.safeSetDecimalSeparator(ButtonGroupKt.getValue(decimalSeparatorGroup));
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
