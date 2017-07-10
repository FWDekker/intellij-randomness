package com.fwdekker.randomness.string;

import com.fwdekker.randomness.SettingsAction;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

import javax.swing.*;


/**
 * Controller for random string generation settings.
 */
public final class StringSettingsAction extends SettingsAction {
    private static final String TITLE = "Insert Random String Settings";


    @Override
    public JDialog createDialog() {
        return new StringSettingsDialog();
    }

    @Override
    public String getTitle() {
        return TITLE;
    }
}
