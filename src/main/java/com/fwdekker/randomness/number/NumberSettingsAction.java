package com.fwdekker.randomness.number;

import com.fwdekker.randomness.SettingsAction;

import javax.swing.*;


/**
 * Controller for random number generation settings.
 */
public final class NumberSettingsAction extends SettingsAction {
    private static final String TITLE = "Insert Random Number Settings";


    @Override
    public JDialog createDialog() {
        return new NumberSettingsDialog();
    }

    @Override
    public String getTitle() {
        return TITLE;
    }
}
