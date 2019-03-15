package com.fwdekker.randomness.integer

import com.fwdekker.randomness.SettingsAction


/**
 * Controller for random integer generation settings.
 */
class IntegerSettingsAction : SettingsAction() {
    override val title = "Integer Settings"


    public override fun createDialog() = IntegerSettingsDialog()
}
