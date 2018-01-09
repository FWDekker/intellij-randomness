package com.fwdekker.randomness.decimal;

import com.fwdekker.randomness.InsertRandomSomethingArray;


public final class DecimalArrayAction extends InsertRandomSomethingArray {
    public DecimalArrayAction() {
        super(new InsertRandomDecimal());
    }


    @Override
    protected String getName() {
        return "Insert Decimal Array";
    }
}
