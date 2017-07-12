package com.fwdekker.randomness.decimal;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.DialogWrapper;


/**
 * Controller for random decimal generation settings.
 */
public final class DecimalSettingsAction extends AnAction {
    private static final String TITLE = "Insert Random Decimal Settings";


    @Override
    public void actionPerformed(final AnActionEvent event) {
        final DialogWrapper dialog = createDialog();
        dialog.setTitle(getTitle());
        dialog.show();
        dialog.getExitCode();
    }

    /**
     * Returns a new {@code DialogWrapper}.
     *
     * @return a new {@code DialogWrapper}
     */
    private DialogWrapper createDialog() {
        return new DecimalSettingsDialog();
    }

    /**
     * Returns the dialog's title.
     *
     * @return the dialog's title
     */
    private String getTitle() {
        return TITLE;
    }
}
