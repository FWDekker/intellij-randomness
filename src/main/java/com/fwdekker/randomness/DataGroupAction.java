package com.fwdekker.randomness;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public abstract class DataGroupAction extends ActionGroup {
    private final InsertRandomSomething insertAction;
    private final SettingsAction settingsAction;


    public DataGroupAction() {
        insertAction = getInsertAction();
        settingsAction = getSettingsAction();
    }


    @NotNull
    @Override
    public AnAction[] getChildren(final @Nullable AnActionEvent e) {
        return new AnAction[] {
                insertAction,
                settingsAction
        };
    }

    @Override
    public boolean canBePerformed(final DataContext context) {
        return context.getData(CommonDataKeys.EDITOR) != null;
    }

    @Override
    public void actionPerformed(final AnActionEvent event) {
        super.actionPerformed(event);

        getInsertAction().actionPerformed(event);
    }

    @Override
    public void update(final AnActionEvent event) {
        super.update(event);

        event.getPresentation().setText(insertAction.getName());
    }


    protected abstract InsertRandomSomething getInsertAction();

    protected abstract SettingsAction getSettingsAction();
}
