package com.fwdekker.randomness.integer

import com.fwdekker.randomness.DataGenerationException
import com.fwdekker.randomness.DataGroupAction
import com.fwdekker.randomness.DataInsertAction
import com.fwdekker.randomness.DataInsertArrayAction
import com.fwdekker.randomness.DataSettingsAction
import com.fwdekker.randomness.array.ArraySettings
import com.fwdekker.randomness.integer.IntegerScheme.Companion.DECIMAL_BASE
import icons.RandomnessIcons
import java.text.DecimalFormat


/**
 * All actions related to inserting integers.
 */
class IntegerGroupAction : DataGroupAction(RandomnessIcons.Integer.Base) {
    override val insertAction = IntegerInsertAction()
    override val insertArrayAction = IntegerInsertArrayAction()
    override val settingsAction = IntegerSettingsAction()
}


/**
 * Inserts random integers.
 *
 * @param settings the settings to use for generating integers
 *
 * @see IntegerInsertArrayAction
 * @see IntegerSettings
 */
class IntegerInsertAction(private val settings: IntegerSettings = IntegerSettings.default) :
    DataInsertAction(RandomnessIcons.Integer.Base) {
    override val name = "Random Integer"


    /**
     * Returns random integers between the minimum and maximum value, inclusive.
     *
     * @param count the number of integers to generate
     * @return random integers between the minimum and maximum value, inclusive
     */
    override fun generateStrings(count: Int) =
        List(count) {
            val scheme = settings.currentScheme
            if (scheme.minValue > scheme.maxValue)
                throw DataGenerationException("Minimum value is larger than maximum value.")

            convertToString(random.nextLong(scheme.minValue, scheme.maxValue + 1))
        }


    /**
     * Returns a nicely formatted representation of an integer.
     *
     * @param value the value to format
     * @return a nicely formatted representation of an integer
     */
    private fun convertToString(value: Long): String {
        val scheme = settings.currentScheme

        if (scheme.base != DECIMAL_BASE)
            return value.toString(scheme.base)

        val format = DecimalFormat()
        format.isGroupingUsed = scheme.groupingSeparator.isNotEmpty()
        format.minimumFractionDigits = 0
        format.maximumFractionDigits = 0
        format.decimalFormatSymbols = format.decimalFormatSymbols
            .also { it.groupingSeparator = scheme.groupingSeparator.getOrElse(0) { Char.MIN_VALUE } }

        return format.format(value)
    }
}


/**
 * Inserts an array-like string of integers.
 *
 * @param arraySettings the settings to use for generating arrays
 * @param settings the settings to use for generating integers
 *
 * @see IntegerInsertAction
 */
class IntegerInsertArrayAction(
    arraySettings: ArraySettings = ArraySettings.default,
    settings: IntegerSettings = IntegerSettings.default
) : DataInsertArrayAction(arraySettings, IntegerInsertAction(settings), RandomnessIcons.Integer.Array) {
    override val name = "Random Integer Array"
}


/**
 * Controller for random integer generation settings.
 *
 * @see IntegerSettings
 * @see IntegerSettingsComponent
 */
class IntegerSettingsAction : DataSettingsAction<IntegerSettings, IntegerScheme>(RandomnessIcons.Integer.Settings) {
    override val title = "Integer Settings"

    override val configurableClass = IntegerSettingsConfigurable::class.java
}
