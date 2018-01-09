package com.fwdekker.randomness;

import com.fwdekker.randomness.ui.JBPopupHelper;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.ui.popup.list.ListPopupImpl;


/**
 * Shows a popup for all available actions.
 */
public final class PopupAction extends AnAction {
    private static final String TITLE = "Insert Random Data";
    private static final String SHIFT_TITLE = "Insert Random Array";
    private static final String CTRL_TITLE = "Insert Random Settings";
    private static final String AD_TEXT = "Shift = Array. Ctrl = Settings.";


    @Override
    public void actionPerformed(final AnActionEvent event) {
        final Project project = event.getProject();
        if (project == null) {
            return;
        }

        final DefaultActionGroup actionGroup = (DefaultActionGroup) ActionManager.getInstance()
                .getAction("randomness.Group");
        final ListPopupImpl popup = (ListPopupImpl) JBPopupFactory.getInstance()
                .createActionGroupPopup(TITLE, actionGroup, event.getDataContext(),
                                        JBPopupFactory.ActionSelectionAid.NUMBERING, true, event.getPlace());
        JBPopupHelper.disableSpeedSearch(popup);
        JBPopupHelper.registerShiftActions(popup, TITLE, SHIFT_TITLE);
        JBPopupHelper.registerCtrlActions(popup, TITLE, CTRL_TITLE);

        popup.setAdText(AD_TEXT);
        popup.showCenteredInCurrentWindow(project);
    }
}
