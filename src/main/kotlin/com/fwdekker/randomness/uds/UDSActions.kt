package com.fwdekker.randomness.uds

import com.fwdekker.randomness.DataGroupAction
import com.fwdekker.randomness.DataInsertAction
import com.fwdekker.randomness.DataInsertArrayAction
import com.fwdekker.randomness.DataInsertRepeatAction
import com.fwdekker.randomness.DataInsertRepeatArrayAction
import com.fwdekker.randomness.DataSettingsAction
import com.fwdekker.randomness.array.ArraySettings
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
}


/**
 * Inserts random arbitrary strings based on the UDS descriptor.
 *
 * @param scheme the scheme to use for generating UDS-based strings
 */
class UDSInsertAction(private val scheme: UDSSettings = UDSSettings.default) :
    DataInsertAction(RandomnessIcons.Data.Base) {
    override val name = "Random Decimal"


    /**
     * Returns random UDS-based strings based on the descriptor.
     *
     * @param count the number of strings to generate
     * @return random UDS-based strings based on the descriptor
     */
    override fun generateStrings(count: Int) = scheme.generateStrings(count)

    /**
     * Inserts an array-like string of UDS-based strings.
     *
     * @param arrayScheme the scheme to use for generating arrays
     * @param scheme the scheme to use for generating strings
     */
    class ArrayAction(
        arrayScheme: ArraySettings = ArraySettings.default,
        scheme: UDSSettings = UDSSettings.default
    ) : DataInsertArrayAction(arrayScheme, UDSInsertAction(scheme), RandomnessIcons.Data.Array) {
        override val name = "Random UDS Array"
    }

    /**
     * Inserts repeated random UDS-based strings.
     *
     * @param scheme the settings to use for generating strings
     */
    class RepeatAction(scheme: UDSSettings = UDSSettings.default) :
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
        arrayScheme: ArraySettings = ArraySettings.default,
        scheme: UDSSettings = UDSSettings.default
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
}
