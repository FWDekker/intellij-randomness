package com.fwdekker.randomness;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.event.InputEvent;


/**
 * A group of actions for a particular type of random data that can be generated.
 */
public abstract class DataGroupAction extends ActionGroup {
    private final DataInsertAction insertAction;
    private final DataArrayInsertAction insertArrayAction;
    private final SettingsAction settingsAction;


    /**
     * Constructs a new {@code DataGroupAction}.
     */
    public DataGroupAction() {
        insertAction = getInsertAction();
        insertArrayAction = getInsertArrayAction();
        settingsAction = getSettingsAction();
    }


    @NotNull
    @Override
    public final AnAction[] getChildren(final @Nullable AnActionEvent event) {
        return new AnAction[]{
                insertAction,
                insertArrayAction,
                settingsAction
        };
    }

    @Override
    public final boolean canBePerformed(final DataContext context) {
        return true;
    }

    @Override
    @SuppressWarnings("PMD.ConfusingTernary") // != 0 for binary mask is expected
    public final void actionPerformed(final AnActionEvent event) {
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
    public final void update(final AnActionEvent event) {
        super.update(event);

        event.getPresentation().setText(insertAction.getName());
    }

    @Override
    public final boolean isPopup() {
        return true;
    }


    /**
     * Returns a new {@link DataInsertAction}.
     *
     * @return a new {@link DataInsertAction}
     */
    protected abstract DataInsertAction getInsertAction();

    /**
     * Returns a new {@link DataArrayInsertAction}.
     *
     * @return a new {@link DataArrayInsertAction}
     */
    protected abstract DataArrayInsertAction getInsertArrayAction();

    /**
     * Returns a new {@link SettingsAction}.
     *
     * @return a new {@link SettingsAction}
     */
    protected abstract SettingsAction getSettingsAction();
}
