package com.fwdekker.randomness.string;

import com.fwdekker.randomness.DataGroupAction;
import com.fwdekker.randomness.InsertRandomSomething;
import com.fwdekker.randomness.InsertRandomSomethingArray;
import com.fwdekker.randomness.SettingsAction;


/**
 * All actions related to inserting strings.
 */
public final class StringGroupAction extends DataGroupAction {
    @Override
    protected InsertRandomSomething getInsertAction() {
        return new InsertRandomString();
    }

    @Override
    protected InsertRandomSomethingArray getInsertArrayAction() {
        return new StringArrayAction();
    }

    @Override
    protected SettingsAction getSettingsAction() {
        return new StringSettingsAction();
    }
}
