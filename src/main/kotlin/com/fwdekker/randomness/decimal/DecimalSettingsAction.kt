package com.fwdekker.randomness.decimal

import com.fwdekker.randomness.SettingsAction


/**
 * Controller for random decimal generation settings.
 */
class DecimalSettingsAction : SettingsAction() {
    override val title = "Decimal Settings"


    public override fun createDialog() = DecimalSettingsDialog()
}
