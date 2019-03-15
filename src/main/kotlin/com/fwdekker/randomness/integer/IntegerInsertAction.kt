package com.fwdekker.randomness.integer

import com.fwdekker.randomness.DataInsertAction
import java.text.DecimalFormat
import java.util.concurrent.ThreadLocalRandom


/**
 * Generates a random integer based on the settings in [IntegerSettings].
 *
 * @param settings the settings to use for generating integers. Defaults to [IntegerSettings.instance]
 */
class IntegerInsertAction(private val settings: IntegerSettings = IntegerSettings.instance) : DataInsertAction() {
    override fun getName() = "Insert Integer"


    /**
     * Returns a random integer between the minimum and maximum value, inclusive.
     *
     * @return a random integer between the minimum and maximum value, inclusive
     */
    public override fun generateString(): String {
        val randomValue = ThreadLocalRandom.current()
            .nextLong(settings.minValue, settings.maxValue + 1)

        return convertToString(randomValue)
    }


    /**
     * Returns a nicely formatted representation of a long.
     *
     * @param integer a `long`
     * @return a nicely formatted representation of a long
     */
    private fun convertToString(integer: Long): String {
        // TODO Kotlin-ify this
        if (settings.base != IntegerSettings.DECIMAL_BASE) {
            return java.lang.Long.toString(integer, settings.base)
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


    /**
     * Inserts an array of integers.
     */
    inner class ArrayAction : DataInsertAction.ArrayAction(this@IntegerInsertAction) {
        override fun getName() = "Insert Integer Array"
    }
}
