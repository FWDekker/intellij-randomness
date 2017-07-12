package com.fwdekker.randomness.integer;

import com.fwdekker.randomness.SettingsDialog;
import com.intellij.openapi.ui.ValidationInfo;
import java.text.ParseException;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSpinner;


/**
 * Dialog for settings of random integer generation.
 */
final class IntegerSettingsDialog extends SettingsDialog {
    private final IntegerSettings integerSettings = IntegerSettings.getInstance();

    private JPanel contentPane;
    private JSpinner minValue;
    private JSpinner maxValue;


    /**
     * Constructs a new {@code DecimalSettingsDialog}.
     */
    IntegerSettingsDialog() {
        super();

        init();
        loadSettings();
    }


    @Override
    protected JComponent createCenterPanel() {
        return contentPane;
    }


    @Override
    protected void loadSettings() {
        minValue.setValue(integerSettings.getMinValue());
        maxValue.setValue(integerSettings.getMaxValue());
    }

    @Override
    protected void saveSettings() {
        try {
            minValue.commitEdit();
            maxValue.commitEdit();
        } catch (final ParseException e) {
            throw new IllegalStateException("Settings were committed, but input could not be parsed.", e);
        }

        integerSettings.setMinValue((Integer) minValue.getValue());
        integerSettings.setMaxValue((Integer) maxValue.getValue());
    }

    @Override
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
}
