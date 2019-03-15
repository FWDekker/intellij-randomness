package com.fwdekker.randomness.decimal

import com.fwdekker.randomness.SettingsAction


/**
 * Controller for random decimal generation settings.
 */
class DecimalSettingsAction : SettingsAction() {
    public override fun createDialog() = DecimalSettingsDialog()

    public override fun getTitle() = "Decimal Settings"
}
