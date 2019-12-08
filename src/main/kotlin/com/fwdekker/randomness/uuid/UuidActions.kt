package com.fwdekker.randomness.uuid

import com.fasterxml.uuid.Generators
import com.fwdekker.randomness.DataGroupAction
import com.fwdekker.randomness.DataInsertAction
import com.fwdekker.randomness.DataInsertArrayAction
import com.fwdekker.randomness.SettingsAction
import com.fwdekker.randomness.array.ArraySettings
import kotlin.random.asJavaRandom


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
    override val name = "Random UUID"


    /**
     * Returns random type 4 UUIDs.
     *
     * @param count the number of type 4 UUIDs to generate
     * @return random type 4 UUIDs
     */
    override fun generateStrings(count: Int): List<String> {
        val generator = Generators.randomBasedGenerator(random.asJavaRandom())

        return (0 until count)
            .map { generator.generate().toString() }
            .map { settings.capitalization.transform(it) }
            .map {
                if (settings.addDashes) it
                else it.replace("-", "")
            }
            .map { settings.enclosure + it + settings.enclosure }
    }
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
    override val name = "Random UUID Array"
}


/**
 * Controller for random UUID generation settings.
 *
 * @see UuidSettings
 * @see UuidSettingsComponent
 */
class UuidSettingsAction : SettingsAction<UuidSettings>() {
    override val title = "UUID Settings"

    override val configurableClass = UuidSettingsConfigurable::class.java
}
