package com.fwdekker.randomness.decimal

import com.fwdekker.randomness.Settings
import com.fwdekker.randomness.SettingsConfigurable
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil


/**
 * Contains settings for generating random decimals.
 *
 * @property minValue The minimum value to be generated, inclusive.
 * @property maxValue The maximum value to be generated, inclusive.
 * @property decimalCount The number of decimals to display.
 * @property showTrailingZeroes Whether to include trailing zeroes in the decimals.
 * @property groupingSeparator The character that should separate groups.
 * @property decimalSeparator The character that should separate decimals.
 *
 * @see DecimalInsertAction
 * @see DecimalSettingsAction
 * @see DecimalSettingsComponent
 */
// TODO Turn separator properties into char properties once supported by the settings serializer
@State(name = "DecimalSettings", storages = [Storage("\$APP_CONFIG\$/randomness.xml")])
data class DecimalSettings(
    var minValue: Double = DEFAULT_MIN_VALUE,
    var maxValue: Double = DEFAULT_MAX_VALUE,
    var decimalCount: Int = DEFAULT_DECIMAL_COUNT,
    var showTrailingZeroes: Boolean = DEFAULT_SHOW_TRAILING_ZEROES,
    var groupingSeparator: String = DEFAULT_GROUPING_SEPARATOR,
    var decimalSeparator: String = DEFAULT_DECIMAL_SEPARATOR
) : Settings<DecimalSettings> {
    companion object {
        /**
         * The default value of the [minValue][DecimalSettings.minValue] field.
         */
        const val DEFAULT_MIN_VALUE = 0.0
        /**
         * The default value of the [maxValue][DecimalSettings.maxValue] field.
         */
        const val DEFAULT_MAX_VALUE = 1_000.0
        /**
         * The default value of the [decimalCount][DecimalSettings.decimalCount] field.
         */
        const val DEFAULT_DECIMAL_COUNT = 2
        /**
         * The default value of the [showTrailingZeroes][DecimalSettings.showTrailingZeroes] field.
         */
        const val DEFAULT_SHOW_TRAILING_ZEROES = true
        /**
         * The default value of the [groupingSeparator][DecimalSettings.groupingSeparator] field.
         */
        const val DEFAULT_GROUPING_SEPARATOR = ""
        /**
         * The default value of the [decimalSeparator][DecimalSettings.decimalSeparator] field.
         */
        const val DEFAULT_DECIMAL_SEPARATOR = "."


        /**
         * The persistent `DecimalSettings` instance.
         */
        val default: DecimalSettings
            get() = ServiceManager.getService(DecimalSettings::class.java)
    }


    /**
     * Sets the grouping separator safely to ensure that exactly one character is set.
     *
     * @param groupingSeparator the possibly-unsafe grouping separator string
     */
    fun safeSetGroupingSeparator(groupingSeparator: String?) =
        if (groupingSeparator == null || groupingSeparator.isEmpty())
            this.groupingSeparator = DEFAULT_GROUPING_SEPARATOR
        else
            this.groupingSeparator = groupingSeparator.substring(0, 1)

    /**
     * Sets the decimal separator safely to ensure that exactly one character is set.
     *
     * @param decimalSeparator the possibly-unsafe decimal separator string
     */
    fun safeSetDecimalSeparator(decimalSeparator: String?) =
        if (decimalSeparator == null || decimalSeparator.isEmpty())
            this.decimalSeparator = DEFAULT_DECIMAL_SEPARATOR
        else
            this.decimalSeparator = decimalSeparator.substring(0, 1)


    override fun copyState() = DecimalSettings().also { it.loadState(this) }

    override fun getState() = this

    override fun loadState(state: DecimalSettings) = XmlSerializerUtil.copyBean(state, this)
}


/**
 * The configurable for decimal settings.
 *
 * @see DecimalSettingsAction
 */
class DecimalSettingsConfigurable(
    override val component: DecimalSettingsComponent = DecimalSettingsComponent()
) : SettingsConfigurable<DecimalSettings>() {
    override fun getDisplayName() = "Decimals"
}
