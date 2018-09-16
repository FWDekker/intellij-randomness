package com.fwdekker.randomness.word;

import com.fwdekker.randomness.DataInsertAction.ArrayAction;
import com.fwdekker.randomness.DataGroupAction;
import com.fwdekker.randomness.DataInsertAction;
import com.fwdekker.randomness.SettingsAction;


/**
 * All actions related to inserting words.
 */
public final class WordGroupAction extends DataGroupAction {
    @Override
    protected DataInsertAction getInsertAction() {
        return new WordInsertAction();
    }

    @Override
    protected DataInsertAction.ArrayAction getInsertArrayAction() {
        return new WordInsertAction().new ArrayAction();
    }

    @Override
    protected SettingsAction getSettingsAction() {
        return new WordSettingsAction();
    }
}
