package com.fwdekker.randomness.string;

import com.fwdekker.randomness.SettingsAction;
import com.intellij.openapi.ui.DialogWrapper;
import javax.swing.JDialog;


/**
 * Controller for random string generation settings.
 */
public final class StringSettingsAction extends SettingsAction {
    private static final String TITLE = "Insert Random String Settings";


    @Override
    public DialogWrapper createDialog() {
        return new StringSettingsDialog();
    }

    @Override
    public String getTitle() {
        return TITLE;
    }
}
