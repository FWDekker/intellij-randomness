package com.fwdekker.randomness.uuid

import com.fwdekker.randomness.SettingsAction


/**
 * Controller for random UUID generation settings.
 */
class UuidSettingsAction : SettingsAction() {
    override val title = "UUID Settings"


    public override fun createDialog() = UuidSettingsDialog()
}
