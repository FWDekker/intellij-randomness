package com.fwdekker.randomness.integer;

import com.fwdekker.randomness.DataArrayInsertAction;


/**
 * Inserts an array of integers.
 */
public final class IntegerArrayInsertAction extends DataArrayInsertAction {
    /**
     * Constructs a new {@code IntegerArrayInsertAction}.
     */
    public IntegerArrayInsertAction() {
        super(new IntegerInsertAction());
    }


    @Override
    protected String getName() {
        return "Insert Integer Array";
    }
}
