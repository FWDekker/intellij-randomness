package com.fwdekker.randomness.uds

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


/**
 * All actions related to inserting UDS-based strings.
 */
class UDSGroupAction : DataGroupAction(RandomnessIcons.Data.Base) {
    override val insertAction = UDSInsertAction()
    override val insertArrayAction = UDSInsertAction.ArrayAction()
    override val insertRepeatAction = UDSInsertAction.RepeatAction()
    override val insertRepeatArrayAction = UDSInsertAction.RepeatArrayAction()
    override val settingsAction = UDSSettingsAction()
    override val quickSwitchSchemeAction = UDSSettingsAction.UDSQuickSwitchSchemeAction()
    override val quickSwitchArraySchemeAction = ArraySettingsAction.ArrayQuickSwitchSchemeAction()
}


/**
 * Inserts random arbitrary strings based on the UDS descriptor.
 *
 * @param scheme the scheme to use for generating UDS-based strings
 */
class UDSInsertAction(private val scheme: UDSScheme = UDSSettings.default.currentScheme) :
    DataInsertAction(RandomnessIcons.Data.Base) {
    override val name = "Random Decimal"


    /**
     * Returns random UDS-based strings based on the descriptor.
     *
     * @param count the number of strings to generate
     * @return random UDS-based strings based on the descriptor
     */
    override fun generateStrings(count: Int) =
        try {
            UDSParser.parse(scheme.descriptor).also { it.random = this.random }.generateStrings(count)
        } catch (e: UDSParseException) {
            throw DataGenerationException(e.message, e)
        }

    /**
     * Inserts an array-like string of UDS-based strings.
     *
     * @param arrayScheme the scheme to use for generating arrays
     * @param scheme the scheme to use for generating strings
     */
    class ArrayAction(
        arrayScheme: ArrayScheme = ArraySettings.default.currentScheme,
        scheme: UDSScheme = UDSSettings.default.currentScheme
    ) : DataInsertArrayAction(arrayScheme, UDSInsertAction(scheme), RandomnessIcons.Data.Array) {
        override val name = "Random UDS Array"
    }

    /**
     * Inserts repeated random UDS-based strings.
     *
     * @param scheme the settings to use for generating strings
     */
    class RepeatAction(scheme: UDSScheme = UDSSettings.default.currentScheme) :
        DataInsertRepeatAction(UDSInsertAction(scheme), RandomnessIcons.Data.Repeat) {
        override val name = "Random Repeated UDS"
    }

    /**
     * Inserts repeated array-like strings of UDS-based strings.
     *
     * @param arrayScheme the scheme to use for generating arrays
     * @param scheme the scheme to use for generating strings
     */
    class RepeatArrayAction(
        arrayScheme: ArrayScheme = ArraySettings.default.currentScheme,
        scheme: UDSScheme = UDSSettings.default.currentScheme
    ) : DataInsertRepeatArrayAction(ArrayAction(arrayScheme, scheme), RandomnessIcons.Data.RepeatArray) {
        override val name = "Random Repeated UDS Array"
    }
}


/**
 * Controller for random string generation settings.
 *
 * @see UDSSettings
 * @see UDSSettingsComponent
 */
class UDSSettingsAction : DataSettingsAction(RandomnessIcons.Data.Settings) {
    override val name = "UDS Settings"

    override val configurableClass = UDSSettingsConfigurable::class.java


    /**
     * Opens a popup to allow the user to quickly switch to the selected scheme.
     *
     * @param settings the settings containing the schemes that can be switched between
     */
    class UDSQuickSwitchSchemeAction(settings: UDSSettings = UDSSettings.default) :
        DataQuickSwitchSchemeAction<UDSScheme>(settings, RandomnessIcons.Data.QuickSwitchScheme) {
        override val name = "Quick Switch UDS Scheme"
    }
}
