package com.fwdekker.randomness;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.ui.popup.list.ListPopupImpl;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.KeyStroke;


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
        final ListPopupImpl popup = (ListPopupImpl) JBPopupFactory.getInstance()
                .createActionGroupPopup(TITLE, actionGroup, event.getDataContext(),
                                        JBPopupFactory.ActionSelectionAid.NUMBERING, true, event.getPlace());
        popup.registerAction("invokeAction", KeyStroke.getKeyStroke("shift ENTER"), new AbstractAction() {
            @Override
            public void actionPerformed(final ActionEvent event) {
                final KeyEvent keyEvent = new KeyEvent(popup.getComponent(), event.getID(), event.getWhen(),
                                                       event.getModifiers(), KeyEvent.VK_ENTER, KeyEvent.CHAR_UNDEFINED,
                                                       KeyEvent.KEY_LOCATION_UNKNOWN);
                popup.handleSelect(true, keyEvent);
            }
        });

        popup.showCenteredInCurrentWindow(project);
    }
}
