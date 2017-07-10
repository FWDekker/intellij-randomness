package com.fwdekker.randomness.string;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;


/**
 * Controller for random string generation settings.
 */
public class StringSettingsAction extends AnAction {
    /**
     * Shows a {@link StringSettingsDialog}.
     */
    @Override
    public void actionPerformed(final AnActionEvent e) {
        final StringSettingsDialog dialog = new StringSettingsDialog();
        dialog.setTitle("Insert Random String Settings");
        dialog.pack();
        dialog.setVisible(true);
    }
}
