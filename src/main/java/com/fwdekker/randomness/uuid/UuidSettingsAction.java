package com.fwdekker.randomness.uuid;

import com.fwdekker.randomness.SettingsAction;
import com.fwdekker.randomness.SettingsDialog;


/**
 * Controller for random UUID generation settings.
 */
public final class UuidSettingsAction extends SettingsAction {
    @Override
    public SettingsDialog createDialog() {
        return new UuidSettingsDialog();
    }

    @Override
    public String getTitle() {
        return "Integer Settings";
    }
}
