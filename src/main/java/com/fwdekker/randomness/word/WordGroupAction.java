package com.fwdekker.randomness.word;

import com.fwdekker.randomness.DataGroupAction;
import com.fwdekker.randomness.InsertRandomSomething;
import com.fwdekker.randomness.InsertRandomSomethingArray;
import com.fwdekker.randomness.SettingsAction;


/**
 * All actions related to inserting words.
 */
public final class WordGroupAction extends DataGroupAction {
    @Override
    protected InsertRandomSomething getInsertAction() {
        return new InsertRandomWord();
    }

    @Override
    protected InsertRandomSomethingArray getInsertArrayAction() {
        return new WordArrayAction();
    }

    @Override
    protected SettingsAction getSettingsAction() {
        return new WordSettingsAction();
    }
}
