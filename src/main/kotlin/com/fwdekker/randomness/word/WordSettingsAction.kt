package com.fwdekker.randomness.word

import com.fwdekker.randomness.SettingsAction


/**
 * Controller for random string generation settings.
 */
class WordSettingsAction : SettingsAction() {
    public override fun createDialog() = WordSettingsDialog()

    public override fun getTitle() = "Word Settings"
}
