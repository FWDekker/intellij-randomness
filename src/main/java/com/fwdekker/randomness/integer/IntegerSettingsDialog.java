package com.fwdekker.randomness.integer;

import com.fwdekker.randomness.JavaHelperKt;
import com.fwdekker.randomness.SettingsDialog;
import com.fwdekker.randomness.ui.ButtonGroupKt;
import com.fwdekker.randomness.ui.JIntSpinner;
import com.fwdekker.randomness.ui.JLongSpinner;
import com.fwdekker.randomness.ui.JSpinnerRange;
import com.intellij.openapi.ui.ValidationInfo;
import kotlin.Unit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JPanel;


/**
 * Dialog for settings of random integer generation.
 *
 * @see IntegerSettings
 * @see IntegerSettingsAction
 */
public final class IntegerSettingsDialog extends SettingsDialog<IntegerSettings> {
    private JPanel contentPane;
    private JSpinnerRange valueRange;
    private JLongSpinner minValue;
    private JLongSpinner maxValue;
    private JIntSpinner base;
    private ButtonGroup groupingSeparatorGroup;


    /**
     * Constructs a new {@code IntegerSettingsDialog} that uses the singleton {@code IntegerSettings} instance.
     */
    /* default */ IntegerSettingsDialog() {
        this(IntegerSettings.Companion.getDefault());
    }

    /**
     * Constructs a new {@code IntegerSettingsDialog} that uses the given {@code IntegerSettings} instance.
     *
     * @param settings the settings to manipulate with this dialog
     */
    /* default */ IntegerSettingsDialog(final @NotNull IntegerSettings settings) {
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
        minValue = new JLongSpinner();
        maxValue = new JLongSpinner();
        base = new JIntSpinner(IntegerSettings.DECIMAL_BASE, IntegerSettings.MIN_BASE, IntegerSettings.MAX_BASE);
        valueRange = new JSpinnerRange(minValue, maxValue, Long.MAX_VALUE);

        base.addChangeListener(event -> {
            final int value = ((JIntSpinner) event.getSource()).getValue();
            final boolean enabled = value == IntegerSettings.DECIMAL_BASE;

            ButtonGroupKt.forEach(
                groupingSeparatorGroup,
                button -> {
                    button.setEnabled(enabled);
                    return Unit.INSTANCE;
                }
            );
        });
    }


    @Override
    public void loadSettings(final @NotNull IntegerSettings settings) {
        minValue.setValue(settings.getMinValue());
        maxValue.setValue(settings.getMaxValue());
        base.setValue(settings.getBase());
        ButtonGroupKt.setValue(groupingSeparatorGroup, settings.getGroupingSeparator());
    }

    @Override
    public void saveSettings(final @NotNull IntegerSettings settings) {
        settings.setMinValue(minValue.getValue());
        settings.setMaxValue(maxValue.getValue());
        settings.setBase(base.getValue());
        settings.safeSetGroupingSeparator(ButtonGroupKt.getValue(groupingSeparatorGroup));
    }

    @Override
    @Nullable
    public ValidationInfo doValidate() {
        return JavaHelperKt.firstNonNull(
            minValue.validateValue(),
            maxValue.validateValue(),
            base.validateValue(),
            valueRange.validateValue()
        );
    }
}
