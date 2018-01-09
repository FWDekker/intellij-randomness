package com.fwdekker.randomness.string;

import com.fwdekker.randomness.DataGroupAction;
import com.fwdekker.randomness.DataInsertAction;
import com.fwdekker.randomness.DataArrayInsertAction;
import com.fwdekker.randomness.SettingsAction;


/**
 * All actions related to inserting strings.
 */
public final class StringGroupAction extends DataGroupAction {
    @Override
    protected DataInsertAction getInsertAction() {
        return new StringInsertAction();
    }

    @Override
    protected DataArrayInsertAction getInsertArrayAction() {
        return new StringArrayInsertAction();
    }

    @Override
    protected SettingsAction getSettingsAction() {
        return new StringSettingsAction();
    }
}
