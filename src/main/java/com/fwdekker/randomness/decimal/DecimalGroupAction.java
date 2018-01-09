package com.fwdekker.randomness.decimal;

import com.fwdekker.randomness.DataGroupAction;
import com.fwdekker.randomness.InsertRandomSomething;
import com.fwdekker.randomness.SettingsAction;


public final class DecimalGroupAction extends DataGroupAction {
    @Override
    protected InsertRandomSomething getInsertAction() {
        return new InsertRandomDecimal();
    }

    @Override
    protected SettingsAction getSettingsAction() {
        return new DecimalSettingsAction();
    }
}
