package com.fwdekker.randomness.integer;

import com.fwdekker.randomness.DataArrayInsertAction;
import com.fwdekker.randomness.DataGroupAction;
import com.fwdekker.randomness.DataInsertAction;
import com.fwdekker.randomness.SettingsAction;
import org.jetbrains.annotations.NotNull;


/**
 * All actions related to inserting integers.
 */
public final class IntegerGroupAction extends DataGroupAction {
    @Override
    @NotNull
    protected DataInsertAction getInsertAction() {
        return new IntegerInsertAction();
    }

    @Override
    @NotNull
    protected DataArrayInsertAction getInsertArrayAction() {
        return new IntegerArrayInsertAction();
    }

    @Override
    @NotNull
    protected SettingsAction getSettingsAction() {
        return new IntegerSettingsAction();
    }
}
