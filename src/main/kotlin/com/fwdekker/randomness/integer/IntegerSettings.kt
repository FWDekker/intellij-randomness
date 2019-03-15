package com.fwdekker.randomness.integer

import com.fwdekker.randomness.Settings
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil


/**
 * Contains settings for generating random integers.
 */
@State(name = "IntegerSettings", storages = [Storage("\$APP_CONFIG$/randomness.xml")])
class IntegerSettings : Settings<IntegerSettings> {
    companion object {
        const val MIN_BASE = 2
        const val DECIMAL_BASE = 10
        const val MAX_BASE = 36

        private const val DEFAULT_MIN_VALUE = 0L
        private const val DEFAULT_MAX_VALUE = 1000L
        private const val DEFAULT_BASE = 10
        private const val DEFAULT_GROUPING_SEPARATOR = '\u0000'


        /**
         * The singleton `IntegerSettings` instance.
         */
        val default: IntegerSettings = ServiceManager.getService(IntegerSettings::class.java)
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


    override fun getState() = this

    override fun loadState(state: IntegerSettings) = XmlSerializerUtil.copyBean(state, this)


    /**
     * Sets the character that should separate groups.
     *
     * @param groupingSeparator a string of which the first character should separate groups. If the string is empty, no
     * character is used
     */
    fun setGroupingSeparator(groupingSeparator: String) {
        this.groupingSeparator =
            if (groupingSeparator.isEmpty()) '\u0000'
            else groupingSeparator[0]
    }
}
