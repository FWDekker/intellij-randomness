package com.fwdekker.randomness.word;

import com.fwdekker.randomness.InsertRandomSomethingArray;


/**
 * Inserts an array of words.
 */
public final class WordArrayAction extends InsertRandomSomethingArray {
    /**
     * Constructs a new {@code WordArrayAction}.
     */
    public WordArrayAction() {
        super(new InsertRandomWord());
    }


    @Override
    protected String getName() {
        return "Insert Word Array";
    }
}
