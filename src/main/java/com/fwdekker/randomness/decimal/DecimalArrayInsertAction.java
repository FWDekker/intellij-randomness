package com.fwdekker.randomness.decimal;

import com.fwdekker.randomness.DataArrayInsertAction;


/**
 * Inserts an array of decimals.
 */
public final class DecimalArrayInsertAction extends DataArrayInsertAction {
    /**
     * Constructs a new {@code DecimalArrayInsertAction}.
     */
    public DecimalArrayInsertAction() {
        super(new DecimalInsertAction());
    }


    @Override
    protected String getName() {
        return "Insert Decimal Array";
    }
}
