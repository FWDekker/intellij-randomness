package com.fwdekker.randomness.uuid;

import com.fwdekker.randomness.DataInsertAction.ArrayAction;
import com.fwdekker.randomness.DataGroupAction;
import com.fwdekker.randomness.DataInsertAction;
import com.fwdekker.randomness.SettingsAction;


/**
 * All actions related to inserting UUIDs.
 */
public final class UuidGroupAction extends DataGroupAction {
    @Override
    protected DataInsertAction getInsertAction() {
        return new UuidInsertAction();
    }

    @Override
    protected DataInsertAction.ArrayAction getInsertArrayAction() {
        return new UuidInsertAction().new ArrayAction();
    }

    @Override
    protected SettingsAction getSettingsAction() {
        return new UuidSettingsAction();
    }
}
