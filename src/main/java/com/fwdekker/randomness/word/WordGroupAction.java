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
    @NotNull
    protected DataInsertAction getInsertAction() {
        return new WordInsertAction();
    }

    @Override
    @NotNull
    protected DataArrayInsertAction getInsertArrayAction() {
        return new WordArrayInsertAction();
    }

    @Override
    @NotNull
    protected SettingsAction getSettingsAction() {
        return new WordSettingsAction();
    }
}
