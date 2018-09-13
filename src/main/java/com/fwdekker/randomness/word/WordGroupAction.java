package com.fwdekker.randomness.word;

import com.fwdekker.randomness.DataArrayInsertAction;
import com.fwdekker.randomness.DataGroupAction;
import com.fwdekker.randomness.DataInsertAction;
import com.fwdekker.randomness.SettingsAction;
import org.jetbrains.annotations.NotNull;


/**
 * All actions related to inserting words.
 */
public final class WordGroupAction extends DataGroupAction {
    @Override
    protected DataInsertAction getInsertAction() {
        return new WordInsertAction();
    }

    @Override
    protected DataArrayInsertAction getInsertArrayAction() {
        return new WordArrayInsertAction();
    }

    @Override
    protected SettingsAction getSettingsAction() {
        return new WordSettingsAction();
    }
}
