package com.fwdekker.randomness.word

import com.fwdekker.randomness.DataGroupAction


/**
 * All actions related to inserting words.
 */
class WordGroupAction : DataGroupAction() {
    override fun getInsertAction() = WordInsertAction()

    override fun getInsertArrayAction() = WordInsertAction().ArrayAction()

    override fun getSettingsAction() = WordSettingsAction()
}
