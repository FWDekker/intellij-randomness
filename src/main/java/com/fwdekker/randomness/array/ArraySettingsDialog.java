package com.fwdekker.randomness.array;

import com.fwdekker.randomness.SettingsDialog;
import com.fwdekker.randomness.ui.ButtonGroupHelper;
import com.fwdekker.randomness.ui.JLongSpinner;
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
    private JLongSpinner countSpinner;
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
        countSpinner = new JLongSpinner(1, 1, Integer.MAX_VALUE);
    }


    @Override
    public void loadSettings(final @NotNull ArraySettings settings) {
        countSpinner.setValue(settings.getCount());
        ButtonGroupHelper.INSTANCE.setValue(bracketsGroup, settings.getBrackets());
        ButtonGroupHelper.INSTANCE.setValue(separatorGroup, settings.getSeparator());
        spaceAfterSeparatorCheckBox.setSelected(settings.isSpaceAfterSeparator());
    }

    @Override
    public void saveSettings(final @NotNull ArraySettings settings) {
        settings.setCount(Math.toIntExact(countSpinner.getValue()));

        final String brackets = ButtonGroupHelper.INSTANCE.getValue(bracketsGroup);
        settings.setBrackets(brackets == null ? ArraySettings.DEFAULT_BRACKETS : brackets);

        final String separator = ButtonGroupHelper.INSTANCE.getValue(separatorGroup);
        settings.setSeparator(separator == null ? ArraySettings.DEFAULT_SEPARATOR : separator);

        settings.setSpaceAfterSeparator(spaceAfterSeparatorCheckBox.isSelected());
    }

    @Nullable
    @Override
    protected ValidationInfo doValidate() {
        return countSpinner.validateValue();
    }
}
