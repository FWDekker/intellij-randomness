package com.fwdekker.randomness.uuid

import com.fwdekker.randomness.SettingsAction


/**
 * Controller for random UUID generation settings.
 */
class UuidSettingsAction : SettingsAction() {
    public override fun createDialog() = UuidSettingsDialog()

    public override fun getTitle() = "UUID Settings"
}
