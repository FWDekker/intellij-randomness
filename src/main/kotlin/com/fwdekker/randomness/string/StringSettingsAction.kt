package com.fwdekker.randomness.string

import com.fwdekker.randomness.SettingsAction


/**
 * Controller for random string generation settings.
 */
class StringSettingsAction : SettingsAction() {
    public override fun createDialog() = StringSettingsDialog()

    public override fun getTitle() = "String Settings"
}
