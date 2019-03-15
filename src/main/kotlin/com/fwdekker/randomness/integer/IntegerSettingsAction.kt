package com.fwdekker.randomness.integer

import com.fwdekker.randomness.SettingsAction


/**
 * Controller for random integer generation settings.
 */
class IntegerSettingsAction : SettingsAction() {
    public override fun createDialog() = IntegerSettingsDialog()

    public override fun getTitle() = "Integer Settings"
}
