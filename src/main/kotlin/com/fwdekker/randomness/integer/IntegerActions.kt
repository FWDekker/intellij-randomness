package com.fwdekker.randomness.integer

import com.fwdekker.randomness.DataGroupAction
import com.fwdekker.randomness.DataInsertAction
import com.fwdekker.randomness.DataInsertArrayAction
import com.fwdekker.randomness.SettingsAction
import java.text.DecimalFormat
import kotlin.random.Random


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
    override val name = "Insert Integer"


    /**
     * Returns random integers between the minimum and maximum value, inclusive.
     *
     * @param count the number of integers to generate
     * @return random integers between the minimum and maximum value, inclusive
     */
    override fun generateStrings(count: Int) =
        List(count) {convertToString(Random.nextLong(settings.minValue, settings.maxValue + 1))}


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
        format.isGroupingUsed = settings.groupingSeparator != '\u0000'

        val symbols = format.decimalFormatSymbols
        symbols.groupingSeparator = settings.groupingSeparator
        format.minimumFractionDigits = 0
        format.maximumFractionDigits = 0
        format.decimalFormatSymbols = symbols

        return format.format(value)
    }
}


/**
 * Inserts an array-like string of integers.
 *
 * @param settings the settings to use for generating integers
 *
 * @see IntegerInsertAction
 */
class IntegerInsertArrayAction(settings: IntegerSettings = IntegerSettings.default) :
    DataInsertArrayAction(IntegerInsertAction(settings)) {
    override val name = "Insert Integer Array"
}


/**
 * Controller for random integer generation settings.
 *
 * @see IntegerSettings
 * @see IntegerSettingsDialog
 */
class IntegerSettingsAction : SettingsAction() {
    override val title = "Integer Settings"


    public override fun createDialog() = IntegerSettingsDialog()
}
