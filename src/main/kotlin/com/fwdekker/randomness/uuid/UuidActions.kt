package com.fwdekker.randomness.uuid

import com.fasterxml.uuid.EthernetAddress
import com.fasterxml.uuid.Generators
import com.fasterxml.uuid.UUIDTimer
import com.fwdekker.randomness.DataGenerationException
import com.fwdekker.randomness.DataGroupAction
import com.fwdekker.randomness.DataInsertAction
import com.fwdekker.randomness.DataInsertArrayAction
import com.fwdekker.randomness.DataSettingsAction
import com.fwdekker.randomness.array.ArrayScheme
import com.fwdekker.randomness.array.ArraySettings
import icons.RandomnessIcons
import kotlin.random.asJavaRandom


/**
 * All actions related to inserting UUIDs.
 */
class UuidGroupAction : DataGroupAction(RandomnessIcons.Uuid.Base) {
    override val insertAction = UuidInsertAction()
    override val insertArrayAction = UuidInsertArrayAction()
    override val settingsAction = UuidSettingsAction()
}


/**
 * Inserts random type 4 UUID.
 *
 * @param scheme the scheme to use for generating UUIDs
 *
 * @see UuidInsertArrayAction
 * @see UuidSettings
 */
class UuidInsertAction(private val scheme: UuidScheme = UuidSettings.default.currentScheme) :
    DataInsertAction(RandomnessIcons.Uuid.Base) {
    override val name = "Random UUID"


    /**
     * Returns random type 4 UUIDs.
     *
     * @param count the number of type 4 UUIDs to generate
     * @return random type 4 UUIDs
     */
    override fun generateStrings(count: Int): List<String> {
        @Suppress("MagicNumber") // UUID version is not magic
        val generator = when (scheme.version) {
            1 ->
                Generators.timeBasedGenerator(
                    EthernetAddress(random.nextLong()),
                    UUIDTimer(random.asJavaRandom(), null)
                )
            4 -> Generators.randomBasedGenerator(random.asJavaRandom())
            else -> throw DataGenerationException("Unknown UUID version `${scheme.version}`.")
        }

        return (0 until count)
            .map { generator.generate().toString() }
            .map { scheme.capitalization.transform(it) }
            .map {
                if (scheme.addDashes) it
                else it.replace("-", "")
            }
            .map { scheme.enclosure + it + scheme.enclosure }
    }
}


/**
 * Inserts an array-like string of UUIDs.
 *
 * @param arrayScheme the scheme to use for generating arrays
 * @param scheme the scheme to use for generating UUIDs
 *
 * @see UuidInsertAction
 */
class UuidInsertArrayAction(
    arrayScheme: ArrayScheme = ArraySettings.default.currentScheme,
    scheme: UuidScheme = UuidSettings.default.currentScheme
) : DataInsertArrayAction(arrayScheme, UuidInsertAction(scheme), RandomnessIcons.Uuid.Array) {
    override val name = "Random UUID Array"
}


/**
 * Controller for random UUID generation settings.
 *
 * @see UuidSettings
 * @see UuidSettingsComponent
 */
class UuidSettingsAction : DataSettingsAction<UuidSettings, UuidScheme>(RandomnessIcons.Uuid.Settings) {
    override val title = "UUID Settings"

    override val configurableClass = UuidSettingsConfigurable::class.java
}
