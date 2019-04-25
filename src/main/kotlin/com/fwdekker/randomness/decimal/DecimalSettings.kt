package com.fwdekker.randomness.decimal


import com.fwdekker.randomness.Settings
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil


/**
 * Contains settings for generating random decimals.
 *
 * @see DecimalInsertAction
 * @see DecimalSettingsAction
 * @see DecimalSettingsDialog
 */
@State(name = "DecimalSettings", storages = [Storage("\$APP_CONFIG\$/randomness.xml")])
class DecimalSettings : Settings<DecimalSettings> {
    companion object {
        /**
         * The default value of the [minValue][DecimalSettings.minValue] field.
         */
        const val DEFAULT_MIN_VALUE = 0.0
        /**
         * The default value of the [maxValue][DecimalSettings.maxValue] field.
         */
        const val DEFAULT_MAX_VALUE = 1000.0
        /**
         * The default value of the [decimalCount][DecimalSettings.decimalCount] field.
         */
        const val DEFAULT_DECIMAL_COUNT = 2
        /**
         * The default value of the [groupingSeparator][DecimalSettings.groupingSeparator] field.
         */
        const val DEFAULT_GROUPING_SEPARATOR = '\u0000'
        /**
         * The default value of the [decimalSeparator][DecimalSettings.decimalSeparator] field.
         */
        const val DEFAULT_DECIMAL_SEPARATOR = '.'


        /**
         * The persistent `DecimalSettings` instance.
         */
        val default: DecimalSettings
            get() = ServiceManager.getService(DecimalSettings::class.java)
    }


    /**
     * The minimum value to be generated, inclusive.
     */
    var minValue = DEFAULT_MIN_VALUE
    /**
     * The maximum value to be generated, inclusive.
     */
    var maxValue = DEFAULT_MAX_VALUE
    /**
     * The number of decimals to display.
     */
    var decimalCount = DEFAULT_DECIMAL_COUNT
    /**
     * The character that should separate groups.
     */
    var groupingSeparator = DEFAULT_GROUPING_SEPARATOR
    /**
     * The character that should separate decimals.
     */
    var decimalSeparator = DEFAULT_DECIMAL_SEPARATOR


    /**
     * Returns `this`.
     *
     * @return `this`
     */
    override fun getState() = this

    /**
     * Copies the fields of [state] to `this`.
     *
     * @param state the state to load into `this`
     */
    override fun loadState(state: DecimalSettings) = XmlSerializerUtil.copyBean(state, this)
}
