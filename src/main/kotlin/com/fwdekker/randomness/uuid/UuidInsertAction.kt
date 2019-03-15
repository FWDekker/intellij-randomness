package com.fwdekker.randomness.uuid

import com.fwdekker.randomness.DataInsertAction
import java.util.UUID


/**
 * Generates a random type 4 UUID.
 *
 * @param settings the settings to use for generating integers. Defaults to [UuidSettings.default]
 */
class UuidInsertAction(private val settings: UuidSettings = UuidSettings.default) : DataInsertAction() {
    override val name = "Insert UUID"


    override fun generateString() = settings.enclosure + UUID.randomUUID().toString() + settings.enclosure


    /**
     * Inserts an array of UUIDs.
     */
    inner class ArrayAction : DataInsertAction.ArrayAction(this) {
        override val name = "Insert UUID Array"
    }
}
