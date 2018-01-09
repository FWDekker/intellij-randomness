package com.fwdekker.randomness.word;

import com.fwdekker.randomness.DataArrayInsertAction;


/**
 * Inserts an array of words.
 */
public final class WordArrayInsertAction extends DataArrayInsertAction {
    /**
     * Constructs a new {@code WordArrayInsertAction}.
     */
    public WordArrayInsertAction() {
        super(new WordInsertAction());
    }


    @Override
    protected String getName() {
        return "Insert Word Array";
    }
}
