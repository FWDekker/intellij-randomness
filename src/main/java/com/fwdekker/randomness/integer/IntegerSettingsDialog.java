package com.fwdekker.randomness.integer;

import com.fwdekker.randomness.SettingsDialog;
import com.fwdekker.randomness.ui.ButtonGroupHelper;
import com.fwdekker.randomness.ui.JLongSpinner;
import com.fwdekker.randomness.ui.JSpinnerRange;
import com.intellij.openapi.ui.ValidationInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JPanel;
import java.util.Optional;


/**
 * Dialog for settings of random integer generation.
 */
public final class IntegerSettingsDialog extends SettingsDialog<IntegerSettings> {
    private JPanel contentPane;
    private JSpinnerRange valueRange;
    private JLongSpinner minValue;
    private JLongSpinner maxValue;
    private JLongSpinner base;
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
        minValue = new JLongSpinner();
        maxValue = new JLongSpinner();
        base = new JLongSpinner(IntegerSettings.DECIMAL_BASE, IntegerSettings.MIN_BASE, IntegerSettings.MAX_BASE);
        valueRange = new JSpinnerRange(minValue, maxValue, Long.MAX_VALUE);

        base.addChangeListener(event -> {
            final long value = ((JLongSpinner) event.getSource()).getValue();
            final boolean enabled = value == IntegerSettings.DECIMAL_BASE;
            ButtonGroupHelper.INSTANCE.forEach(groupingSeparatorGroup,
                button -> button.setEnabled(enabled));
        });
    }

    @Override
    @Nullable
    protected ValidationInfo doValidate() {
        return Optional.ofNullable(minValue.validateValue())
            .orElse(Optional.ofNullable(maxValue.validateValue())
                .orElse(Optional.ofNullable(base.validateValue())
                    .orElse(valueRange.validateValue())));
    }


    @Override
    public void loadSettings(final @NotNull IntegerSettings settings) {
        minValue.setValue(settings.getMinValue());
        maxValue.setValue(settings.getMaxValue());
        base.setValue((long) settings.getBase());
        ButtonGroupHelper.INSTANCE.setValue(groupingSeparatorGroup, String.valueOf(settings.getGroupingSeparator()));
    }

    @Override
    public void saveSettings(final @NotNull IntegerSettings settings) {
        settings.setMinValue(minValue.getValue());
        settings.setMaxValue(maxValue.getValue());
        settings.setBase(base.getValue().intValue());
        settings.setGroupingSeparator(ButtonGroupHelper.INSTANCE.getValue(groupingSeparatorGroup));
    }
}
