package com.fwdekker.randomness.array

import com.fwdekker.randomness.DataSettingsAction


/**
 * Controller for random array generation settings.
 *
 * @see ArraySettings
 * @see ArraySettingsComponent
 */
class ArraySettingsAction : DataSettingsAction<ArraySettings, ArrayScheme>() {
    override val title = "Array Settings"

    override val configurableClass = ArraySettingsConfigurable::class.java
}
