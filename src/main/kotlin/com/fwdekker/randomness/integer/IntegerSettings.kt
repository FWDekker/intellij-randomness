package com.fwdekker.randomness.integer

import com.fwdekker.randomness.Settings
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil


/**
 * Contains settings for generating random integers.
 *
 * @property minValue The minimum value to be generated, inclusive.
 * @property maxValue The maximum value to be generated, inclusive.
 * @property base The base the generated value should be displayed in.
 * @property groupingSeparator The character that should separate groups.
 *
 * @see IntegerInsertAction
 * @see IntegerSettingsAction
 * @see IntegerSettingsDialog
 */
// TODO Turn the separator property into a char property once supported by the settings serializer
@State(name = "IntegerSettings", storages = [Storage("\$APP_CONFIG\$/randomness.xml")])
data class IntegerSettings(
    var minValue: Long = DEFAULT_MIN_VALUE,
    var maxValue: Long = DEFAULT_MAX_VALUE,
    var base: Int = DEFAULT_BASE,
    var groupingSeparator: String = DEFAULT_GROUPING_SEPARATOR
) : Settings<IntegerSettings> {
    companion object {
        /**
         * The minimum value of the [base][IntegerSettings.base] field.
         */
        const val MIN_BASE = 2
        /**
         * The maximum value of the [base][IntegerSettings.base] field.
         */
        const val MAX_BASE = 36
        /**
         * The definition of decimal base.
         */
        const val DECIMAL_BASE = 10

        /**
         * The default value of the [minValue][IntegerSettings.minValue] field.
         */
        const val DEFAULT_MIN_VALUE = 0L
        /**
         * The default value of the [maxValue][IntegerSettings.maxValue] field.
         */
        const val DEFAULT_MAX_VALUE = 1000L
        /**
         * The default value of the [base][IntegerSettings.base] field.
         */
        const val DEFAULT_BASE = DECIMAL_BASE
        /**
         * The default value of the [groupingSeparator][IntegerSettings.groupingSeparator] field.
         */
        const val DEFAULT_GROUPING_SEPARATOR = ""


        /**
         * The persistent `IntegerSettings` instance.
         */
        val default: IntegerSettings
            get() = ServiceManager.getService(IntegerSettings::class.java)
    }


    /**
     * Sets the grouping separator safely to ensure that exactly one character is set.
     *
     * @param groupingSeparator the possibly-unsafe grouping separator string
     */
    fun safeSetGroupingSeparator(groupingSeparator: String?) {
        if (groupingSeparator == null || groupingSeparator.isEmpty())
            this.groupingSeparator = DEFAULT_GROUPING_SEPARATOR
        else
            this.groupingSeparator = groupingSeparator.substring(0, 1)
    }


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
    override fun loadState(state: IntegerSettings) = XmlSerializerUtil.copyBean(state, this)
}
