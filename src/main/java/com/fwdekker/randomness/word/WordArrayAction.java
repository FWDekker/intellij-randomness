package com.fwdekker.randomness.word;

import com.fwdekker.randomness.InsertRandomSomethingArray;


public final class WordArrayAction extends InsertRandomSomethingArray {
    public WordArrayAction() {
        super(new InsertRandomWord());
    }


    @Override
    protected String getName() {
        return "Insert Word Array";
    }
}
