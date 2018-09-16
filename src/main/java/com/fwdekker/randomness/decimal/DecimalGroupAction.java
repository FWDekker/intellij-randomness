package com.fwdekker.randomness.decimal;

import com.fwdekker.randomness.DataInsertAction.ArrayAction;
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
    protected DataInsertAction.ArrayAction getInsertArrayAction() {
        return new DecimalInsertAction().new ArrayAction();
    }

    @Override
    protected SettingsAction getSettingsAction() {
        return new DecimalSettingsAction();
    }
}
