package com.fwdekker.randomness.integer;

import com.fwdekker.randomness.DataGroupAction;
import com.fwdekker.randomness.InsertRandomSomething;
import com.fwdekker.randomness.InsertRandomSomethingArray;
import com.fwdekker.randomness.SettingsAction;


/**
 * All actions related to inserting integers.
 */
public final class IntegerGroupAction extends DataGroupAction {
    @Override
    protected InsertRandomSomething getInsertAction() {
        return new InsertRandomInteger();
    }

    @Override
    protected InsertRandomSomethingArray getInsertArrayAction() {
        return new IntegerArrayAction();
    }

    @Override
    protected SettingsAction getSettingsAction() {
        return new IntegerSettingsAction();
    }
}
