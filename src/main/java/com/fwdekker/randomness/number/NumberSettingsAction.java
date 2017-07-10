package com.fwdekker.randomness.number;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;


/**
 * Controller for random number generation settings.
 */
public final class NumberSettingsAction extends AnAction {
    /**
     * Shows a {@link NumberSettingsDialog}.
     */
    @Override
    public void actionPerformed(final AnActionEvent e) {
        final NumberSettingsDialog dialog = new NumberSettingsDialog();
        dialog.setTitle("Insert Random Number Settings");
        dialog.pack();
        dialog.setVisible(true);
    }
}
