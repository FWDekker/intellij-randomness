package com.fwdekker.randomness.decimal;

import com.fwdekker.randomness.DataGroupAction;
import com.fwdekker.randomness.InsertRandomSomething;
import com.fwdekker.randomness.InsertRandomSomethingArray;
import com.fwdekker.randomness.SettingsAction;


/**
 * All actions related to inserting decimals.
 */
public final class DecimalGroupAction extends DataGroupAction {
    @Override
    protected InsertRandomSomething getInsertAction() {
        return new InsertRandomDecimal();
    }

    @Override
    protected InsertRandomSomethingArray getInsertArrayAction() {
        return new DecimalArrayAction();
    }

    @Override
    protected SettingsAction getSettingsAction() {
        return new DecimalSettingsAction();
    }
}
