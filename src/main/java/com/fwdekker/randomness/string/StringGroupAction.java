package com.fwdekker.randomness.string;

import com.fwdekker.randomness.DataArrayInsertAction;
import com.fwdekker.randomness.DataGroupAction;
import com.fwdekker.randomness.DataInsertAction;
import com.fwdekker.randomness.SettingsAction;
import org.jetbrains.annotations.NotNull;


/**
 * All actions related to inserting strings.
 */
public final class StringGroupAction extends DataGroupAction {
    @Override
    @NotNull
    protected DataInsertAction getInsertAction() {
        return new StringInsertAction();
    }

    @Override
    @NotNull
    protected DataArrayInsertAction getInsertArrayAction() {
        return new StringArrayInsertAction();
    }

    @Override
    @NotNull
    protected SettingsAction getSettingsAction() {
        return new StringSettingsAction();
    }
}
