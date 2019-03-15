package com.fwdekker.randomness.decimal


import com.fwdekker.randomness.Settings
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil


/**
 * Contains settings for generating random decimals.
 */
@State(name = "DecimalSettings", storages = [Storage("\$APP_CONFIG$/randomness.xml")])
class DecimalSettings : Settings<DecimalSettings> {
    companion object {
        private const val DEFAULT_MIN_VALUE = 0.0
        private const val DEFAULT_MAX_VALUE = 1000.0
        private const val DEFAULT_DECIMAL_COUNT = 2
        private const val DEFAULT_GROUPING_SEPARATOR = '\u0000'
        private const val DEFAULT_DECIMAL_SEPARATOR = '.'


        /**
         * The singleton `DecimalSettings` instance.
         */
        val instance: DecimalSettings
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


    override fun getState() = this

    override fun loadState(state: DecimalSettings) = XmlSerializerUtil.copyBean(state, this)

    /**
     * Sets the character that should separate groups.
     *
     * @param groupingSeparator a string of which the first character should separate groups. If the string is empty, no
     * character is used
     */
    fun setGroupingSeparator(groupingSeparator: String) {
        // TODO Move logic to caller so that Kotlin set() can be used above
        this.groupingSeparator =
            if (groupingSeparator.isEmpty()) '\u0000'
            else groupingSeparator[0]
    }

    /**
     * Sets the character that should separate decimals.
     *
     * @param decimalSeparator a string of which the first character should separate decimals. If the string is empty,
     * no character is used
     */
    fun setDecimalSeparator(decimalSeparator: String) {
        // TODO Move logic to caller so that Kotlin set() can be used above
        this.decimalSeparator =
            if (decimalSeparator.isEmpty()) '\u0000'
            else decimalSeparator[0]
    }
}
