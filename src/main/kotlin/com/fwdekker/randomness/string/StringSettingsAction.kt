package com.fwdekker.randomness.string

import com.fwdekker.randomness.SettingsAction


/**
 * Controller for random string generation settings.
 */
class StringSettingsAction : SettingsAction() {
    override val title = "String Settings"


    public override fun createDialog() = StringSettingsDialog()
}
