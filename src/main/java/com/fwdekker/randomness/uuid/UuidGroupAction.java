package com.fwdekker.randomness.uuid;

import com.fwdekker.randomness.DataArrayInsertAction;
import com.fwdekker.randomness.DataGroupAction;
import com.fwdekker.randomness.DataInsertAction;
import com.fwdekker.randomness.SettingsAction;
import org.jetbrains.annotations.NotNull;


/**
 * All actions related to inserting UUIDs.
 */
public final class UuidGroupAction extends DataGroupAction {
    @Override
    protected DataInsertAction getInsertAction() {
        return new UuidInsertAction();
    }

    @Override
    protected DataArrayInsertAction getInsertArrayAction() {
        return new UuidArrayInsertAction();
    }

    @Override
    protected SettingsAction getSettingsAction() {
        return new UuidSettingsAction();
    }
}
