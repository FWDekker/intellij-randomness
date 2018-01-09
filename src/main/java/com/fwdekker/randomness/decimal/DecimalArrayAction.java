package com.fwdekker.randomness.decimal;

import com.fwdekker.randomness.InsertRandomSomethingArray;


/**
 * Inserts an array of decimals.
 */
public final class DecimalArrayAction extends InsertRandomSomethingArray {
    /**
     * Constructs a new {@code DecimalArrayAction}.
     */
    public DecimalArrayAction() {
        super(new InsertRandomDecimal());
    }


    @Override
    protected String getName() {
        return "Insert Decimal Array";
    }
}
