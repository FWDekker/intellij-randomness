package com.fwdekker.randomness.uuid

import com.fwdekker.randomness.DataGroupAction


/**
 * All actions related to inserting UUIDs.
 */
class UuidGroupAction : DataGroupAction() {
    override val insertAction = UuidInsertAction()
    override val insertArrayAction = UuidInsertAction().ArrayAction()
    override val settingsAction = UuidSettingsAction()
}
