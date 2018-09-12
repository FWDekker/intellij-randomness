package com.fwdekker.randomness.decimal;

import com.fwdekker.randomness.DataArrayInsertAction;
import com.fwdekker.randomness.DataGroupAction;
import com.fwdekker.randomness.DataInsertAction;
import com.fwdekker.randomness.SettingsAction;
import org.jetbrains.annotations.NotNull;


/**
 * All actions related to inserting decimals.
 */
public final class DecimalGroupAction extends DataGroupAction {
    @Override
    @NotNull
    protected DataInsertAction getInsertAction() {
        return new DecimalInsertAction();
    }

    @Override
    @NotNull
    protected DataArrayInsertAction getInsertArrayAction() {
        return new DecimalArrayInsertAction();
    }

    @Override
    @NotNull
    protected SettingsAction getSettingsAction() {
        return new DecimalSettingsAction();
    }
}
