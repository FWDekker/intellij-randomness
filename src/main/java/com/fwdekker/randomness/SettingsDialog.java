package com.fwdekker.randomness;

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import org.jetbrains.annotations.Nullable;


/**
 * Superclass for settings dialogs.
 * <p>
 * Subclasses <b>MUST</b> call {@link #init()} and {@link #loadSettings()} in their constructor.
 */
public abstract class SettingsDialog extends DialogWrapper {
    /**
     * Constructs a new {@code SettingsDialog}.
     * <p>
     * Subclasses <b>MUST</b> call {@link #init()} and {@link #loadSettings()} in their constructor.
     */
    protected SettingsDialog() {
        super(null);
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
     * Loads settings from the model into the UI.
     */
    protected abstract void loadSettings();

    /**
     * Commits the values entered by the user to the model.
     */
    protected abstract void saveSettings();

    /**
     * Validates all input fields.
     *
     * @return {@code null} if the input is valid, or {@code ValidationInfo} indicating the error if input is not valid
     */
    @Override
    @Nullable
    protected abstract ValidationInfo doValidate();
}
