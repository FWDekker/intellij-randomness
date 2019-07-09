package com.fwdekker.randomness.array

import com.fwdekker.randomness.SettingsAction


/**
 * Controller for random array generation settings.
 *
 * @see ArraySettings
 * @see ArraySettingsDialog
 */
class ArraySettingsAction : SettingsAction() {
    override val configurable = ArraySettingsConfigurable()
}
