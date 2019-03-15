package com.fwdekker.randomness.word

import com.fwdekker.randomness.DataGroupAction


/**
 * All actions related to inserting words.
 */
class WordGroupAction : DataGroupAction() {
    override val insertAction = WordInsertAction()
    override val insertArrayAction = WordInsertAction().ArrayAction()
    override val settingsAction = WordSettingsAction()
}
