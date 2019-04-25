package com.fwdekker.randomness.decimal

import com.fwdekker.randomness.DataGroupAction
import com.fwdekker.randomness.DataInsertAction
import com.fwdekker.randomness.DataInsertArrayAction
import com.fwdekker.randomness.SettingsAction
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
     * Returns a random decimal between the minimum and maximum value, inclusive.
     *
     * @return a random decimal between the minimum and maximum value, inclusive
     */
    override fun generateString() =
        convertToString(Random.nextDouble(settings.minValue, Math.nextUp(settings.maxValue)))


    /**
     * Returns a nicely formatted representation of a decimal.
     *
     * @param decimal the decimal to format
     * @return a nicely formatted representation of a decimal
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
}


/**
 * Inserts an array-like string of decimals.
 *
 * @param settings the settings to use for generating decimals
 *
 * @see DecimalInsertAction
 */
class DecimalInsertArrayAction(settings: DecimalSettings = DecimalSettings.default) :
    DataInsertArrayAction(DecimalInsertAction(settings)) {
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
