package com.fwdekker.randomness.integer;

import com.fwdekker.randomness.DataGroupAction;
import com.fwdekker.randomness.InsertRandomSomething;
import com.fwdekker.randomness.SettingsAction;


public final class IntegerGroupAction extends DataGroupAction {
    @Override
    protected InsertRandomSomething getInsertAction() {
        return new InsertRandomInteger();
    }

    @Override
    protected SettingsAction getSettingsAction() {
        return new IntegerSettingsAction();
    }
}
