package com.fwdekker.randomness.string;

import com.fwdekker.randomness.DataArrayInsertAction;


/**
 * Inserts an array of strings.
 */
public final class StringArrayInsertAction extends DataArrayInsertAction {
    /**
     * Constructs a new {@code StringArrayInsertAction}.
     */
    public StringArrayInsertAction() {
        super(new StringInsertAction());
    }


    @Override
    protected String getName() {
        return "Insert String Array";
    }
}
