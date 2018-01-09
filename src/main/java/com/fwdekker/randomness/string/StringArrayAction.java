package com.fwdekker.randomness.string;

import com.fwdekker.randomness.InsertRandomSomethingArray;


/**
 * Inserts an array of strings.
 */
public final class StringArrayAction extends InsertRandomSomethingArray {
    /**
     * Constructs a new {@code StringArrayAction}.
     */
    public StringArrayAction() {
        super(new InsertRandomString());
    }


    @Override
    protected String getName() {
        return "Insert String Array";
    }
}
