package com.fwdekker.randomness.integer

import com.fwdekker.randomness.Settings
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil


/**
 * Contains settings for generating random integers.
 *
 * @see IntegerInsertAction
 * @see IntegerSettingsAction
 * @see IntegerSettingsDialog
 */
@State(name = "IntegerSettings", storages = [Storage("\$APP_CONFIG\$/randomness.xml")])
class IntegerSettings : Settings<IntegerSettings> {
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
        const val DEFAULT_GROUPING_SEPARATOR = '\u0000'


        /**
         * The persistent `IntegerSettings` instance.
         */
        val default: IntegerSettings
            get() = ServiceManager.getService(IntegerSettings::class.java)
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
     * The base the generated value should be displayed in.
     */
    var base = DEFAULT_BASE
    /**
     * The character that should separate groups.
     */
    var groupingSeparator = DEFAULT_GROUPING_SEPARATOR


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
