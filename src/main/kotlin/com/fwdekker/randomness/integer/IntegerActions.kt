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
 * Generates a random integer based on the settings in [IntegerSettings].
 *
 * @param settings the settings to use for generating integers. Defaults to [IntegerSettings.default]
 */
class IntegerInsertAction(private val settings: IntegerSettings = IntegerSettings.default) : DataInsertAction() {
    override val name = "Insert Integer"


    /**
     * Returns a random integer between the minimum and maximum value, inclusive.
     *
     * @return a random integer between the minimum and maximum value, inclusive
     */
    override fun generateString() =
        convertToString(Random.nextLong(settings.minValue, settings.maxValue + 1))


    /**
     * Returns a nicely formatted representation of a long.
     *
     * @param integer a `long`
     * @return a nicely formatted representation of a long
     */
    private fun convertToString(integer: Long): String {
        if (settings.base != IntegerSettings.DECIMAL_BASE) {
            return integer.toString(settings.base)
        }


        val format = DecimalFormat()
        format.isGroupingUsed = settings.groupingSeparator != '\u0000'

        val symbols = format.decimalFormatSymbols
        symbols.groupingSeparator = settings.groupingSeparator
        format.minimumFractionDigits = 0
        format.maximumFractionDigits = 0
        format.decimalFormatSymbols = symbols

        return format.format(integer)
    }
}


/**
 * Inserts an array of integers.
 */
class IntegerInsertArrayAction(settings: IntegerSettings = IntegerSettings.default) :
    DataInsertArrayAction(IntegerInsertAction(settings)) {
    override val name = "Insert Integer Array"
}


/**
 * Controller for random integer generation settings.
 */
class IntegerSettingsAction : SettingsAction() {
    override val title = "Integer Settings"


    public override fun createDialog() = IntegerSettingsDialog()
}
