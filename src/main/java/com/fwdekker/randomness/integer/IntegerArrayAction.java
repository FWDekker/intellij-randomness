package com.fwdekker.randomness.integer;

import com.fwdekker.randomness.InsertRandomSomethingArray;


/**
 * Inserts an array of integers.
 */
public final class IntegerArrayAction extends InsertRandomSomethingArray {
    /**
     * Constructs a new {@code IntegerArrayAction}.
     */
    public IntegerArrayAction() {
        super(new InsertRandomInteger());
    }


    @Override
    protected String getName() {
        return "Insert Integer Array";
    }
}
