package com.fwdekker.randomness.array;

import com.fwdekker.randomness.SettingsAction;
import com.fwdekker.randomness.SettingsDialog;


/**
 * Controller for random array generation settings.
 */
public final class ArraySettingsAction extends SettingsAction {
    @Override
    public SettingsDialog createDialog() {
        return new ArraySettingsDialog();
    }

    @Override
    public String getTitle() {
        return "Array Settings";
    }
}
