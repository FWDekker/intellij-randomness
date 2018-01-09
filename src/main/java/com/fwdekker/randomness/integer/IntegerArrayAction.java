package com.fwdekker.randomness.integer;

import com.fwdekker.randomness.InsertRandomSomethingArray;


public final class IntegerArrayAction extends InsertRandomSomethingArray {
    public IntegerArrayAction() {
        super(new InsertRandomInteger());
    }


    @Override
    protected String getName() {
        return "Insert Integer Array";
    }
}
