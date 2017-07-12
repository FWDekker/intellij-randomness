package com.fwdekker.randomness.integer;

import com.fwdekker.randomness.SettingsAction;
import com.intellij.openapi.ui.DialogWrapper;


/**
 * Controller for random integer generation settings.
 */
public final class IntegerSettingsAction extends SettingsAction {
    private static final String TITLE = "Insert Random Integer Settings";


    @Override
    public DialogWrapper createDialog() {
        return new IntegerSettingsDialog();
    }

    @Override
    public String getTitle() {
        return TITLE;
    }
}
