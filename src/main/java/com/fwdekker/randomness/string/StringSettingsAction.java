package com.fwdekker.randomness.string;

import com.fwdekker.randomness.SettingsAction;
import com.fwdekker.randomness.SettingsDialog;


/**
 * Controller for random string generation settings.
 */
public final class StringSettingsAction extends SettingsAction {
    private static final String TITLE = "Random String Settings";


    @Override
    public SettingsDialog createDialog() {
        return new StringSettingsDialog();
    }

    @Override
    public String getTitle() {
        return TITLE;
    }
}
