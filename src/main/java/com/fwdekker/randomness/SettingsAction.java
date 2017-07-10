package com.fwdekker.randomness;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

import javax.swing.*;


/**
 * Shows a modal dialog for changing settings.
 */
public abstract class SettingsAction extends AnAction {
    /**
     * Creates and displays a modal dialog for changing settings.
     *
     * @param event the event that triggered the creation of this dialog
     */
    @Override
    public final void actionPerformed(final AnActionEvent event) {
        final JDialog dialog = createDialog();
        dialog.setTitle(getTitle());
        dialog.pack();
        dialog.setVisible(true);
    }


    /**
     * Returns the dialog to display.
     *
     * @return the dialog to display
     */
    protected abstract JDialog createDialog();

    /**
     * Returns the title of the dialog to display.
     *
     * @return the title of the dialog to display
     */
    protected abstract String getTitle();
}
