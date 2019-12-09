package com.fwdekker.randomness.integer

import com.fwdekker.randomness.DataGenerationException
import com.fwdekker.randomness.DataGroupAction
import com.fwdekker.randomness.DataInsertAction
import com.fwdekker.randomness.DataInsertArrayAction
import com.fwdekker.randomness.SettingsAction
import com.fwdekker.randomness.array.ArraySettings
import java.text.DecimalFormat


/**
 * All actions related to inserting integers.
 */
class IntegerGroupAction : DataGroupAction() {
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
class IntegerInsertAction(private val settings: IntegerSettings = IntegerSettings.default) : DataInsertAction() {
    override val name = "Random Integer"


    /**
     * Returns random integers between the minimum and maximum value, inclusive.
     *
     * @param count the number of integers to generate
     * @return random integers between the minimum and maximum value, inclusive
     */
    override fun generateStrings(count: Int) =
        List(count) {
            if (settings.minValue > settings.maxValue)
                throw DataGenerationException("Minimum value is larger than maximum value.")

            convertToString(random.nextLong(settings.minValue, settings.maxValue + 1))
        }


    /**
     * Returns a nicely formatted representation of an integer.
     *
     * @param value the value to format
     * @return a nicely formatted representation of an integer
     */
    private fun convertToString(value: Long): String {
        if (settings.base != IntegerSettings.DECIMAL_BASE)
            return value.toString(settings.base)

        val format = DecimalFormat()
        format.isGroupingUsed = settings.groupingSeparator.isNotEmpty()
        format.minimumFractionDigits = 0
        format.maximumFractionDigits = 0
        format.decimalFormatSymbols = format.decimalFormatSymbols
            .also { it.groupingSeparator = settings.groupingSeparator.getOrElse(0) { Char.MIN_VALUE } }

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
) : DataInsertArrayAction(arraySettings, IntegerInsertAction(settings)) {
    override val name = "Random Integer Array"
}


/**
 * Controller for random integer generation settings.
 *
 * @see IntegerSettings
 * @see IntegerSettingsComponent
 */
class IntegerSettingsAction : SettingsAction<IntegerSettings>() {
    override val title = "Integer Settings"

    override val configurableClass = IntegerSettingsConfigurable::class.java
}
