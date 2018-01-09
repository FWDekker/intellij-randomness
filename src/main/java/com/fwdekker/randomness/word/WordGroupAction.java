package com.fwdekker.randomness.word;

import com.fwdekker.randomness.DataGroupAction;
import com.fwdekker.randomness.InsertRandomSomething;
import com.fwdekker.randomness.SettingsAction;


public final class WordGroupAction extends DataGroupAction {
    @Override
    protected InsertRandomSomething getInsertAction() {
        return new InsertRandomWord();
    }

    @Override
    protected SettingsAction getSettingsAction() {
        return new WordSettingsAction();
    }
}
