package com.fwdekker.randomness.decimal;

import com.fwdekker.randomness.SettingsAction;

import javax.swing.*;


/**
 * Controller for random decimal generation settings.
 */
public final class DecimalSettingsAction extends SettingsAction {
    private static final String TITLE = "Insert Random Decimal Settings";


    @Override
    public JDialog createDialog() {
        return new DecimalSettingsDialog();
    }

    @Override
    public String getTitle() {
        return TITLE;
    }
}
