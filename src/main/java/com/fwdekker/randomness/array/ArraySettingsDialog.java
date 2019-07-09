package com.fwdekker.randomness.array;

import com.fwdekker.randomness.SettingsDialog;
import com.fwdekker.randomness.ui.ButtonGroupKt;
import com.fwdekker.randomness.ui.JIntSpinner;
import com.intellij.openapi.ui.ValidationInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;


/**
 * Dialog for settings of random array generation.
 *
 * @see ArraySettings
 * @see ArraySettingsAction
 */
public final class ArraySettingsDialog extends SettingsDialog<ArraySettings> {
    private JPanel contentPane;
    private JIntSpinner countSpinner;
    private ButtonGroup bracketsGroup;
    private ButtonGroup separatorGroup;
    private JCheckBox spaceAfterSeparatorCheckBox;


    /**
     * Constructs a new {@code StringSettingsDialog} that uses the singleton {@code StringSettings} instance.
     */
    /* default */ ArraySettingsDialog() {
        this(ArraySettings.Companion.getDefault());
    }

    /**
     * Constructs a new {@code StringSettingsDialog} that uses the given {@code StringSettings} instance.
     *
     * @param settings the settings to manipulate with this dialog
     */
    /* default */ ArraySettingsDialog(final @NotNull ArraySettings settings) {
        super(settings);

        loadSettings();
    }


    @Override
    public JComponent getRootPane() {
        return contentPane;
    }

    /**
     * Initialises custom UI components.
     * <p>
     * This method is called by the scene builder at the start of the constructor.
     */
    private void createUIComponents() {
        countSpinner = new JIntSpinner(1, 1);
    }


    @Override
    public void loadSettings(final @NotNull ArraySettings settings) {
        countSpinner.setValue(settings.getCount());
        ButtonGroupKt.setValue(bracketsGroup, settings.getBrackets());
        ButtonGroupKt.setValue(separatorGroup, settings.getSeparator());
        spaceAfterSeparatorCheckBox.setSelected(settings.isSpaceAfterSeparator());
    }

    @Override
    public void saveSettings(final @NotNull ArraySettings settings) {
        settings.setCount(countSpinner.getValue());

        final String brackets = ButtonGroupKt.getValue(bracketsGroup);
        settings.setBrackets(brackets == null ? ArraySettings.DEFAULT_BRACKETS : brackets);

        final String separator = ButtonGroupKt.getValue(separatorGroup);
        settings.setSeparator(separator == null ? ArraySettings.DEFAULT_SEPARATOR : separator);

        settings.setSpaceAfterSeparator(spaceAfterSeparatorCheckBox.isSelected());
    }

    @Nullable
    @Override
    public ValidationInfo doValidate() {
        return countSpinner.validateValue();
    }
}
