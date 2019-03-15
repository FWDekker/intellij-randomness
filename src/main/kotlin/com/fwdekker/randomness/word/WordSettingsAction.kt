package com.fwdekker.randomness.word

import com.fwdekker.randomness.SettingsAction


/**
 * Controller for random string generation settings.
 */
class WordSettingsAction : SettingsAction() {
    override val title = "Word Settings"


    public override fun createDialog() = WordSettingsDialog()
}
