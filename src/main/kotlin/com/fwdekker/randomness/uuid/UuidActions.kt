package com.fwdekker.randomness.uuid

import com.fwdekker.randomness.DataGroupAction
import com.fwdekker.randomness.DataInsertAction
import com.fwdekker.randomness.DataInsertArrayAction
import com.fwdekker.randomness.SettingsAction
import com.fwdekker.randomness.array.ArraySettings
import java.util.UUID


/**
 * All actions related to inserting UUIDs.
 */
class UuidGroupAction : DataGroupAction() {
    override val insertAction = UuidInsertAction()
    override val insertArrayAction = UuidInsertArrayAction()
    override val settingsAction = UuidSettingsAction()
}


/**
 * Inserts random type 4 UUID.
 *
 * @param settings the settings to use for generating UUIDs
 *
 * @see UuidInsertArrayAction
 * @see UuidSettings
 */
class UuidInsertAction(private val settings: UuidSettings = UuidSettings.default) : DataInsertAction() {
    override val name = "Insert UUID"


    /**
     * Returns random type 4 UUIDs.
     *
     * @param count the number of type 4 UUIDs to generate
     * @return random type 4 UUIDs
     */
    override fun generateStrings(count: Int) =
        List(count) { settings.enclosure + UUID.randomUUID().toString() + settings.enclosure }
}


/**
 * Inserts an array-like string of UUIDs.
 *
 * @param arraySettings the settings to use for generating arrays
 * @param settings the settings to use for generating UUIDs
 *
 * @see UuidInsertAction
 */
class UuidInsertArrayAction(
    arraySettings: ArraySettings = ArraySettings.default,
    settings: UuidSettings = UuidSettings.default
) : DataInsertArrayAction(arraySettings, UuidInsertAction(settings)) {
    override val name = "Insert UUID Array"
}


/**
 * Controller for random UUID generation settings.
 *
 * @see UuidSettings
 * @see UuidSettingsDialog
 */
class UuidSettingsAction : SettingsAction() {
    override val configurable = UuidSettingsConfigurable()
}
