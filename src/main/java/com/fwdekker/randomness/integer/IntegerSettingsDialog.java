package com.fwdekker.randomness.integer;

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
 * Dialog for settings of random integer generation.
 */
final class IntegerSettingsDialog extends SettingsDialog<IntegerSettings> {
    private JPanel contentPane;
    private JSpinner minValue;
    private JSpinner maxValue;


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
    IntegerSettingsDialog(@NotNull final IntegerSettings settings) {
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
        minValue = new JSpinner(new SpinnerNumberModel(0L, Long.MIN_VALUE, Long.MAX_VALUE, 1L));
        maxValue = new JSpinner(new SpinnerNumberModel(0L, Long.MIN_VALUE, Long.MAX_VALUE, 1L));
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

        final double newMinValue = ((Number) minValue.getValue()).doubleValue();
        if (newMinValue % 1 != 0) {
            return new ValidationInfo("Minimum value must be an integer.", minValue);
        }

        final double newMaxValue = ((Number) maxValue.getValue()).doubleValue();
        if (newMaxValue % 1 != 0) {
            return new ValidationInfo("Maximum value must be an integer.", maxValue);
        }

        if (newMaxValue < newMinValue) {
            return new ValidationInfo("Maximum value cannot be smaller than minimum value.", maxValue);
        }

        return null;
    }


    @Override
    public void loadSettings(@NotNull final IntegerSettings settings) {
        minValue.setValue(settings.getMinValue());
        maxValue.setValue(settings.getMaxValue());
    }

    @Override
    public void saveSettings(@NotNull final IntegerSettings settings) {
        final long newMinValue = ((Number) minValue.getValue()).longValue();
        final long newMaxValue = ((Number) maxValue.getValue()).longValue();

        settings.setMinValue(newMinValue);
        settings.setMaxValue(newMaxValue);
    }
}
