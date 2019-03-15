package com.fwdekker.randomness.uuid

import com.fwdekker.randomness.DataGroupAction


/**
 * All actions related to inserting UUIDs.
 */
class UuidGroupAction : DataGroupAction() {
    override fun getInsertAction() = UuidInsertAction()

    override fun getInsertArrayAction() = UuidInsertAction().ArrayAction()

    override fun getSettingsAction() = UuidSettingsAction()
}
