package com.fwdekker.randomness.integer;

import com.fwdekker.randomness.SettingsDialog;
import com.fwdekker.randomness.ValidationException;
import com.fwdekker.randomness.ui.ButtonGroupHelper;
import com.fwdekker.randomness.ui.JLongSpinner;
import com.fwdekker.randomness.ui.JSpinnerRange;
import com.intellij.openapi.ui.ValidationInfo;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JPanel;


/**
 * Dialog for settings of random integer generation.
 */
final class IntegerSettingsDialog extends SettingsDialog<IntegerSettings> {
    private JPanel contentPane;
    private JSpinnerRange valueRange;
    private JLongSpinner minValue;
    private JLongSpinner maxValue;
    private ButtonGroup groupingSeparatorGroup;


    /**
     * Constructs a new {@code IntegerSettingsDialog} that uses the singleton {@code IntegerSettings} instance.
     */
    IntegerSettingsDialog() {
        this(IntegerSettings.getInstance());
    }

    /**
     * Constructs a new {@code IntegerSettingsDialog} that uses the given {@code IntegerSettings} instance.
     *
     * @param settings the settings to manipulate with this dialog
     */
    IntegerSettingsDialog(final @NotNull IntegerSettings settings) {
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
        minValue = new JLongSpinner();
        maxValue = new JLongSpinner();
        valueRange = new JSpinnerRange(minValue, maxValue, Long.MAX_VALUE);
    }

    @Override
    @Nullable
    protected ValidationInfo doValidate() {
        try {
            minValue.validateValue();
            maxValue.validateValue();
            valueRange.validate();
        } catch (final ValidationException e) {
            return new ValidationInfo(e.getMessage(), e.getComponent());
        }

        return null;
    }


    @Override
    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH") // minValue and such are always non-null
    public void loadSettings(final @NotNull IntegerSettings settings) {
        minValue.setValue(settings.getMinValue());
        maxValue.setValue(settings.getMaxValue());
        ButtonGroupHelper.setValue(groupingSeparatorGroup, String.valueOf(settings.getGroupingSeparator()));
    }

    @Override
    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH") // minValue and such are always non-null
    public void saveSettings(final @NotNull IntegerSettings settings) {
        settings.setMinValue(minValue.getValue());
        settings.setMaxValue(maxValue.getValue());
        settings.setGroupingSeparator(ButtonGroupHelper.getValue(groupingSeparatorGroup));
    }
}
