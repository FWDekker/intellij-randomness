package com.fwdekker.randomness.string;

import com.fwdekker.randomness.InsertRandomSomethingArray;


public final class StringArrayAction extends InsertRandomSomethingArray {
    public StringArrayAction() {
        super(new InsertRandomString());
    }


    @Override
    protected String getName() {
        return "Insert String Array";
    }
}
