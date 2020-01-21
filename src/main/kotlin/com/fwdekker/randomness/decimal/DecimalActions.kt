package com.fwdekker.randomness.decimal

import com.fwdekker.randomness.DataGenerationException
import com.fwdekker.randomness.DataGroupAction
import com.fwdekker.randomness.DataInsertAction
import com.fwdekker.randomness.DataInsertArrayAction
import com.fwdekker.randomness.DataInsertRepeatAction
import com.fwdekker.randomness.DataInsertRepeatArrayAction
import com.fwdekker.randomness.DataQuickSwitchSchemeAction
import com.fwdekker.randomness.DataSettingsAction
import com.fwdekker.randomness.array.ArrayScheme
import com.fwdekker.randomness.array.ArraySettings
import com.fwdekker.randomness.array.ArraySettingsAction
import icons.RandomnessIcons
import java.text.DecimalFormat
import kotlin.math.nextUp


/**
 * All actions related to inserting decimals.
 */
class DecimalGroupAction : DataGroupAction(RandomnessIcons.Decimal.Base) {
    override val insertAction = DecimalInsertAction()
    override val insertArrayAction = DecimalInsertAction.ArrayAction()
    override val insertRepeatAction = DecimalInsertAction.RepeatAction()
    override val insertRepeatArrayAction = DecimalInsertAction.RepeatArrayAction()
    override val settingsAction = DecimalSettingsAction()
    override val quickSwitchSchemeAction = DecimalSettingsAction.DecimalQuickSwitchSchemeAction()
    override val quickSwitchArraySchemeAction = ArraySettingsAction.ArrayQuickSwitchSchemeAction()
}


/**
 * Inserts random decimals.
 *
 * @param scheme the settings to use for generating decimals
 */
class DecimalInsertAction(private val scheme: DecimalScheme = DecimalSettings.default.currentScheme) :
    DataInsertAction(RandomnessIcons.Decimal.Base) {
    override val name = "Random Decimal"


    /**
     * Returns random decimals between the minimum and maximum value, inclusive.
     *
     * @param count the number of decimals to generate
     * @return random decimals between the minimum and maximum value, inclusive
     */
    override fun generateStrings(count: Int) =
        List(count) {
            if (scheme.minValue > scheme.maxValue)
                throw DataGenerationException("Minimum value is larger than maximum value.")

            convertToString(random.nextDouble(scheme.minValue, scheme.maxValue.nextUp()))
        }


    /**
     * Returns a nicely formatted representation of a decimal.
     *
     * @param decimal the decimal to format
     * @return a nicely formatted representation of a decimal
     */
    private fun convertToString(decimal: Double): String {
        val format = DecimalFormat()
        format.isGroupingUsed = scheme.groupingSeparator.isNotEmpty()

        val symbols = format.decimalFormatSymbols
        symbols.groupingSeparator = scheme.groupingSeparator.getOrElse(0) { Char.MIN_VALUE }
        symbols.decimalSeparator = scheme.decimalSeparator.getOrElse(0) { Char.MIN_VALUE }
        if (scheme.showTrailingZeroes) format.minimumFractionDigits = scheme.decimalCount
        format.maximumFractionDigits = scheme.decimalCount
        format.decimalFormatSymbols = symbols

        return format.format(decimal)
    }


    /**
     * Inserts an array-like string of decimals.
     *
     * @param arrayScheme the scheme to use for generating arrays
     * @param scheme the scheme to use for generating decimals
     */
    class ArrayAction(
        arrayScheme: ArrayScheme = ArraySettings.default.currentScheme,
        scheme: DecimalScheme = DecimalSettings.default.currentScheme
    ) : DataInsertArrayAction(arrayScheme, DecimalInsertAction(scheme), RandomnessIcons.Decimal.Array) {
        override val name = "Random Decimal Array"
    }

    /**
     * Inserts repeated random decimals.
     *
     * @param scheme the settings to use for generating decimals
     */
    class RepeatAction(scheme: DecimalScheme = DecimalSettings.default.currentScheme) :
        DataInsertRepeatAction(DecimalInsertAction(scheme), RandomnessIcons.Decimal.Repeat) {
        override val name = "Random Repeated Decimal"
    }

    /**
     * Inserts repeated array-like strings of decimals.
     *
     * @param arrayScheme the scheme to use for generating arrays
     * @param scheme the scheme to use for generating decimals
     */
    class RepeatArrayAction(
        arrayScheme: ArrayScheme = ArraySettings.default.currentScheme,
        scheme: DecimalScheme = DecimalSettings.default.currentScheme
    ) : DataInsertRepeatArrayAction(ArrayAction(arrayScheme, scheme), RandomnessIcons.Decimal.RepeatArray) {
        override val name = "Random Repeated Decimal Array"
    }
}


/**
 * Controller for random decimal generation settings.
 *
 * @see DecimalSettings
 * @see DecimalSettingsComponent
 */
class DecimalSettingsAction : DataSettingsAction(RandomnessIcons.Decimal.Settings) {
    override val name = "Decimal Settings"

    override val configurableClass = DecimalSettingsConfigurable::class.java


    /**
     * Opens a popup to allow the user to quickly switch to the selected scheme.
     *
     * @param settings the settings containing the schemes that can be switched between
     */
    class DecimalQuickSwitchSchemeAction(settings: DecimalSettings = DecimalSettings.default) :
        DataQuickSwitchSchemeAction<DecimalScheme>(settings, RandomnessIcons.Decimal.QuickSwitchScheme) {
        override val name = "Quick Switch Decimal Scheme"
    }
}
