package com.fwdekker.randomness;

import com.fwdekker.randomness.array.ArraySettingsAction;
import com.fwdekker.randomness.decimal.DecimalGroupAction;
import com.fwdekker.randomness.integer.IntegerGroupAction;
import com.fwdekker.randomness.string.StringGroupAction;
import com.fwdekker.randomness.ui.JBPopupHelper;
import com.fwdekker.randomness.uuid.UuidGroupAction;
import com.fwdekker.randomness.word.WordGroupAction;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Separator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.ui.popup.list.ListPopupImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


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

        final ListPopupImpl popup = (ListPopupImpl) JBPopupFactory.getInstance()
                .createActionGroupPopup(TITLE, new PopupGroup(), event.getDataContext(),
                        JBPopupFactory.ActionSelectionAid.NUMBERING, true, event.getPlace());
        JBPopupHelper.disableSpeedSearch(popup);
        JBPopupHelper.registerShiftActions(popup, TITLE, SHIFT_TITLE);
        JBPopupHelper.registerCtrlActions(popup, TITLE, CTRL_TITLE);

        popup.setAdText(AD_TEXT);
        popup.showCenteredInCurrentWindow(project);
    }


    /**
     * The {@code ActionGroup} containing the popup's actions.
     */
    private static class PopupGroup extends ActionGroup {
        @NotNull
        @Override
        public AnAction[] getChildren(final @Nullable AnActionEvent event) {
            return new AnAction[]{
                    new IntegerGroupAction(),
                    new DecimalGroupAction(),
                    new StringGroupAction(),
                    new WordGroupAction(),
                    new UuidGroupAction(),
                    new Separator(),
                    new ArraySettingsAction()
            };
        }
    }
}
