package com.fwdekker.randomness.string;

import com.fwdekker.randomness.DataInsertAction.ArrayAction;
import com.fwdekker.randomness.DataGroupAction;
import com.fwdekker.randomness.DataInsertAction;
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
    protected DataInsertAction.ArrayAction getInsertArrayAction() {
        return new StringInsertAction().new ArrayAction();
    }

    @Override
    protected SettingsAction getSettingsAction() {
        return new StringSettingsAction();
    }
}
