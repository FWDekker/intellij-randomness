package com.fwdekker.randomness.array

import com.fwdekker.randomness.SettingsAction


/**
 * Controller for random array generation settings.
 */
class ArraySettingsAction : SettingsAction() {
    public override fun createDialog() = ArraySettingsDialog()

    public override fun getTitle() = "Array Settings"
}
