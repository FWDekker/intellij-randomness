package com.fwdekker.randomness;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.event.InputEvent;


/**
 * A group of actions for a particular type of random data that can be generated.
 */
public abstract class DataGroupAction extends ActionGroup {
    private final @NotNull DataInsertAction insertAction;
    private final @NotNull DataInsertAction.ArrayAction insertArrayAction;
    private final @NotNull SettingsAction settingsAction;


    /**
     * Constructs a new {@code DataGroupAction}.
     */
    public DataGroupAction() {
        super();

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
    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH") // Verified by annotation
    public final void actionPerformed(final @NotNull AnActionEvent event) {
        super.actionPerformed(event);

        final boolean shiftPressed = (event.getModifiers() & (InputEvent.SHIFT_MASK | InputEvent.SHIFT_DOWN_MASK)) != 0;
        final boolean ctrlPressed = (event.getModifiers() & (InputEvent.CTRL_MASK | InputEvent.CTRL_DOWN_MASK)) != 0;

        if (shiftPressed) {
            insertArrayAction.actionPerformed(event);
        } else if (ctrlPressed) {
            settingsAction.actionPerformed(event);
        } else {
            insertAction.actionPerformed(event);
        }
    }

    @Override
    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH") // Verified by annotation
    public final void update(final @NotNull AnActionEvent event) {
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
     * Returns a new {@link DataInsertAction.ArrayAction}.
     *
     * @return a new {@link DataInsertAction.ArrayAction}
     */
    protected abstract DataInsertAction.ArrayAction getInsertArrayAction();

    /**
     * Returns a new {@link SettingsAction}.
     *
     * @return a new {@link SettingsAction}
     */
    protected abstract SettingsAction getSettingsAction();
}
