package com.fwdekker.randomness.uuid;

import com.fwdekker.randomness.SettingsComponent;
import com.fwdekker.randomness.ui.ButtonGroupKt;
import com.intellij.openapi.ui.ValidationInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.ButtonGroup;
import javax.swing.JPanel;


/**
 * Component for settings of random UUID generation.
 *
 * @see UuidSettings
 * @see UuidSettingsAction
 */
public final class UuidSettingsComponent extends SettingsComponent<UuidSettings> {
    private JPanel contentPane;
    private ButtonGroup enclosureGroup;


    /**
     * Constructs a new {@code UuidSettingsComponent} that uses the singleton {@code UuidSettings} instance.
     */
    /* default */ UuidSettingsComponent() {
        this(UuidSettings.Companion.getDefault());
    }

    /**
     * Constructs a new {@code UuidSettingsComponent} that uses the given {@code UuidSettings} instance.
     *
     * @param settings the settings to manipulate with this component
     */
    /* default */ UuidSettingsComponent(final @NotNull UuidSettings settings) {
        super(settings);

        loadSettings();
    }


    @Override
    public JPanel getRootPane() {
        return contentPane;
    }


    @Override
    public void loadSettings(final @NotNull UuidSettings settings) {
        ButtonGroupKt.setValue(enclosureGroup, settings.getEnclosure());
    }

    @Override
    public void saveSettings(final @NotNull UuidSettings settings) {
        final String enclosure = ButtonGroupKt.getValue(enclosureGroup);
        settings.setEnclosure(enclosure == null ? UuidSettings.DEFAULT_ENCLOSURE : enclosure);
    }

    @Override
    @Nullable
    public ValidationInfo doValidate() {
        return null;
    }
}
