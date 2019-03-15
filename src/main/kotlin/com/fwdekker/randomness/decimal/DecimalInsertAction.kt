package com.fwdekker.randomness.decimal

import com.fwdekker.randomness.DataInsertAction
import java.text.DecimalFormat
import java.util.concurrent.ThreadLocalRandom


/**
 * Generates a random integer based on the settings in [DecimalSettings].
 *
 * @param settings the settings to use for generating decimals. Defaults to [DecimalSettings.default]
 */
class DecimalInsertAction(private val settings: DecimalSettings = DecimalSettings.default) : DataInsertAction() {
    override val name = "Insert Decimal"


    /**
     * Returns a random integer between the minimum and maximum value, inclusive.
     *
     * @return a random integer between the minimum and maximum value, inclusive
     */
    public override fun generateString(): String {
        // TODO Use Kotlin's Random?
        val randomValue = ThreadLocalRandom.current()
            .nextDouble(settings.minValue, Math.nextUp(settings.maxValue))

        return convertToString(randomValue)
    }

    /**
     * Returns a nicely formatted representation of a double.
     *
     * @param decimal the double to format
     * @return a nicely formatted representation of a double
     */
    private fun convertToString(decimal: Double): String {
        val format = DecimalFormat()
        format.isGroupingUsed = settings.groupingSeparator != '\u0000'

        val symbols = format.decimalFormatSymbols
        symbols.groupingSeparator = settings.groupingSeparator
        symbols.decimalSeparator = settings.decimalSeparator
        format.minimumFractionDigits = settings.decimalCount
        format.maximumFractionDigits = settings.decimalCount
        format.decimalFormatSymbols = symbols

        return format.format(decimal)
    }


    /**
     * Inserts an array of decimals.
     */
    inner class ArrayAction : DataInsertAction.ArrayAction(this) {
        override val name = "Insert Decimal Array"
    }
}
