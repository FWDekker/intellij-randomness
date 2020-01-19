package com.fwdekker.randomness.uuid

import com.fasterxml.uuid.EthernetAddress
import com.fasterxml.uuid.Generators
import com.fasterxml.uuid.UUIDTimer
import com.fwdekker.randomness.DataGenerationException
import com.fwdekker.randomness.DataGroupAction
import com.fwdekker.randomness.DataInsertAction
import com.fwdekker.randomness.DataInsertArrayAction
import com.fwdekker.randomness.DataInsertRepeatAction
import com.fwdekker.randomness.DataInsertRepeatArrayAction
import com.fwdekker.randomness.DataQuickSwitchSchemeAction
import com.fwdekker.randomness.DataSettingsAction
import com.fwdekker.randomness.array.ArrayScheme
import com.fwdekker.randomness.array.ArraySettings
import com.fwdekker.randomness.array.ArraySettingsAction
import icons.RandomnessIcons
import kotlin.random.asJavaRandom


/**
 * All actions related to inserting UUIDs.
 */
class UuidGroupAction : DataGroupAction(RandomnessIcons.Uuid.Base) {
    override val insertAction = UuidInsertAction()
    override val insertArrayAction = UuidInsertAction.ArrayAction()
    override val insertRepeatAction = UuidInsertAction.RepeatAction()
    override val insertRepeatArrayAction = UuidInsertAction.RepeatArrayAction()
    override val settingsAction = UuidSettingsAction()
    override val quickSwitchSchemeAction = UuidSettingsAction.UuidQuickSwitchSchemeAction()
    override val quickSwitchArraySchemeAction = ArraySettingsAction.ArrayQuickSwitchSchemeAction()
}


/**
 * Inserts random type 4 UUID.
 *
 * @param scheme the scheme to use for generating UUIDs
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


    /**
     * Inserts an array-like string of UUIDs.
     *
     * @param arrayScheme the scheme to use for generating arrays
     * @param scheme the scheme to use for generating UUIDs
     */
    class ArrayAction(
        arrayScheme: ArrayScheme = ArraySettings.default.currentScheme,
        scheme: UuidScheme = UuidSettings.default.currentScheme
    ) : DataInsertArrayAction(arrayScheme, UuidInsertAction(scheme), RandomnessIcons.Uuid.Array) {
        override val name = "Random UUID Array"
    }

    /**
     * Inserts repeated random UUIDs.
     *
     * @param scheme the settings to use for generating UUIDs
     */
    class RepeatAction(scheme: UuidScheme = UuidSettings.default.currentScheme) :
        DataInsertRepeatAction(UuidInsertAction(scheme), RandomnessIcons.Uuid.Repeat) {
        override val name = "Random Repeated Uuid"
    }

    /**
     * Inserts repeated array-like strings of UUIDs.
     *
     * @param arrayScheme the scheme to use for generating arrays
     * @param scheme the scheme to use for generating UUIDs
     */
    class RepeatArrayAction(
        arrayScheme: ArrayScheme = ArraySettings.default.currentScheme,
        scheme: UuidScheme = UuidSettings.default.currentScheme
    ) : DataInsertRepeatArrayAction(ArrayAction(arrayScheme, scheme), RandomnessIcons.Uuid.RepeatArray) {
        override val name = "Random Repeated Uuid Array"
    }
}


/**
 * Controller for random UUID generation settings.
 *
 * @see UuidSettings
 * @see UuidSettingsComponent
 */
class UuidSettingsAction : DataSettingsAction(RandomnessIcons.Uuid.Settings) {
    override val title = "UUID Settings"

    override val configurableClass = UuidSettingsConfigurable::class.java


    /**
     * Opens a popup to allow the user to quickly switch to the selected scheme.
     *
     * @param settings the settings containing the schemes that can be switched between
     */
    class UuidQuickSwitchSchemeAction(settings: UuidSettings = UuidSettings.default) :
        DataQuickSwitchSchemeAction<UuidScheme>(settings, RandomnessIcons.Uuid.Settings) {
        override val title = "Quick Switch UUID Scheme"
    }
}
