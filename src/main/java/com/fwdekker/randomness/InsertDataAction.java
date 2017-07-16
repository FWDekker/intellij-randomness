package com.fwdekker.randomness;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;


/**
 * Shows a popup for all available actions.
 */
public final class InsertDataAction extends AnAction {
    private static final String TITLE = "Insert Random Data";


    @Override
    public void actionPerformed(final AnActionEvent event) {
        final Project project = event.getProject();
        if (project == null) {
            return;
        }

        final DefaultActionGroup actionGroup = (DefaultActionGroup) ActionManager.getInstance()
                .getAction("randomness.Group");
        final JBPopup popup = JBPopupFactory.getInstance().createActionGroupPopup(TITLE, actionGroup,
                event.getDataContext(), JBPopupFactory.ActionSelectionAid.NUMBERING, true, event.getPlace());

        popup.showCenteredInCurrentWindow(project);
    }
}
