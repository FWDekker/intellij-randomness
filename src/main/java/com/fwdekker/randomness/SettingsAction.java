package com.fwdekker.randomness;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;


/**
 * Shows a modal dialog for changing settings.
 */
public abstract class SettingsAction extends AnAction {
    @Override
    public final void update(final AnActionEvent event) {
        super.update(event);

        event.getPresentation().setText(getTitle());
    }

    /**
     * Creates and displays a modal dialog for changing settings.
     *
     * @param event the event that triggered the creation of this dialog
     */
    @Override
    public final void actionPerformed(final AnActionEvent event) {
        final SettingsDialog dialog = createDialog();
        dialog.setTitle(getTitle());
        dialog.show();
        dialog.getExitCode();
    }


    /**
     * Returns the dialog to display.
     *
     * @return the dialog to display
     */
    protected abstract SettingsDialog createDialog();

    /**
     * Returns the title of the dialog to display.
     *
     * @return the title of the dialog to display
     */
    protected abstract String getTitle();
}
