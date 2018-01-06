package com.fwdekker.randomness.decimal;

import com.fwdekker.randomness.SettingsAction;
import com.fwdekker.randomness.SettingsDialog;


/**
 * Controller for random decimal generation settings.
 */
public final class DecimalSettingsAction extends SettingsAction {
    @Override
    public SettingsDialog createDialog() {
        return new DecimalSettingsDialog();
    }

    @Override
    public String getTitle() {
        return "Random Decimal Settings";
    }
}
