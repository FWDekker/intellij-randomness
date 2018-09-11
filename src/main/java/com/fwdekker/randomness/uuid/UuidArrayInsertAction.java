package com.fwdekker.randomness.uuid;

import com.fwdekker.randomness.DataArrayInsertAction;


/**
 * Inserts an array of UUIDs.
 */
public final class UuidArrayInsertAction extends DataArrayInsertAction {
    /**
     * Constructs a new {@code UuidArrayInsertAction}.
     */
    public UuidArrayInsertAction() {
        super(new UuidInsertAction());
    }


    @Override
    protected String getName() {
        return "Insert UUID Array";
    }
}
