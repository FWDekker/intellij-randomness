package com.fwdekker.randomness.integer;

import com.fwdekker.randomness.SettingsAction;
import com.fwdekker.randomness.SettingsDialog;


/**
 * Controller for random integer generation settings.
 */
public final class IntegerSettingsAction extends SettingsAction {
    @Override
    public SettingsDialog createDialog() {
        return new IntegerSettingsDialog();
    }

    @Override
    public String getTitle() {
        return "Integer Settings";
    }
}
