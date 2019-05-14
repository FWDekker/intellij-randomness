package com.fwdekker.randomness.decimal

import com.fwdekker.randomness.DataGroupAction
import com.fwdekker.randomness.DataInsertAction
import com.fwdekker.randomness.DataInsertArrayAction
import com.fwdekker.randomness.SettingsAction
import com.fwdekker.randomness.array.ArraySettings
import java.text.DecimalFormat
import kotlin.random.Random


/**
 * All actions related to inserting decimals.
 */
class DecimalGroupAction : DataGroupAction() {
    override val insertAction = DecimalInsertAction()
    override val insertArrayAction = DecimalInsertArrayAction()
    override val settingsAction = DecimalSettingsAction()
}


/**
 * Inserts random decimals.
 *
 * @param settings the settings to use for generating decimals
 *
 * @see DecimalInsertArrayAction
 * @see DecimalSettings
 */
class DecimalInsertAction(private val settings: DecimalSettings = DecimalSettings.default) : DataInsertAction() {
    override val name = "Insert Decimal"


    /**
     * Returns random decimals between the minimum and maximum value, inclusive.
     *
     * @param count the number of decimals to generate
     * @return random decimals between the minimum and maximum value, inclusive
     */
    override fun generateStrings(count: Int) =
        List(count) { convertToString(Random.nextDouble(settings.minValue, Math.nextUp(settings.maxValue))) }


    /**
     * Returns a nicely formatted representation of a decimal.
     *
     * @param decimal the decimal to format
     * @return a nicely formatted representation of a decimal
     */
    private fun convertToString(decimal: Double): String {
        val format = DecimalFormat()
        format.isGroupingUsed = settings.groupingSeparator.isNotEmpty()

        val symbols = format.decimalFormatSymbols
        symbols.groupingSeparator = settings.groupingSeparator.getOrElse(0) { Char.MIN_VALUE }
        symbols.decimalSeparator = settings.decimalSeparator.getOrElse(0) { Char.MIN_VALUE }
        format.minimumFractionDigits = settings.decimalCount
        format.maximumFractionDigits = settings.decimalCount
        format.decimalFormatSymbols = symbols

        return format.format(decimal)
    }
}


/**
 * Inserts an array-like string of decimals.
 *
 * @param arraySettings the settings to use for generating arrays
 * @param settings the settings to use for generating decimals
 *
 * @see DecimalInsertAction
 */
class DecimalInsertArrayAction(
    arraySettings: ArraySettings = ArraySettings.default,
    settings: DecimalSettings = DecimalSettings.default
) : DataInsertArrayAction(arraySettings, DecimalInsertAction(settings)) {
    override val name = "Insert Decimal Array"
}


/**
 * Controller for random decimal generation settings.
 *
 * @see DecimalSettings
 * @see DecimalSettingsDialog
 */
class DecimalSettingsAction : SettingsAction() {
    override val title = "Decimal Settings"


    public override fun createDialog() = DecimalSettingsDialog()
}
