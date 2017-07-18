package com.fwdekker.randomness;

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import org.jetbrains.annotations.Nullable;


/**
 * Superclass for settings dialogs.
 * <p>
 * Subclasses <b>MUST</b> call {@link #init()} and {@link #loadSettings()} in their constructor.
 *
 * @param <S> the type of settings managed by the subclass
 */
public abstract class SettingsDialog<S extends Settings> extends DialogWrapper implements SettingsManager<S> {
    private final S settings;


    /**
     * Constructs a new {@code SettingsDialog}.
     * <p>
     * Subclasses <b>MUST</b> call {@link #init()} and {@link #loadSettings()} in their constructor.
     *
     * @param settings the settings to manage
     */
    protected SettingsDialog(final S settings) {
        super(null);

        this.settings = settings;
    }


    @Override
    public final void loadSettings() {
        loadSettings(settings);
    }

    @Override
    public final void saveSettings() {
        saveSettings(settings);
    }


    @Override
    protected final String getDimensionServiceKey() {
        return getClass().getSimpleName();
    }

    @Override
    protected final void doOKAction() {
        processDoNotAskOnOk(OK_EXIT_CODE);

        if (getOKAction().isEnabled()) {
            saveSettings();
            close(OK_EXIT_CODE);
        }
    }

    /**
     * Validates all input fields.
     *
     * @return {@code null} if the input is valid, or {@code ValidationInfo} indicating the error if input is not valid
     */
    @Override
    @Nullable
    protected abstract ValidationInfo doValidate();
}
