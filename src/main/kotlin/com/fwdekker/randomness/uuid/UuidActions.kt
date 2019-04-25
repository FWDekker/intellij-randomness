package com.fwdekker.randomness.uuid

import com.fwdekker.randomness.DataGroupAction
import com.fwdekker.randomness.DataInsertAction
import com.fwdekker.randomness.DataInsertArrayAction
import com.fwdekker.randomness.SettingsAction
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
     * Returns a random type 4 UUID.
     *
     * @return a random type 4 UUID
     */
    override fun generateString() = settings.enclosure + UUID.randomUUID().toString() + settings.enclosure
}


/**
 * Inserts an array-like string of UUIDs.
 *
 * @param settings the settings to use for generating UUIDs
 *
 * @see UuidInsertAction
 */
class UuidInsertArrayAction(settings: UuidSettings = UuidSettings.default) :
    DataInsertArrayAction(UuidInsertAction(settings)) {
    override val name = "Insert UUID Array"
}


/**
 * Controller for random UUID generation settings.
 *
 * @see UuidSettings
 * @see UuidSettingsDialog
 */
class UuidSettingsAction : SettingsAction() {
    override val title = "UUID Settings"


    public override fun createDialog() = UuidSettingsDialog()
}
