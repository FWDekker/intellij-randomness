package com.fwdekker.randomness.word;

import com.fwdekker.randomness.SettingsAction;
import com.fwdekker.randomness.SettingsDialog;


/**
 * Controller for random string generation settings.
 */
public final class WordSettingsAction extends SettingsAction {
    @Override
    public SettingsDialog createDialog() {
        return new WordSettingsDialog();
    }

    @Override
    public String getTitle() {
        return "Word Settings";
    }
}
