package com.fwdekker.randomness.integer;

import com.fwdekker.randomness.SettingsAction;
import javax.swing.JDialog;


/**
 * Controller for random integer generation settings.
 */
public final class IntegerSettingsAction extends SettingsAction {
    private static final String TITLE = "Insert Random Integer Settings";


    @Override
    public JDialog createDialog() {
        return new IntegerSettingsDialog();
    }

    @Override
    public String getTitle() {
        return TITLE;
    }
}
