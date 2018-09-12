package com.fwdekker.randomness.uuid;

import com.fwdekker.randomness.SettingsDialog;
import com.fwdekker.randomness.ui.ButtonGroupHelper;
import com.intellij.openapi.ui.ValidationInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JPanel;


/**
 * Dialog for settings of random UUID generation.
 */
public final class UuidSettingsDialog extends SettingsDialog<UuidSettings> {
    private JPanel contentPane;
    private ButtonGroup enclosureGroup;


    /**
     * Constructs a new {@code UuidSettingsDialog} that uses the singleton {@code UuidSettings} instance.
     */
    UuidSettingsDialog() {
        this(UuidSettings.getInstance());
    }

    /**
     * Constructs a new {@code UuidSettingsDialog} that uses the given {@code UuidSettings} instance.
     *
     * @param settings the settings to manipulate with this dialog
     */
    UuidSettingsDialog(final @NotNull UuidSettings settings) {
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
        return null;
    }


    @Override
    public void loadSettings(final @NotNull UuidSettings settings) {
        ButtonGroupHelper.setValue(enclosureGroup, settings.getEnclosure());
    }

    @Override
    public void saveSettings(final @NotNull UuidSettings settings) {
        settings.setEnclosure(ButtonGroupHelper.getValue(enclosureGroup));
    }
}
