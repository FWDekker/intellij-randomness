package com.fwdekker.randomness.decimal;

import com.fwdekker.randomness.DataArrayInsertAction;
import com.fwdekker.randomness.DataGroupAction;
import com.fwdekker.randomness.DataInsertAction;
import com.fwdekker.randomness.SettingsAction;


/**
 * All actions related to inserting decimals.
 */
public final class DecimalGroupAction extends DataGroupAction {
    @Override
    protected DataInsertAction getInsertAction() {
        return new DecimalInsertAction();
    }

    @Override
    protected DataArrayInsertAction getInsertArrayAction() {
        return new DecimalArrayInsertAction();
    }

    @Override
    protected SettingsAction getSettingsAction() {
        return new DecimalSettingsAction();
    }
}
