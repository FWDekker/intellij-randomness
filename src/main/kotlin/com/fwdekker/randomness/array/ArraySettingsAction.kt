package com.fwdekker.randomness.array

import com.fwdekker.randomness.SettingsAction


/**
 * Controller for random array generation settings.
 *
 * @see ArraySettings
 * @see ArraySettingsComponent
 */
class ArraySettingsAction : SettingsAction<ArraySettings>() {
    override val title = "Array Settings"

    override val configurableClass = ArraySettingsConfigurable::class.java
}
