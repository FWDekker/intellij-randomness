package com.fwdekker.randomness;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import java.awt.event.InputEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public abstract class DataGroupAction extends ActionGroup {
    private final InsertRandomSomething insertAction;
    private final InsertRandomSomethingArray insertArrayAction;
    private final SettingsAction settingsAction;


    public DataGroupAction() {
        insertAction = getInsertAction();
        insertArrayAction = getInsertArrayAction();
        settingsAction = getSettingsAction();
    }


    @NotNull
    @Override
    public AnAction[] getChildren(final @Nullable AnActionEvent e) {
        return new AnAction[] {
                insertAction,
                insertArrayAction,
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

        if ((event.getModifiers() & (InputEvent.SHIFT_MASK | InputEvent.SHIFT_DOWN_MASK)) != 0) {
            getInsertArrayAction().actionPerformed(event);
        } else if ((event.getModifiers() & (InputEvent.CTRL_MASK | InputEvent.CTRL_DOWN_MASK)) != 0) {
            getSettingsAction().actionPerformed(event);
        } else {
            getInsertAction().actionPerformed(event);
        }
    }

    @Override
    public void update(final AnActionEvent event) {
        super.update(event);

        event.getPresentation().setText(insertAction.getName());
    }


    protected abstract InsertRandomSomething getInsertAction();

    protected abstract InsertRandomSomethingArray getInsertArrayAction();

    protected abstract SettingsAction getSettingsAction();
}
