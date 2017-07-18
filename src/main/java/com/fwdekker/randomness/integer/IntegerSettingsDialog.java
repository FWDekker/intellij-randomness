package com.fwdekker.randomness.integer;

import com.fwdekker.randomness.SettingsDialog;
import com.intellij.openapi.ui.ValidationInfo;
import java.text.ParseException;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSpinner;
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

    @Override
    @Nullable
    protected ValidationInfo doValidate() {
        if (!(minValue.getValue() instanceof Integer)) {
            return new ValidationInfo("Minimum value must be an integer.", minValue);
        }
        if (!(maxValue.getValue() instanceof Integer)) {
            return new ValidationInfo("Maximum value must be an integer.", maxValue);
        }

        final int newMinValue = (Integer) minValue.getValue();
        final int newMaxValue = (Integer) maxValue.getValue();
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
        try {
            minValue.commitEdit();
            maxValue.commitEdit();
        } catch (final ParseException e) {
            throw new IllegalStateException("Settings were committed, but input could not be parsed.", e);
        }

        settings.setMinValue((Integer) minValue.getValue());
        settings.setMaxValue((Integer) maxValue.getValue());
    }
}
