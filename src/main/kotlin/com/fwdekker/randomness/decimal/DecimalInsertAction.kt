package com.fwdekker.randomness.decimal

import com.fwdekker.randomness.DataInsertAction
import java.text.DecimalFormat
import java.util.concurrent.ThreadLocalRandom


/**
 * Generates a random integer based on the settings in [DecimalSettings].
 *
 * @param decimalSettings the settings to use for generating decimals. Defaults to [DecimalSettings.instance]
 */
class DecimalInsertAction(private val decimalSettings: DecimalSettings = DecimalSettings.instance) :
    DataInsertAction() {
    override fun getName() = "Insert Decimal" // TODO convert to field in superclass


    /**
     * Returns a random integer between the minimum and maximum value, inclusive.
     *
     * @return a random integer between the minimum and maximum value, inclusive
     */
    public override fun generateString(): String {
        // TODO Use Kotlin's Random?
        val randomValue = ThreadLocalRandom.current()
            .nextDouble(decimalSettings.minValue, Math.nextUp(decimalSettings.maxValue))

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
        format.isGroupingUsed = decimalSettings.groupingSeparator != '\u0000'

        val symbols = format.decimalFormatSymbols
        symbols.groupingSeparator = decimalSettings.groupingSeparator
        symbols.decimalSeparator = decimalSettings.decimalSeparator
        format.minimumFractionDigits = decimalSettings.decimalCount
        format.maximumFractionDigits = decimalSettings.decimalCount
        format.decimalFormatSymbols = symbols

        return format.format(decimal)
    }


    /**
     * Inserts an array of decimals.
     */
    inner class ArrayAction : DataInsertAction.ArrayAction(this@DecimalInsertAction) {
        // TODO Inspect the use of `this@` above
        override fun getName() = "Insert Decimal Array"
    }
}
