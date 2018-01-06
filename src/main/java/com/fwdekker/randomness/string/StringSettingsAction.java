package com.fwdekker.randomness.string;

import com.fwdekker.randomness.SettingsAction;
import com.fwdekker.randomness.SettingsDialog;


/**
 * Controller for random string generation settings.
 */
public final class StringSettingsAction extends SettingsAction {
    @Override
    public SettingsDialog createDialog() {
        return new StringSettingsDialog();
    }

    @Override
    public String getTitle() {
        return "Random String Settings";
    }
}
