package com.fwdekker.randomness.array

import com.fwdekker.randomness.SettingsAction


/**
 * Controller for random array generation settings.
 */
class ArraySettingsAction : SettingsAction() {
    override val title = "Array Settings"


    public override fun createDialog() = ArraySettingsDialog()
}
